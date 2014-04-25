package com.zhan_dui.download.alfred.missions;

import com.zhan_dui.download.alfred.utils.AlfredUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by daimajia on 14-1-30.
 */
public class Mission implements Runnable{

    public static int ID = 0;

    private final int MissionID;
    private String Uri;
    private String SaveDir;
    private String SaveName;

    protected int mDownloaded;
    protected int mFileSize;

    private String mOriginFilename;
    private String mExtension;
    private String mShowName;
    private long mPreviousNotifyTime;


    private int mProgressNotifyInterval = 1;
    private TimeUnit mTimeUnit = TimeUnit.SECONDS;

    private long mLastSecondDownloadTime = 0;
    private long mLastSecondDownload = 0;


    private int mPreviousPercentage = -1;

    protected long mStartTime;
    protected long mEndTime;

    public enum RESULT_STATUS{
        STILL_DOWNLOADING,SUCCESS,FAILED
    }
    private RESULT_STATUS mResultStatus = RESULT_STATUS.STILL_DOWNLOADING;

    private boolean isDone = false;
    private boolean isPaused = false;
    private boolean isSuccess = false;
    private boolean isCanceled = false;
    private final Object o = new Object();

    private ArrayList<MissionListener> missionListeners;
    private HashMap<String,Object> extras;

    /**
     *
     * @param uri the file url you want to download
     * @param saveDir save to which directory
     */
    public Mission(String uri,String saveDir){
        MissionID = ID++;
        Uri = uri;
        SaveDir = saveDir;

        mStartTime = System.currentTimeMillis();
        mPreviousNotifyTime = mStartTime;

        setOriginFileName(AlfredUtils.getFileName(uri));
        setExtension(AlfredUtils.getFileExtension(uri));

        SaveName = getOriginFileName() + "." + getExtension();
        missionListeners = new ArrayList<MissionListener>();
        extras = new HashMap<String, Object>();
    }

    /**
     *
     * @param uri the file url you want to download
     * @param saveDir save to which directory
     * @param saveFilename the file name you want to save as
     */
    public Mission(String uri, String saveDir, String saveFilename){
        this(uri, saveDir);
        SaveName = getSafeFilename(saveFilename);
    }

    public Mission(String uri,String saveDir,String saveFilename,String showName){
        this(uri,saveDir,saveFilename);
        this.mShowName = showName;
    }


    public void addMissionListener(MissionListener listener){
        missionListeners.add(listener);
    }

    public void removeMissionListener(MissionListener listener){
        missionListeners.remove(listener);
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
                notifyPercentageChange();
                notifySpeedChange();
                checkPaused();
            }

