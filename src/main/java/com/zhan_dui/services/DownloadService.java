package com.zhan_dui.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.BaseAdapter;

import com.zhan_dui.adapters.DownloadAdapter;
import com.zhan_dui.download.alfred.Alfred;
import com.zhan_dui.download.alfred.defaults.MissionListenerForNotification;
import com.zhan_dui.download.alfred.defaults.MissionSaver;
import com.zhan_dui.download.alfred.missions.M3U8Mission;
import com.zhan_dui.download.alfred.missions.Mission;

/**
 * Created by daimajia on 14-2-11.
 */
public class DownloadService extends Service implements Mission.MissionListener<M3U8Mission>{

    public static final String TAG = "DownloadService";
    private Alfred alfred = Alfred.getInstance();
    private DownloadAdapter missionAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
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
        return super.onUnbind(intent);
    }

    public class DownloadServiceBinder extends Binder {

        public void startDownload(M3U8Mission mission) {
            mission.addMissionListener(new MissionListenerForNotification(DownloadService.this));
            mission.addMissionListener(missionAdapter);
            mission.addMissionListener(new MissionSaver());
            mission.addMissionListener(DownloadService.this);
            alfred.addMission(mission);
        }

        public BaseAdapter getMissionAdapter(){
            return missionAdapter;
        }
    }

    private int count = 0;

    @Override
    public void onStart(M3U8Mission mission) {
        count++;
    }

    @Override
    public void onMetaDataPrepared(M3U8Mission mission) {

    }

    @Override
    public void onPercentageChange(M3U8Mission mission) {

    }

    @Override
    public void onSpeedChange(M3U8Mission mission) {

    }

    @Override
    public void onError(M3U8Mission mission, Exception e) {

    }

    @Override
    public void onSuccess(M3U8Mission mission) {

    }

    @Override
    public void onFinish(M3U8Mission mission) {
        count--;
        if(count == 0){
            Log.e(TAG,"StopSelf() executed");
            stopSelf();
        }
    }

    @Override
    public void onPause(M3U8Mission mission) {

    }

    @Override
    public void onResume(M3U8Mission mission) {

    }

    @Override
    public void onCancel(M3U8Mission mission) {

    }
}
