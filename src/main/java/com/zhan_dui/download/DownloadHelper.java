package com.zhan_dui.download;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.zhan_dui.animetaste.R;
import com.zhan_dui.download.alfred.missions.M3U8Mission;
import com.zhan_dui.model.Animation;
import com.zhan_dui.model.DownloadRecord;
import com.zhan_dui.services.DownloadService;
import com.zhan_dui.services.DownloadService.DownloadServiceBinder;
import com.zhan_dui.utils.NetworkUtils;

import java.io.File;

/**
 * Created by daimajia on 14-4-3.
 */
public class DownloadHelper {

    private Context mContext;
    private DownloadServiceBinder mDownloadServiceBinder;
    private Boolean isConnected = false;
    private Object o = new Object();

    public DownloadHelper(Context context){
        mContext = context;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadServiceBinder = (DownloadService.DownloadServiceBinder)service;
            synchronized (o){
                isConnected = true;
                o.notify();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
        }
    };

    public void startDownload(final Animation animation){
        DownloadRecord record = new Select()
                .from(DownloadRecord.class)
                .where("AnimationId = ? and Status = ?",animation.AnimationId, DownloadRecord.STATUS.SUCCESS)
                .executeSingle();
        if(record != null){
            File file = new File(record.SaveDir + record.SaveFileName);
            if(file.exists() && file.isFile()){
                 new AlertDialog.Builder(mContext)
                         .setTitle(R.string.tip)
                         .setMessage(R.string.redownload_tips)
                         .setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                safeDownload(animation);
                             }
                         })
                         .setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                             }
                         })
                         .create()
                         .show();
            }else{
                safeDownload(animation);
            }
        }else {
            safeDownload(animation);
        }
    }

    private void safeDownload(final Animation animation){
        if(NetworkUtils.isNetworkAvailable(mContext)){
            if(NetworkUtils.isWifiConnected(mContext)){
                download(animation);
            }else{
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.tip)
                        .setMessage(R.string.no_wifi_download)
                        .setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                download(animation);
                            }
                        })
                        .setNegativeButton(R.string.no,null)
                        .create()
                        .show();
            }
        }else{
            Toast.makeText(mContext,R.string.no_network,Toast.LENGTH_LONG).show();
        }
    }

    private void download(final Animation animation){
        new Thread(){
            @Override
            public void run() {
                super.run();
                if(!isDownloadServiceRunning()){
                    mContext.startService(new Intent(mContext,DownloadService.class));
                }
                if(mDownloadServiceBinder==null || isConnected == false){
                    mContext.bindService(new Intent(mContext, DownloadService.class), connection, Context.BIND_AUTO_CREATE);
                    synchronized (o){
                        while (!isConnected){
                            try {
                                o.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                file.mkdirs();
                M3U8Mission mission = new M3U8Mission(animation.SD,file.getAbsolutePath() + "/AnimeTaste/" ,animation.Name + ".ts");
                mission.addExtraInformation(mission.getUri(),animation);
                mDownloadServiceBinder.startDownload(mission);
            }
        }.start();
    }


    private boolean isDownloadServiceRunning(){
        ActivityManager manager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)){
            if(DownloadService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    public void unbindDownloadService(){
        if(isDownloadServiceRunning() && isConnected && connection!=null){
            mContext.unbindService(connection);
        }
    }
}
