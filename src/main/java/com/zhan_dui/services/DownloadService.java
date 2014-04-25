package com.zhan_dui.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.zhan_dui.adapters.DownloadAdapter;
import com.zhan_dui.animetaste.R;
import com.zhan_dui.download.alfred.Alfred;
import com.zhan_dui.download.alfred.defaults.MissionListenerForNotification;
import com.zhan_dui.download.alfred.defaults.MissionSaver;
import com.zhan_dui.download.alfred.missions.M3U8Mission;
import com.zhan_dui.download.alfred.missions.Mission;

/**
 * Created by daimajia on 14-2-11.
 */
public class DownloadService extends Service implements Mission.MissionListener<M3U8Mission>{

    private Alfred alfred = Alfred.getInstance();
    private DownloadAdapter missionAdapter;

    private final int MSG_REPEAT = 0;
    private final int MSG_START = 1;
    private Handler downloadHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case MSG_REPEAT:
                    Toast.makeText(DownloadService.this, R.string.downloading, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_START:
                    Toast.makeText(DownloadService.this, getString(R.string.start_downloading), Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        missionAdapter = new DownloadAdapter(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class DownloadServiceBinder extends Binder {

        public void startDownload(M3U8Mission mission) {
            if(missionAdapter.isDownloadingRightNow(mission.getUri())){
                downloadHandler.sendEmptyMessage(MSG_REPEAT);
            }else{
                mission.addMissionListener(new MissionListenerForNotification(DownloadService.this));
                mission.addMissionListener(missionAdapter);
                mission.addMissionListener(new MissionSaver());
                mission.addMissionListener(DownloadService.this);
                alfred.addMission(mission);
                downloadHandler.sendEmptyMessage(MSG_START);
            }
        }

        public BaseAdapter getMissionAdapter(){
            return missionAdapter;
        }

        public void stopMission(int MissionID){
            alfred.cancelMission(MissionID);
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
