package com.daimajia.alfred.defaults;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.daimajia.alfred.missions.M3U8Mission;
import com.daimajia.alfred.missions.Mission;

import java.util.ArrayList;

/**
 * Created by daimajia on 14-4-4.
 */
public abstract class MissionListenerForAdapter extends BaseAdapter implements Mission.MissionListener<M3U8Mission>{

    private ArrayList<M3U8Mission> missions = new ArrayList<M3U8Mission>();

    protected Mission getMission(int position){
        return missions.get(position);
    }

    @Override
    public int getCount() {
        return missions.size();
    }

    @Override
    public Object getItem(int position) {
        return missions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public abstract View getView(int position, View convertView, ViewGroup parent);



    @Override
    public void onStart(M3U8Mission mission) {
        missions.add(mission);
    }

    @Override
    public void onMetaDataPrepared(M3U8Mission mission) {

    }

    @Override
    public void onPercentageChange(M3U8Mission mission) {
        //notify
    }

    @Override
    public void onSpeedChange(M3U8Mission mission) {

    }

    @Override
    public void onError(M3U8Mission mission, Exception e) {
        //判断时间 然后notify
    }

    @Override
    public void onSuccess(M3U8Mission mission) {
        //notify
        missions.remove(mission);
    }

    @Override
    public void onFinish(M3U8Mission mission) {

    }

    @Override
    public void onPause(M3U8Mission mission) {
        //展示错误
    }

    @Override
    public void onResume(M3U8Mission mission) {
        //展示恢复
    }

    @Override
    public void onCancel(M3U8Mission mission) {
        //取消
    }
}
