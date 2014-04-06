package com.zhan_dui.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.BaseAdapter;

import com.zhan_dui.download.alfred.Alfred;
import com.zhan_dui.download.alfred.defaults.MissionListenerForNotification;
import com.zhan_dui.download.alfred.missions.M3U8Mission;
import com.zhan_dui.download.alfred.missions.Mission;
import com.zhan_dui.adapters.DownloadAdapter;

/**
 * Created by daimajia on 14-2-11.
 */
public class DownloadService extends Service{

    public static final String TAG = "DownloadService";
    private Alfred alfred = Alfred.getInstance();
    private Mission.MissionListener missionListenerForNotification;
    private DownloadAdapter missionAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        missionListenerForNotification = new MissionListenerForNotification(this);
        missionAdapter = new DownloadAdapter(this);
        Log.e(TAG, "onCreate() executed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy() executed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG,"onBind() executed");
        return new DownloadServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG,"onUnbind() executed");
        return super.onUnbind(intent);
    }

    public class DownloadServiceBinder extends Binder {

        public void startDownload(M3U8Mission mission) {
            mission.addMissionListener(new MissionListenerForNotification(DownloadService.this));
            mission.addMissionListener(missionAdapter);
            alfred.addMission(mission);
        }

        public BaseAdapter getMissionAdapter(){
            return missionAdapter;
        }
    }

}
