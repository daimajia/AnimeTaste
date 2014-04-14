package com.zhan_dui.download.alfred;

import com.zhan_dui.download.alfred.missions.Mission;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by daimajia on 14-1-30.
 */
public class Alfred {

    private final String TAG = "Alfred";
    private static int MAX_MISSION_COUNT = 5;
    private static Alfred Instance;
    protected ThreadPoolExecutor mExecutorService;
    protected HashMap<Integer,Mission> mMissionBook;

    public static synchronized Alfred getInstance(){
        if(Instance == null || Instance.mExecutorService.isShutdown()){
            Instance = new Alfred();
        }
        return Instance;
    }

    private Alfred(){
        mMissionBook = new HashMap<Integer, Mission>();
        mExecutorService = new AlfredMissionPool(MAX_MISSION_COUNT,MAX_MISSION_COUNT,15,TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>());
    }

    public void addMission(Mission mission){
        mMissionBook.put(mission.getMissionID(), mission);
        mExecutorService.execute(mission);
    }

    public void pauseMission(int missionID){
        if(mMissionBook.containsKey(missionID)){
            mMissionBook.get(missionID).pause();
        }
    }

    public void resumeMission(int missionID){
        if(mMissionBook.containsKey(missionID)){
            mMissionBook.get(missionID).resume();
        }
    }

    public void cancelMission(int missionID){
        if(mMissionBook.containsKey(missionID)){
            mMissionBook.get(missionID).cancel();
        }
    }

    public void surrenderMissions(){
        for(Map.Entry mission : mMissionBook.entrySet()){
            mMissionBook.get(mission).cancel();
        }
    }

    public static void setMaxMissionCount(int count){
        if(Instance == null && count > 0)
            MAX_MISSION_COUNT = count;
        else
            throw new IllegalStateException("Can not change max mission count after getInstance been called");
    }

    private class AlfredMissionPool extends ThreadPoolExecutor {
        private AlfredMissionPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
        }
    }

}
