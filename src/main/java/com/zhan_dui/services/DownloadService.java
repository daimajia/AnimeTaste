package com.zhan_dui.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by daimajia on 14-2-11.
 */
public class DownloadService extends Service{
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
