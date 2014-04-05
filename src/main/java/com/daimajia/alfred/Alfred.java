package com.daimajia.alfred;

import android.util.Log;

import com.daimajia.alfred.missions.M3U8Mission;
import com.daimajia.alfred.missions.Mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by daimajia on 14-1-30.
 */
public class Alfred {

    private final String TAG = "Alfred";
    private static int MAX_MISSION_COUNT = 2;
    private static Alfred Instance;
    protected ThreadPoolExecutor mExecutorService;
    protected HashMap<Integer,Mission> mMissionBook;
    protected ArrayList<AlfredListener> mListeners;

    public static synchronized Alfred getInstance(){
        if(Instance == null || Instance.mExecutorService.isShutdown()){
            Instance = new Alfred();
        }
        return Instance;
    }

    private Alfred(){
        mListeners = new ArrayList<AlfredListener>();
        mMissionBook = new HashMap<Integer, Mission>();
        mExecutorService = new AlfredMissionPool(MAX_MISSION_COUNT,MAX_MISSION_COUNT,15,TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>());
        Log.e(TAG,"Create a New Alfred");
    }

    public void bindAlfredLisener(AlfredListener listener){
        if(listener != null){
            mListeners.add(listener);
        }
    }

    public void unBindAlfredLisener(AlfredListener listener){
        if(listener != null){
            mListeners.remove(listener);
        }
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

    public static void setMaxMissionCount(int count){
        if(Instance == null && count > 0)
            MAX_MISSION_COUNT = count;
        else
            throw new IllegalStateException("Can not change max mission count after getInstance been called");
    }

    private class AlfredMissionPool extends ThreadPoolExecutor{
        private AlfredMissionPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);

            M3U8Mission mission = (M3U8Mission)r;
            Log.e(TAG,mission.getUri());

            if(getQueue().size() == 0){
                for(AlfredListener listener : mListeners){
                    listener.onAllMissionFinished();
                }
            }
        }
    }

    public interface AlfredListener{
        public void onMissionFinished(Mission mission);
        public void onAllMissionFinished();
    }

}
