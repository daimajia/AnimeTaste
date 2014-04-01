package com.daimajia.alfred.Missions;

import com.daimajia.alfred.Utils.AlfredUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by daimajia on 14-1-30.
 */
public class Mission implements Runnable{

    private String Uri;
    private String SaveDir;
    private String SaveName;

    protected int mDownloaded;
    protected int mFileSize;

    private String mOriginFilename;
    private String mExtention;
    private long mPreviousNotifyTime;
    private int mProgressNotifyInterval = 1;
    private TimeUnit mTimeUnit = TimeUnit.SECONDS;

    private STATUS mStatus = STATUS.IDLE;

    private int mPreviousPercentage = -1;

    protected long mStartTime;
    protected long mEndTime;
    protected MissionListener mMissionListener;

    public enum STATUS{
        IDLE,META_READY,DOWNLOADING,PAUSED,ERROR,SUCCESS
    };

    private boolean isDone = false;
    private boolean isPaused = false;
    private boolean isCanceled = false;
    private final Object o = new Object();

    /**
     *
     * @param uri the file url you want to download
     * @param saveDir save to which directory
     * @param saveFilename the file name you want to save as
     */
    public Mission(String uri, String saveDir, String saveFilename){
        this(uri, saveDir);
        SaveName = saveFilename;
    }

    /**
     *
     * @param uri the file url you want to download
     * @param saveDir save to which directory
     */
    public Mission(String uri,String saveDir){
        Uri = uri;
        SaveDir = saveDir;

        mStartTime = System.currentTimeMillis();
        mPreviousNotifyTime = mStartTime;
        setStatus(STATUS.IDLE);

        setOriginFileName(AlfredUtils.getFileName(uri));
        setExtention(AlfredUtils.getFileExtension(uri));

        SaveName = getOriginFileName() + "." + getExtention();
    }

    public void setMissionListener(MissionListener listener){
        mMissionListener = listener;
    }

    public void setProgressNotificateInterval(TimeUnit unit,int interval){
        mTimeUnit = unit;
        mProgressNotifyInterval = interval;
    }

    @Override
    public void run() {
        notifyStart();

        BufferedInputStream in = null;
        FileOutputStream out = null;
        HttpURLConnection httpURLConnection;
        try
        {
            httpURLConnection = (HttpURLConnection) new URL(Uri).openConnection();
            setFileSize(httpURLConnection.getContentLength());

            notifyMetaDataReady();

            in = new BufferedInputStream(httpURLConnection.getInputStream());
            out = getSafeOutputStream(getSaveDir(),getSaveName());

            byte data[] = new byte[1024];
            int count;

            notifyMetaDataReady();

            while (!isCanceled() && (count=in.read(data,0,1024)) != -1){
                out.write(data,0,count);;
                mDownloaded+=count;
                notifyProgressing();
                checkPaused();
            }

            if(isCanceled == false){
                notifyProgressing();
                notifySuccess();
            }else{
                notifyCancel();
            }
        } catch (Exception e) {
            notifyError(e);
        } finally{
            try {
                if(in!=null) in.close();
                if(out!=null) out.close();
            } catch (IOException e) {
                notifyError(e);
            }
            notifyFinish();
        }
    }

    protected FileOutputStream getSafeOutputStream(String directory,String filename) throws FileNotFoundException {
        return new FileOutputStream(directory + File.separator + filename);
    }

