package com.zhan_dui.download.alfred.missions;

import com.zhan_dui.utils.m3u8.Element;
import com.zhan_dui.utils.m3u8.Playlist;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by daimajia on 14-2-28.
 */
public class M3U8Mission extends Mission{

    protected int mVideoSegmentCount = -1;
    protected int mDownloadedSgementCount = 0;

    protected int mTotalVideoDuration;
    protected int mDownloadedVideoDuration;

    protected int mCurrentPartDownloaded = 0;
    private int mReopenCount = 30;
    private final int MAX_REOPEN_COUNT = 0;

    protected Playlist mM3U8Playlist;

    public M3U8Mission(String uri,String saveDirectory,String saveName){
        super(uri,saveDirectory,saveName);
    }

    public M3U8Mission(String uri,String saveDirectory,String saveName,String showName){
        super(uri,saveDirectory,saveName,showName);
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
            mVideoSegmentCount = mM3U8Playlist.getElements().size();
            notifyMetaDataReady();

            byte data[] = new byte[1024];
            int count;
            int c = 0;
            for(Element el :mM3U8Playlist){
                HttpURLConnection connection = (HttpURLConnection)new URL(el.getURI().toString()).openConnection();
                connection.setConnectTimeout(5000);
                in = new BufferedInputStream(connection.getInputStream());

                while (isCanceled() == false){
                    try{
                        count = in.read(data,0,1024);
                        if(count == -1){
                            break;
                        }
                        out.write(data,0,count);
                        mCurrentPartDownloaded += count;
                        mDownloaded+=count;
                        notifySpeedChange();
                        checkPaused();
                    }catch (Exception e){
                        //if pause download, it will makes the socket close,and stop the download.
                        //so we need to reopen it.
                        //well, maybe a big bug here, maybe cause dead cycle :( so I set a
                        //max open count to prevent this situation.
                        mReopenCount++;
                        if(mReopenCount > MAX_REOPEN_COUNT){
                            throw new Exception("There is too much open exception.");
                        }
                        connection = (HttpURLConnection)new URL(el.getURI().toString()).openConnection();
                        connection.setRequestProperty("Range","bytes=" + mCurrentPartDownloaded + "-");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        in = new BufferedInputStream(connection.getInputStream());
                    }
                }
                mCurrentPartDownloaded = 0;
                mDownloadedSgementCount++;
                mDownloadedVideoDuration+=el.getDuration();
                notifyPercentageChange();
                if(isCanceled()){
                    notifyCancel();
                    break;
                }
            }
            if(!isCanceled()){
                notifyPercentageChange();
                notifySuccess();
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

    public int getSegmentsCount(){
        return mVideoSegmentCount;
    }

    /**
     * get m3u8 video duration
     * @return the video duration(seconds)
     */
    public int getVideoDuration(){
        return mTotalVideoDuration;
    }

    /**
     * get m3u8 video downloaded duration(seconds)
     * @return
     */
    public int getDownloadedDuration(){
        return mDownloadedVideoDuration;
    }

    public int getDownloadedSegmentCount(){
        return mDownloadedSgementCount;
    }

    public int getCurrentSegmentDownloaded(){
        return mCurrentPartDownloaded;
    }

}