            if(isCanceled == false){
                notifyPercentageChange();
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

    protected FileOutputStream getSafeOutputStream(String directory,String filename) {
        String filepath;
        if(directory.lastIndexOf(File.separator) != directory.length() - 1){
            directory += File.separator;
        }
        File dir = new File(directory);
        dir.mkdirs();
        filepath = directory + filename;
        File file = new File(filepath);
        try{
            file.createNewFile();
            return new FileOutputStream(file.getCanonicalFile().toString());
        }catch (Exception e){
            e.printStackTrace();
            throw new Error("Can not get an valid output stream");
        }
    }

    protected String getSafeFilename(String name){
        return name.replaceAll("[\\\\|\\/|\\:|\\*|\\?|\\\"|\\<|\\>|\\|]", "-");
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
        if(isPaused() || isDone()) return;
        isPaused = true;
    }

    public void resume(){
        if(!isPaused() || isDone()){
            return;
        }
        synchronized (o){
            isPaused = false;
            o.notifyAll();
        }
        notifyResume();
    }

    public void cancel(){
        isCanceled = true;
        if(isPaused()){
            resume();
        }
    }

    protected final void notifyPercentageChange(){
        if(missionListeners != null && missionListeners.size()!=0){
            int currentPercentage = getPercentage();
            if(mPreviousPercentage != currentPercentage){
                for(MissionListener l:missionListeners){
                    l.onPercentageChange(this);
                }
                mPreviousPercentage = currentPercentage;
            }
        }
    }

    protected final void notifySpeedChange(){
        if(missionListeners != null && missionListeners.size()!=0){
            long currentNotifyTime = System.currentTimeMillis();
            long notifyDuration = currentNotifyTime - mPreviousNotifyTime;
            long spend = mTimeUnit.convert(notifyDuration,TimeUnit.MILLISECONDS);
            if(spend >= mProgressNotifyInterval){
                mPreviousNotifyTime = currentNotifyTime;
                for(MissionListener l : missionListeners){
                    l.onSpeedChange(this);
                }
            }
            long speedRecordDuration = currentNotifyTime - mLastSecondDownloadTime;
            if(TimeUnit.MILLISECONDS.toSeconds(speedRecordDuration) >= 1){
                mLastSecondDownloadTime = currentNotifyTime;
                mLastSecondDownload = getDownloaded();
            }
        }
    }

    protected final void notifyStart(){
        if(missionListeners != null && missionListeners.size()!=0){
            for(MissionListener l : missionListeners){
                l.onStart(this);
            }
        }
    }

    protected final void notifyPause(){
        if(missionListeners != null && missionListeners.size()!=0){
            for(MissionListener l : missionListeners){
                l.onPause(this);
            }
        }
    }

    protected final void notifyResume(){
        if(missionListeners != null && missionListeners.size()!=0){
            for(MissionListener l : missionListeners){
                l.onResume(this);
            }
        }
    }

    protected final void notifyCancel(){
        isDone = true;
        if(missionListeners != null && missionListeners.size()!=0){
            for(MissionListener l : missionListeners){
                l.onCancel(this);
            }
        }
    }

    protected final void notifyMetaDataReady(){
        if(missionListeners != null && missionListeners.size()!=0){
            for(MissionListener l : missionListeners){
                l.onMetaDataPrepared(this);
            }
        }
    }

    protected final void notifySuccess(){
        mResultStatus = RESULT_STATUS.SUCCESS;
        isDone = true;
        isSuccess = true;
        if(missionListeners != null && missionListeners.size()!=0){
            for(MissionListener l : missionListeners){
                l.onSuccess(this);
            }
        }
    }

    protected final void notifyError(Exception e){
        mResultStatus = RESULT_STATUS.FAILED;
        isDone = true;
        isSuccess = false;
        if(missionListeners != null && missionListeners.size()!=0){
            for(MissionListener l : missionListeners){
                l.onError(this, e);
            }
        }
    }

    protected final void notifyFinish(){
        mEndTime = System.currentTimeMillis();
        isDone = true;
        if(missionListeners != null && missionListeners.size()!=0){
            for(MissionListener l : missionListeners){
                l.onFinish(this);
            }
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

    public String getAverageReadableSpeed(){
        return AlfredUtils.getReadableSpeed(getDownloaded(), getTimeSpend(), TimeUnit.MILLISECONDS);
    }

    public String getAccurateReadableSpeed(){
        return AlfredUtils.getReadableSize(getDownloaded() - mLastSecondDownload, false) + "/s";
    }

    public int getPercentage(){
        if(mFileSize == 0){
            return 0;
        }else{
            return (int)(mDownloaded * 100.0f/mFileSize);
        }
    }

    public String getReadablePercentage(){
        StringBuilder builder = new StringBuilder();
        builder.append(getPercentage());
        builder.append("%");
        return builder.toString();
    }

    public void setOriginFileName(String name){
        mOriginFilename = name;
    }

    public String getOriginFileName(){
        return mOriginFilename;
    }

    public void setExtension(String extension){
        mExtension = extension;
    }

    public String getExtension(){
        return mExtension;
    }

    public String getShowName(){
        if(mShowName == null || mShowName.length() == 0){
            return getSaveName();
        }else{
            return mShowName;
        }
    }

    public int getMissionID(){
        return MissionID;
    }

    public boolean isDone(){
        return isDone;
    }

    public boolean isSuccess(){
        return isSuccess;
    }

    public boolean isPaused(){return isPaused;}

    public boolean isCanceled(){
        return isCanceled;
    }

    public RESULT_STATUS getResultStatus(){
        return mResultStatus;
    }

    public void addExtraInformation(String key, Object value){
        extras.put(key,value);
    }

    public void removeExtraInformation(String key){
        extras.remove(key);
    }

    public Object getExtraInformation(String key){
        return extras.get(key);
    }

    public interface MissionListener<T extends Mission>{
        /**
         * when the run() start, Notice: do not call some get method, such as getFileType, getOriginFileName. Because
         * these information is not ready, you can get them in onMetaDataPrepared.
         * @param mission
         */
        public void onStart(T mission);

        /**
         * when the download file meta information ready, such as the target file size,
         * file type, video duration, and some other meta information.
         * @param mission
         */
        public void onMetaDataPrepared(T mission);

        /**
         * if you want to update your ui information, you can do in this function(notice: this function
         * was been called in thread). You can involk getPercentage(),getAverageReadableSpeed(),and so on.
         * @param mission
         */
        public void onPercentageChange(T mission);

        public void onSpeedChange(T mission);
        /**
         * when error occurs, onError() will be called;
         * @param mission the mission which trigger the error function
         * @param e the exception.
         */
        public void onError(T mission, Exception e);

        /**
         * called when download successfully
         * @param mission
         */
        public void onSuccess(T mission);



        /**
         * no matter success or failed, this function will be call in the end. You can do some clean up or
         * some other things.
         * @param mission
         */
        public void onFinish(T mission);

        public void onPause(T mission);

        public void onResume(T mission);

        public void onCancel(T mission);
    }

}
