package com.zhan_dui.animetaste;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ListView;

import com.zhan_dui.services.DownloadService;
import com.zhan_dui.utils.SwipeBackAppCompatActivity;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

/**
 * Created by daimajia on 14-2-11.
 */
public class DownloadActivity extends SwipeBackAppCompatActivity {

    private DownloadService.DownloadServiceBinder mDownloadBinder;
    private boolean isConnected = false;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isConnected = true;
            ListView download = (ListView)findViewById(R.id.download_list);
            DownloadService.DownloadServiceBinder binder = (DownloadService.DownloadServiceBinder)service;
            download.setAdapter(binder.getMissionAdapter());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        setContentView(R.layout.activity_download);
        setTitle(R.string.my_download);
        Intent intent = new Intent(this,DownloadService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isConnected){
            unbindService(connection);
        }
    }
}
