package com.diandi.klob.player.spider;

import android.util.Base64;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.diandi.klob.player.BuildConfig;
import com.diandi.klob.player.spider.concurrent.RequestProcessor;
import com.diandi.klob.player.spider.concurrent.WorkHandler;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * *******************************************************************************
 * *********    Author : klob(kloblic@gmail.com) .
 * *********    Date : 2015-11-16  .
 * *********    Time : 19:29 .
 * *********    Version : 1.0
 * *********    Copyright © 2015, klob, All Rights Reserved
 * *******************************************************************************
 */

public class YoukuRequest {
    static String TEST_URL = "http://v.youku.com/v_show/id_XNTAzMDQ1MTQw.html?from=s1.8-1-1.2";
    String TAG = getClass().getSimpleName();


    public String getContent(String strUrl) {
        try {
            URL url = new URL(strUrl);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String s = "";
            StringBuffer sb = new StringBuffer("");
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            l(e.toString());
        }
        return "error open url:" + strUrl;
    }

    private String myEncoder(String a, byte[] c, boolean isToBase64) {
        try {
            String result = "";
            ArrayList bytesR = new ArrayList();
            int f = 0;
            int h = 0;
            int q = 0;
            int[] b = new int[256];
            for (int i = 0; i < 256; ++i)
                b[i] = i;
            while (h < 256) {
                f = (f + b[h] + a.charAt(h % a.length())) % 256;
                int temp = b[h];
                b[h] = b[f];
                b[f] = temp;
                ++h;
            }
            f = 0;
            h = 0;
            q = 0;
            while (q < c.length) {
                h = (h + 1) % 256;
                f = (f + b[h]) % 256;
                int temp = b[h];
                b[h] = b[f];
                b[f] = temp;
                byte[] bytes = {(byte) (c[q] ^ b[((b[h] + b[f]) % 256)])};
                bytesR.add(Byte.valueOf(bytes[0]));
                result = result + new String(bytes, "US-ASCII");
                ++q;
            }
            if (isToBase64) {
                Byte[] byteR = (Byte[]) bytesR.toArray(new Byte[bytesR.size()]);
                byte[] bs = new byte[byteR.length];
                for (int i = 0; i < byteR.length; ++i) {
                    bs[i] = byteR[i].byteValue();
                }
                result = Base64.encodeToString(bs, Base64.DEFAULT);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String[] getValues(String vid, String ep) {
        try {
            String template1 = "becaf9be";
            String template2 = "bf7e5f01";
            byte[] bytes = Base64.decode(ep, Base64.DEFAULT);
            ep = new String(bytes, "US-ASCII");
            String temp = myEncoder(template1, bytes, false);
            String[] part = temp.split("_");
            String sid = part[0];
            String token = part[1];
            String whole = sid + "_" + vid + "_" + token;
            byte[] newbytes = whole.getBytes("US-ASCII");
            String epNew = myEncoder(template2, newbytes, true);
            epNew = URLEncoder.encode(epNew);
            String[] rs = new String[3];
            rs[0] = sid;
            rs[1] = token;
            rs[2] = epNew;
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getVideoUrl(final String htmlUrl, final boolean userHd, final MediaLinkGetListener linkGetListener) {
        if (linkGetListener != null) {
            RequestProcessor.execute(new WorkHandler() {
                List<MediaLink> mLinks;
                boolean isSuccess = true;

                @Override
                public void start() {
                    try {
                        //  String fileUrl = getFileUrl(htmlUrl);
                        // return getRealUrl(fileUrl);
                        mLinks = getFileUrl(htmlUrl);
                        isSuccess = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        isSuccess = false;
                        l(TAG, "jsonException");
                    }

                }

                @Override
                public void over() {
                    if (isSuccess) {
                        if (mLinks != null && mLinks.size() != 0) {
                            linkGetListener.onSuccess(mLinks);
                            l(TAG, "onSuccess");
                            if (userHd) {
                                MediaLink recommend = MediaLink.getHdUrl(mLinks);
                                linkGetListener.onRecommend(recommend.m3u8);
                            }else {
                                MediaLink recommend = MediaLink.getCommonUrl(mLinks);
                                linkGetListener.onRecommend(recommend.m3u8);
                            }
                        } else {
                            l(TAG, "onFailure");
                            linkGetListener.onFailure();
                        }
                    } else {
                        l(TAG, "onFailure");
                        linkGetListener.onFailure();
                    }
                }
            });
        }
    }

    private List<MediaLink> getFileUrl(String htmlUrl) throws JSONException {
        htmlUrl = htmlUrl.replace("==", "");
        String id = "";
        String ru = ".*id_(\\w+)\\.html";
        Pattern p = Pattern.compile(ru);

        String u = htmlUrl;
        Matcher m = p.matcher(u);

        if (m.find()) {
            id = m.group(1);
        }

        System.out.println("视频id:" + id);
        String url = "http://v.youku.com/player/getPlayList/VideoIDS/" + id + "/Pf/4/ctype/12/ev/1";
        String s = getContent(url);
        System.out.println(url);
        JSONObject jsobj = new JSONObject();
        jsobj = JSON.parseObject(s);
        JSONArray jsonarr = jsobj.getJSONArray("data");
        System.out.println("data内容：" + jsobj);

        JSONObject obj1 = jsonarr.getJSONObject(0);
        System.out.println("streamtypes：" + obj1.getString("streamtypes"));
        String ep = obj1.getString("ep");
        String oip = obj1.getString("ip");
        String sid = null;
        String token = null;

        List<String> types = M3U8Utils.getStreamtypes(obj1);
        String vid = obj1.getString("videoid");

        String[] values = getValues(vid, ep);
        sid = values[0];
        token = values[1];
        ep = values[2];

        List<MediaLink> m3u8s = new ArrayList<>();
        for (String type1 : types) {
            m3u8s.add(M3U8Utils.builderLink(ep, oip, sid, token, type1, vid));
        }
        System.out.println("m3u8s地址：" + m3u8s);
        String type = "hd2";
        String VideoUrl = "http://pl.youku.com/playlist/m3u8?ctype=12&ep=" + ep +
                "&ev=1&keyframe=1&oip=" + oip + "&sid=" + sid + "&token=" +
                token + "&type=" + type + "&vid=" + vid;

        return m3u8s;
    }

    private List<String> getRealUrl(String strUrl) throws IOException {
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        DataInputStream input = new DataInputStream(conn.getInputStream());

        List urlsList = new ArrayList();
        String line = null;
        String temp = "";
        String http = "";
        while ((line = input.readLine()) != null) {
            if (line.length() <= 100)
                continue;
            temp = line.substring(0, 22);
            if (!http.equals(temp)) {
                urlsList.add(line.substring(0, line.indexOf(".flv") + 4));
                http = temp;
                System.out.println("temp:" + temp + "http:" + http);
            }
        }

        System.out.println("视频真实地址：" + urlsList);
        input.close();
        return urlsList;
    }

    void l(String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, msg);
        }
    }

    void l(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg);
        }
    }

    /*
    data内容：{"controller":{"other_disable":false,"hd3_enabled":false,"mobile_disabled":false,"download_disabled":false,
    "area_code":420100,"pc_disabled":false,"search_count":true,"app_disable":false,"comment_disabled":false,"playmode":"normal",
    "xplayer_disable":false,"pad_disabled":false,"share_disabled":false,"continuous":0,"mp4_restrict":1,"dma_code":4538,
    "stream_mode":2,"circle":false,"tsflag":false,"video_capture":false,"tv_disabled":false},"data":[{"key1":"bd705025",
    "tt":"0","key2":"6c9440e496e507e7","seed":9391,"streamsizes":{"mp4":115959450,"flv":60514407,"3gphd":63431442,"hd2":253123027},
    "streamtypes_o":["hd2","flvhd","mp4","3gp","3gphd"],"segs":{"mp4":[{"no":"0","seconds":407,"size":"29688103","k2":"14360723a762d38cc","k":"b945e1af9b9ce165261eaf5b"},
    {"no":"1","seconds":375,"size":"26551540","k2":"18ee5da6e6dcdadc3","k":"85c01f91c8f3c901261eaf5b"},{"no":"2","seconds":388,"size":"22411812","k2":"15dfb3a0b21635b2c","k":"f8e36547cfed3d1a24126937"},
    {"no":"3","seconds":287,"size":"23592816","k2":"13f194112c64ac272","k":"e50f5bf170d9dd3e261eaf5b"},{"no":"4","seconds":228,"size":"13715179","k2":"17a4203d090d63029","k":"39ea465ca4e9c8af282af580"}],
    "flv":[{"no":"0","seconds":380,"size":"14426879","k2":"12b869ca9e1cb0e7d","k":"2364af4040506d44282af580"},{"no":"1","seconds":390,"size":"14818569","k2":"110768be0cd8a4ebe","k":"8650ce8a2ed8189324126937"},
    {"no":"2","seconds":370,"size":"11038249","k2":"131e95d1f8d7902ab","k":"195ee376fd954b20282af580"},{"no":"3","seconds":284,"size":"11609467","k2":"165c9133753df1293","k":"e5aa3264bbcb8525282af580"},
    {"no":"4","seconds":262,"size":"8621243","k2":"1d613ffc478802f48","k":"789c17b69823397024126937"}],"3gphd":[{"no":"0","seconds":1685,"size":"63431442","k2":"1b355dd9ed06326ac","k":"cc43bec9e6dc20e6261eaf5b"}],"hd2":[{"no":"0","seconds":397,"size":"62439760","k2":"12b317cde518720db","k":"6d789cf342e43f98282af580"},{"no":"1","seconds":364,"size":"56310334","k2":"19022701fa31105f8","k":"a60daa310dce4fb1282af580"},{"no":"2","seconds":387,"size":"48524773","k2":"1cd7883efedf1fb09","k":"a6718b7796c181ae24126937"},{"no":"3","seconds":298,"size":"53795307","k2":"17a9e331f71eb6001","k":"6d4c7e43bafa9e4b261eaf5b"},{"no":"4","seconds":239,"size":"32052853","k2":"1aaaa7d5f4a39c83e","k":"073921dab0f4e5bd261eaf5b"}]},"title":"唯美小清新校园微电影【错过的美好】","userid":"20284548","down":1318,"videoSource":"10020","seconds":"1685.40",
    "streamtypes":["3gphd","flv","mp4","hd2"],"logo":"http://g1.ykimg.com/","categories":"171","up":8937,"streamlogos":{"mp4":0,"flv":0,"3gphd":0,"hd2":0},"ip":3725866121,"videoid":"125761285","ep":"PwXUSQUXL73d2PbI9+JxBtP3sRJr1w/NWRo=","tsup":"EMjlZDR8ACc1HzJNAlPQ*iE",
    "tags":["青春","爱情","校园","唯美","美女","小清新","微电影","屌丝"],"cs":"","ct":"wdyg","vidEncoded":"XNTAzMDQ1MTQw","stream_ids":{"mp4":129929690,"flv":129929631,"3gphd":129929355,"hd2":129929520},"streamfileids":{"mp4":"53*4*53*53*53*31*53*9*53*53*9*53*63*64*64*63*15*63*37*9*25*36*53*55*4*9*31*18*31*18*36*15*15*55*64*55*15*15*48*37*15*36*18*48*18*15*18*63*48*9*15*18*36*48*53*19*31*55*36*55*4*55*6*9*4*63*","flv":"53*4*53*53*53*39*53*9*53*53*9*53*63*64*37*7*4*36*37*9*25*36*53*55*4*9*31*18*31*18*36*15*15*55*64*55*15*15*48*37*15*36*18*48*18*15*18*63*48*9*15*18*36*48*53*19*31*55*36*55*4*55*6*9*4*63*","3gphd":"53*4*53*53*39*53*53*55*53*53*9*53*63*64*37*36*15*19*37*9*25*36*53*55*4*9*31*18*31*18*36*15*15*55*64*55*15*15*48*37*15*36*18*48*18*15*18*63*48*9*15*18*36*48*53*19*31*55*36*55*4*55*6*9*4*63*","hd2":"53*4*53*53*53*55*53*9*53*53*9*53*63*64*31*36*15*6*37*9*25*36*53*55*4*9*31*18*31*18*36*15*15*55*64*55*15*15*48*37*15*36*18*48*18*15*18*63*48*9*15*18*36*48*53*19*31*55*36*55*4*55*6*9*4*63*"},"username":"墨源-BluRay","ts":"EMj5Wjd8ACc1HzJNAW3M*iE"}],"user":{"id":0}}

    * */
}