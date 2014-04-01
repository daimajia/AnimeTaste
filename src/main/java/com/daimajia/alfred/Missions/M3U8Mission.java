package com.daimajia.alfred.Missions;

import net.chilicat.m3u8.Element;
import net.chilicat.m3u8.Playlist;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by daimajia on 14-2-28.
 */
public class M3U8Mission extends Mission{

    protected int mElementCount;
    protected int mDownloadedCount;

    protected int mTotalVideoDuration;
    protected int mDownloadedVideoDuration;

    protected Playlist mM3U8Playlist;

    public M3U8Mission(String uri, String saveDirectory){
        super(uri,saveDirectory);
    }

    public M3U8Mission(String uri,String saveDirectory,String saveName){
        super(uri,saveDirectory,saveName);
    }

    @Override
    public void run() {
        notifyStart();

        BufferedInputStream in = null;
        FileOutputStream out = null;
        HttpURLConnection httpURLConnection;
        try{
            httpURLConnection = (HttpURLConnection) new URL(getUri()).openConnection();
            in = new BufferedInputStream(httpURLConnection.getInputStream());
            out = getSafeOutputStream(getSaveDir(),getSaveName());

            mM3U8Playlist = Playlist.parse(in);
            for(Element el:mM3U8Playlist){
                mTotalVideoDuration += el.getDuration();
            }
            mElementCount = mM3U8Playlist.getElements().size();

            notifyMetaDataReady();

            byte data[] = new byte[1024];
            int count;

            for(Element el :mM3U8Playlist){

                HttpURLConnection connection = (HttpURLConnection)new URL(el.getURI().toString()).openConnection();
                in = new BufferedInputStream(connection.getInputStream());
                while (isCanceled() == false && (count = in.read(data,0,1024)) != -1){
                    out.write(data,0,count);
                    mDownloaded+=count;
                    notifyProgressing();
                    checkPaused();
                }
                mDownloadedCount++;
                mDownloadedVideoDuration+=el.getDuration();

                if(isCanceled()){
                    notifyCancel();
                    break;
                }else{
                    Thread.sleep(1000);
                    notifyProgressing();
                    notifySuccess();
                }
            }
        }catch (Exception e){
            notifyError(e);
        }finally {
            try{
                if(in!=null) in.close();
                if(out!=null) out.close();
            }catch (Exception e){
                notifyError(e);
            }
            notifyFinish();
        }
    }


    @Override
    public int getPercentage() {
        return (int)(mDownloadedVideoDuration * 100.0f/mTotalVideoDuration);
    }

    /**
     * get m3u8 video duration
     * @return the video duration(seconds)
     */
    public int getDuration(){
        return mTotalVideoDuration;
    }

    /**
     * get m3u8 video downloaded duration(seconds)
     * @return
     */
    public int getDownloadedDuration(){
        return mDownloadedVideoDuration;
    }

    public Playlist getPlaylist(){
        return mM3U8Playlist;
    }

}
