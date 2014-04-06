package com.zhan_dui.download.alfred.defaults;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zhan_dui.download.alfred.missions.M3U8Mission;
import com.zhan_dui.download.alfred.missions.Mission;

import java.util.ArrayList;

/**
 * Created by daimajia on 14-4-4.
 */
public abstract class MissionListenerForAdapter extends BaseAdapter implements Mission.MissionListener<M3U8Mission>{

    protected ArrayList<M3U8Mission> onGoingMissions = new ArrayList<M3U8Mission>();
    protected Mission getMission(int position){
        return onGoingMissions.get(position);
    }


    @Override
    public int getCount() {
        return onGoingMissions.size();
    }

    @Override
    public Object getItem(int position) {
        return onGoingMissions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public abstract View getView(int position, View convertView, ViewGroup parent);


    @Override
    public void onStart(M3U8Mission mission) {
        onGoingMissions.add(mission);
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
        onGoingMissions.remove(mission.getUri());
        updateUI();
    }

    @Override
    public void onFinish(M3U8Mission mission) {}

    @Override
    public void onPause(M3U8Mission mission) {}

    @Override
    public void onResume(M3U8Mission mission) {}

    @Override
    public void onCancel(M3U8Mission mission) {}

    protected void updateUI(){
        WindTalker.sendEmptyMessage(0);
    }

    private Handler WindTalker = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            notifyDataSetChanged();
        }
    };
}
