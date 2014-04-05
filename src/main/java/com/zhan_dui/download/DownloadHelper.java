package com.zhan_dui.download;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.daimajia.alfred.missions.M3U8Mission;
import com.zhan_dui.modal.Animation;
import com.zhan_dui.services.DownloadService;
import com.zhan_dui.services.DownloadService.DownloadServiceBinder;

import java.io.File;

/**
 * Created by daimajia on 14-4-3.
 */
public class DownloadHelper {

    private final String TAG = "DownloadHelper";

    private Context mContext;
    private DownloadServiceBinder mDownloadServiceBinder;
    private Boolean isConnected = false;
    private Object o = new Object();
    private int unbindTimeCounter = 0;


    private static DownloadHelper INSTANCE;


    private DownloadHelper(Context context){
        mContext = context.getApplicationContext();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG,"onServiceConnected()");
            mDownloadServiceBinder = (DownloadService.DownloadServiceBinder)service;
            synchronized (o){
                isConnected = true;
                o.notify();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG,"onService Disconnected()");
            isConnected = false;
        }
    };

    public static synchronized DownloadHelper getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new DownloadHelper(context);
        }
        return INSTANCE;
    }

    public void startDownload(final Animation animation){
        Log.e(TAG,"StartDownload");
        new Thread(){
            @Override
            public void run() {
                super.run();
                if(!isDownloadServiceRunning()){
                    mContext.startService(new Intent(mContext,DownloadService.class));
                }
                if(mDownloadServiceBinder==null || isConnected == false){
                    mContext.bindService(new Intent(mContext, DownloadService.class), connection, Context.BIND_AUTO_CREATE);
                    synchronized (o){
                        while (!isConnected){
                            try {
                                o.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                file.mkdirs();
                M3U8Mission mission = new M3U8Mission(animation.SD,file.getAbsolutePath() + "/" ,animation.Name + ".ts");
                mission.addExtraInformation(mission.getUri(),animation);
                mDownloadServiceBinder.startDownload(mission);
            }
        }.start();
    }

    private boolean isDownloadServiceRunning(){
        ActivityManager manager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)){
            if(DownloadService.class.getName().equals(service.service.getClassName())){
                Log.e(TAG,"Service exist");
                return true;
            }
        }
        Log.e(TAG,"Service not exist");
        return false;
    }

    public void unbindDownloadService(){
        Log.e(TAG,"unbindDownloadService");
        if(++unbindTimeCounter > 1){
            return;
        }
        if(isConnected){
            mContext.unbindService(connection);
        }
    }
}
