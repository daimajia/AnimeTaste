package com.daimajia.alfred.defaults;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.daimajia.alfred.Alfred;

/**
 * Created by daimajia on 14-3-31.
 */
public class MissionActionReceiver extends BroadcastReceiver{

    private static final String ID = "MISSION_ID";
    private static final String TYPE = "MISSION_TYPE";

    public enum MISSION_TYPE{
        PAUSE_MISSION,RESUME_MISSION,CANCEL_MISSION
    }

    public static PendingIntent buildReceiverPendingIntent(Context context,MISSION_TYPE type,int missionID){
        Intent intent = new Intent(context,MissionActionReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ID,missionID);
        intent.putExtra(TYPE,type);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, missionID* 100 + type.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MISSION_TYPE type =(MISSION_TYPE)intent.getExtras().getSerializable(TYPE);
        int missionID = intent.getExtras().getInt(ID);
        if(missionID == -1){
            return;
        }
        switch (type){
            case PAUSE_MISSION:
                Alfred.getInstance().pauseMission(missionID);
                break;
            case RESUME_MISSION:
                Alfred.getInstance().resumeMission(missionID);
                break;
            case CANCEL_MISSION:
                Alfred.getInstance().cancelMission(missionID);
                break;
        }
    }
}
