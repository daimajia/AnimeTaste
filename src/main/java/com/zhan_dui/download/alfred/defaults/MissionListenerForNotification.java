package com.zhan_dui.download.alfred.defaults;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.zhan_dui.download.alfred.missions.M3U8Mission;
import com.zhan_dui.download.alfred.missions.Mission;
import com.zhan_dui.animetaste.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by daimajia on 14-3-31.
 */
public class MissionListenerForNotification implements Mission.MissionListener<M3U8Mission>{
    private Context context;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notifyBuilder;

    private PendingIntent pausePendingIntent,resumePendingIntent,cancelPendingIntent;

    public MissionListenerForNotification(Context context){
        this.context = context;
    }

    @Override
    public void onStart(M3U8Mission mission) {
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        cancelPendingIntent = MissionActionReceiver.buildReceiverPendingIntent(context, MissionActionReceiver.MISSION_TYPE.CANCEL_MISSION, mission.getMissionID());
        pausePendingIntent = MissionActionReceiver.buildReceiverPendingIntent(context, MissionActionReceiver.MISSION_TYPE.PAUSE_MISSION, mission.getMissionID());
        resumePendingIntent = MissionActionReceiver.buildReceiverPendingIntent(context, MissionActionReceiver.MISSION_TYPE.RESUME_MISSION, mission.getMissionID());
        notifyBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(mission.getShowName())
                .setContentText("准备下载")
                .setProgress(100,100,true)
                .setContentInfo("0%")
                .addAction(R.drawable.ic_action_coffee,"暂停", pausePendingIntent)
                .addAction(R.drawable.ic_action_cancel,"停止", cancelPendingIntent)
                .setContentIntent(cancelPendingIntent);
        notificationManager.notify(mission.getMissionID(),notifyBuilder.build());
    }

    @Override
    public void onMetaDataPrepared(M3U8Mission mission) {
        notifyBuilder.setContentText("开始下载");
        notificationManager.notify(mission.getMissionID(),notifyBuilder.build());
    }


    @Override
    public void onPercentageChange(M3U8Mission mission) {
        notifyBuilder.setProgress(100, mission.getPercentage(), false);
        notifyBuilder.setContentInfo(mission.getPercentage()+"%");
        notificationManager.notify(mission.getMissionID(),notifyBuilder.build());
    }

    @Override
    public void onError(M3U8Mission mission, Exception e) {
        e.printStackTrace();
        notificationManager.notify(mission.getMissionID(),notifyBuilder.build());
    }

    @Override
    public void onSuccess(M3U8Mission mission) {
        notifyBuilder.setContentText("下载成功");
        notifyBuilder.setSmallIcon(R.drawable.ic_action_emo_wink);
        notificationManager.notify(mission.getMissionID(),notifyBuilder.build());
    }

    @Override
    public void onFinish(final M3U8Mission mission) {
        if(mission.getResultStatus() == Mission.RESULT_STATUS.SUCCESS){
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    notificationManager.cancel(mission.getMissionID());
                }
            },10000);
        }
    }

    @Override
    public void onSpeedChange(M3U8Mission mission) {
        notifyBuilder.setProgress(100,mission.getPercentage(),false).setContentText("正在下载速度:"+mission.getAccurateReadableSpeed());
        notificationManager.notify(mission.getMissionID(),notifyBuilder.build());
    }

    @Override
    public void onPause(M3U8Mission mission) {
        notifyBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(mission.getShowName())
                .setContentText("暂停")
                .setProgress(100,mission.getPercentage(),false)
                .setContentInfo(mission.getPercentage() + "%")
                .addAction(R.drawable.ic_action_rocket,"继续", resumePendingIntent)
                .addAction(R.drawable.ic_action_cancel,"停止", cancelPendingIntent)
                .setContentIntent(cancelPendingIntent);
        notificationManager.notify(mission.getMissionID(),notifyBuilder.build());
    }

    @Override
    public void onResume(M3U8Mission mission) {
        notifyBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(mission.getShowName())
                .setContentText("正在下载")
                .setProgress(100,mission.getPercentage(),false)
                .setContentInfo(mission.getPercentage() + "%")
                .addAction(R.drawable.ic_action_coffee,"暂停", pausePendingIntent)
                .addAction(R.drawable.ic_action_cancel,"停止", cancelPendingIntent)
                .setContentIntent(cancelPendingIntent);
        notificationManager.notify(mission.getMissionID(),notifyBuilder.build());
    }

    @Override
    public void onCancel(M3U8Mission mission) {
        notificationManager.cancel(mission.getMissionID());
    }
}