    protected void checkPaused(){
        synchronized (o){
            while (isPaused){
                try {
                    notifyPause();
                    o.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void pause(){
        isPaused = true;
        setStatus(STATUS.PAUSED);
    }

    public void resume(){
        synchronized (o){
            isPaused = false;
            o.notifyAll();
        }
        notifyResume();
    }

    public void cancel(){
        isCanceled = true;
    }

    protected boolean isCanceled(){
        return isCanceled;
    }

    protected final void notifyProgressing(){
        if(mMissionListener!=null){
            long currentNotifyTime = System.currentTimeMillis();
            long spend = mTimeUnit.convert(currentNotifyTime - mPreviousNotifyTime,TimeUnit.MILLISECONDS);
            if(spend >= mProgressNotifyInterval && mPreviousPercentage != getPercentage()){
                mPreviousNotifyTime = currentNotifyTime;
                mMissionListener.onProgressing(this);
            }
        }
    }

    protected final void notifyStart(){
        setStatus(STATUS.DOWNLOADING);
        if(mMissionListener!=null){
            mMissionListener.onStart(this);
        }
    }

    protected final void notifyPause(){
        if(mMissionListener!=null){
            mMissionListener.onPause(this);
        }
    }

    protected final void notifyResume(){
        if(mMissionListener!=null){
            mMissionListener.onResume(this);
        }
    }

    protected final void notifyCancel(){
        if(mMissionListener!=null){
            mMissionListener.onCancel(this);
        }
    }

    protected final void notifyMetaDataReady(){
        setStatus(STATUS.META_READY);
        if(mMissionListener!=null){
            mMissionListener.onMetaDataPrepared(this);
        }
    }

    protected final void notifySuccess(){
        setStatus(STATUS.SUCCESS);
        if(mMissionListener!=null){
            mMissionListener.onSuccess(this);
        }
    }

    protected final void notifyError(Exception e){
        setStatus(STATUS.ERROR);
        if(mMissionListener!=null){
            mMissionListener.onError(this, e);
        }
    }

    protected final void notifyFinish(){
        mEndTime = System.currentTimeMillis();
        isDone = true;
        if(mMissionListener!=null){
            mMissionListener.onFinish(this);
        }
    }

    public String getUri() {
        return Uri;
    }

    public String getSaveDir() {
        return SaveDir;
    }

    public String getSaveName() {
        return SaveName;
    }

    public long getDownloaded() {
        return mDownloaded;
    }

    public long getFilesize() {
        return mFileSize;
    }

    protected void setFileSize(int size){
        mFileSize = size;
    }

    public long getTimeSpend() {
        if(mEndTime != 0){
            return mEndTime - mStartTime;
        }else{
            return System.currentTimeMillis() - mStartTime;
        }
    }

    protected void setStatus(STATUS status){
        mStatus = status;
    }

    public String getReadableSpeed(){
        return AlfredUtils.getReadableSpeed(getDownloaded(), getTimeSpend(), TimeUnit.MILLISECONDS);
    }

    public int getPercentage(){
        if(mFileSize == 0){
            return 0;
        }else{
            return (int)(mDownloaded * 100.0f/mFileSize);
        }
    }

    public void setOriginFileName(String name){
        mOriginFilename = name;
    }

    public String getOriginFileName(){
        return mOriginFilename;
    }

    public void setExtention(String extention){
        mExtention = extention;
    }

    public String getExtention(){
        return mExtention;
    }

    public boolean isDone(){
        return isDone;
    }

    public interface MissionListener{
        /**
         * when the run() start, Notice: do not call some get method, such as getFileType, getOriginFileName. Because
         * these information is not ready, you can get them in onMetaDataPrepared.
         * @param mission
         */
        public void onStart(Mission mission);

        /**
         * when the download file meta information ready, such as the target file size,
         * file type, video duration, and some other meta information.
         * @param mission
         */
        public void onMetaDataPrepared(Mission mission);

        /**
         * if you want to update your ui information, you can do in this function(notice: this function
         * was been called in thread). You can involk getPercentage(),getReadableSpeed(),and so on.
         * @param mission
         */
        public void onProgressing(Mission mission);

        /**
         * when error occurs, onError() will be called;
         * @param mission the mission which trigger the error function
         * @param e the exception.
         */
        public void onError(Mission mission, Exception e);

        /**
         * called when download successfully
         * @param mission
         */
        public void onSuccess(Mission mission);

        /**
         * no matter success or failed, this function will be call in the end. You can do some clean up or
         * some other things.
         * @param mission
         */
        public void onFinish(Mission mission);

        public void onPause(Mission mission);

        public void onResume(Mission mission);

        public void onCancel(Mission mission);
    }

}
