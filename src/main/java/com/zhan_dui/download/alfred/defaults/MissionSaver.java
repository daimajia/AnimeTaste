package com.zhan_dui.download.alfred.defaults;

import com.zhan_dui.download.alfred.missions.M3U8Mission;
import com.zhan_dui.download.alfred.missions.Mission;
import com.zhan_dui.modal.Animation;
import com.zhan_dui.modal.DownloadRecord;

/**
 * Created by daimajia on 14-4-7.
 */
public class MissionSaver implements Mission.MissionListener<M3U8Mission>{

    private void save(M3U8Mission mission){
        Object obj = mission.getExtraInformation(mission.getUri());
        if(obj!=null){
            Animation animation = (Animation)obj;
            DownloadRecord.save(animation,mission);
        }
    }

    @Override
    public void onStart(M3U8Mission mission) {
        save(mission);
    }

    @Override
    public void onMetaDataPrepared(M3U8Mission mission) {
        save(mission);
    }

    @Override
    public void onPercentageChange(M3U8Mission mission) {
        save(mission);
    }

    @Override
    public void onSpeedChange(M3U8Mission mission) {

    }

    @Override
    public void onError(M3U8Mission mission, Exception e) {
        save(mission);
    }

    @Override
    public void onSuccess(M3U8Mission mission) {
        save(mission);
    }

    @Override
    public void onFinish(M3U8Mission mission) {
        save(mission);
    }

    @Override
    public void onPause(M3U8Mission mission) {
        save(mission);
    }

    @Override
    public void onResume(M3U8Mission mission) {

    }

    @Override
    public void onCancel(M3U8Mission mission) {
        save(mission);
    }
}
