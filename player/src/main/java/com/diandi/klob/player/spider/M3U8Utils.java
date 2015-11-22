package com.diandi.klob.player.spider;

import com.alibaba.fastjson.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * *******************************************************************************
 * *********    Author : klob(kloblic@gmail.com) .
 * *********    Date : 2015-11-21  .
 * *********    Time : 10:28 .
 * *********    Version : 1.0
 * *********    Copyright © 2015, klob, All Rights Reserved
 * *******************************************************************************
 */
public class M3U8Utils {

    public static List<String> m3u8ToUrl(String m3u8) throws IOException {
        URL url = new URL(m3u8);
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

    static String PREFIX = "http://pl.youku.com/playlist/m3u8?ctype=12";

    public static MediaLink builderLink(String ep, String oip, String sid, String token, String type, String vid) {
        MediaLink mediaLink = new MediaLink();
        mediaLink.type = type;
        String VideoUrl = PREFIX
                + "&ep=" + ep
                + "&ev=1&keyframe=1&oip=" + oip
                + "&sid=" + sid
                + "&token=" + token
                + "&type=" + type
                + "&vid=" + vid;
        mediaLink.m3u8 = VideoUrl;
        return mediaLink;
    }


    public static List<String> getStreamtypes(JSONObject jsonObject) {
        List<String> types = new ArrayList<>();
        String streamTypes = jsonObject.getString("streamtypes");
        if (streamTypes.contains("3gphd")) {
            types.add("3gphd");
        }
        if (streamTypes.contains("flv")) {
            types.add("flv");
        }
        if (streamTypes.contains("mp4")) {
            types.add("mp4");
        }
        if (streamTypes.contains("flvhd")) {
            types.add("flvhd");
        }
        if (streamTypes.contains("hd2")) {
            types.add("hd2");
        }
        if (streamTypes.contains("hd3")) {
            types.add("hd3");
        }
        return types;

    }
}
