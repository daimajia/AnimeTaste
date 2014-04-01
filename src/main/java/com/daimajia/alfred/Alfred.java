package com.daimajia.alfred;

import com.daimajia.alfred.Missions.Mission;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by daimajia on 14-1-30.
 */
public class Alfred {

    private static int MAX_MISSION_COUNT = 4;
    private static Alfred Instance;
    private static int MissionId = 0;
    protected ExecutorService mExecutorService;
    protected HashMap<Integer,Mission> mMissionBook;

    public static synchronized Alfred getInstance(){
        if(Instance == null){
            Instance = new Alfred();
        }
        return Instance;
    }

    private Alfred(){
        mMissionBook = new HashMap<Integer, Mission>();
        mExecutorService = Executors.newFixedThreadPool(MAX_MISSION_COUNT);
    }

    public int addMission(Mission mission){
        mMissionBook.put(MissionId, mission);
        mExecutorService.execute(mission);
        return MissionId++;
    }

    public static void setMaxMissionCount(int count){
        if(Instance == null)
            MAX_MISSION_COUNT = count;
        else
            throw new IllegalStateException("Can not change max mission count after getInstance been called");
    }

    public void rest(){
        mExecutorService.shutdown();
    }

    public void cancelAll(){

    }

}
