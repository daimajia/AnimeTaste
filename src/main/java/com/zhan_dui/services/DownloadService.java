package com.zhan_dui.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.BaseAdapter;

import com.daimajia.alfred.Alfred;
import com.daimajia.alfred.defaults.MissionListenerForNotification;
import com.daimajia.alfred.missions.M3U8Mission;
import com.daimajia.alfred.missions.Mission;
import com.zhan_dui.adapters.DownloadAdapter;

/**
 * Created by daimajia on 14-2-11.
 */
public class DownloadService extends Service implements Alfred.AlfredListener{

    public static final String TAG = "DownloadService";
    private Alfred alfred = Alfred.getInstance();
    private Mission.MissionListener missionListenerForNotification;
    private DownloadAdapter missionAdater;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notifyBuilder;
    private PendingIntent pausePendingIntent,resumePendingIntent,cancelPendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        Alfred.getInstance().bindAlfredLisener(this);
        missionListenerForNotification = new MissionListenerForNotification(this);
        missionAdater = new DownloadAdapter(this);
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

    @Override
    public void onAllMissionFinished() {
        Alfred.getInstance().unBindAlfredLisener(this);
        Log.e(TAG,"onAllMissionFinished() executed");
        stopSelf();
    }

    @Override
    public void onMissionFinished(Mission mission) {

    }

    public class DownloadServiceBinder extends Binder {

        public void startDownload(M3U8Mission mission) {
            mission.addMissionListener(missionListenerForNotification);
            mission.addMissionListener(missionAdater);
            alfred.addMission(mission);
        }

        public BaseAdapter getMissionAdapter(){
            return missionAdater;
        }
    }

}
