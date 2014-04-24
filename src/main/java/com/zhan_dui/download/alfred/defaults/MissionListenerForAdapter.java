package com.zhan_dui.download.alfred.defaults;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zhan_dui.download.alfred.missions.M3U8Mission;
import com.zhan_dui.download.alfred.missions.Mission;
import com.zhan_dui.modal.DownloadRecord;

import java.util.ArrayList;

/**
 * Created by daimajia on 14-4-4.
 */
public abstract class MissionListenerForAdapter extends BaseAdapter implements Mission.MissionListener<M3U8Mission>{

    protected ArrayList<M3U8Mission> onGoingMissions = new ArrayList<M3U8Mission>();
    protected ArrayList<DownloadRecord> mCompletedMissions = new ArrayList<DownloadRecord>();

    public MissionListenerForAdapter(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                mCompletedMissions.addAll(DownloadRecord.getAllDownloaded());
                updateUI();
            }
        }.start();
    }

    @Override
    public int getCount() {
        return mCompletedMissions.size() + onGoingMissions.size();
    }

    @Override
    public Object getItem(int position) {
        if(position < onGoingMissions.size()){
            return onGoingMissions.get(position);
        }else{
            return mCompletedMissions.get(position - onGoingMissions.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public abstract View getView(int position, View convertView, ViewGroup parent);


    @Override
    public synchronized void onStart(M3U8Mission mission) {
        onGoingMissions.add(mission);
        ArrayList<DownloadRecord> records = new ArrayList<DownloadRecord>();
        records.addAll(DownloadRecord.getAllDownloaded());
        mCompletedMissions = records;
        updateUI();
    }

    @Override
    public void onMetaDataPrepared(M3U8Mission mission) {}

    @Override
    public void onPercentageChange(M3U8Mission mission) {
        updateUI();
    }

    @Override
    public void onSpeedChange(M3U8Mission mission) {}

    @Override
    public void onError(M3U8Mission mission, Exception e) {}

    @Override
    public void onSuccess(M3U8Mission mission) {
        updateUI();
    }

    @Override
    public synchronized void onFinish(M3U8Mission mission) {
        onGoingMissions.remove(mission);
        reloadData();
    }

    @Override
    public void onPause(M3U8Mission mission) {}

    @Override
    public void onResume(M3U8Mission mission) {}

    @Override
    public void onCancel(M3U8Mission mission) {
        onGoingMissions.remove(mission);
        reloadData();
    }

    public void updateUI(){
        WindTalker.sendEmptyMessage(0);
    }

    public void reloadData(){
        ArrayList<DownloadRecord> records = new ArrayList<DownloadRecord>();
        records.addAll(DownloadRecord.getAllDownloaded());
        mCompletedMissions = records;
        updateUI();
    }

    private Handler WindTalker = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            notifyDataSetChanged();
        }
    };

    public boolean isDownloadingRightNow(String url){
        for(M3U8Mission mission : onGoingMissions){
            if(mission.getUri().equals(url)){
                return true;
            }
        }
        return false;
    }
}
