package com.zhan_dui.animetaste;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.zhan_dui.adapters.DownloadAdapter;
import com.zhan_dui.download.alfred.missions.Mission;
import com.zhan_dui.model.Animation;
import com.zhan_dui.model.DownloadRecord;
import com.zhan_dui.services.DownloadService;

import java.io.File;

public class DownloadActivity extends ActionBarActivity implements AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener{

    private boolean isConnected = false;
    private ListView mDownloadList = null;
    private DownloadService.DownloadServiceBinder mBinder;
    private DownloadAdapter mAdapter;
    private Context mContext;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (DownloadService.DownloadServiceBinder)service;
            isConnected = true;
            mDownloadList = (ListView)findViewById(R.id.download_list);
            mAdapter = (DownloadAdapter)mBinder.getMissionAdapter();
            mDownloadList.setAdapter(mAdapter);
            mDownloadList.setOnItemClickListener(DownloadActivity.this);
            mDownloadList.setOnItemLongClickListener(DownloadActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        setContentView(R.layout.activity_download);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = new Intent(this,DownloadService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);
        showHelp();
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("我的下载");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isConnected){
            unbindService(connection);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_delete_all:
                new AlertDialog.Builder(DownloadActivity.this)
                        .setTitle(R.string.tip)
                        .setMessage(R.string.delete_all_downloaded)
                        .setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAll();
                            }
                        })
                        .setNegativeButton(R.string.no,null)
                        .create()
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                DownloadRecord.deleteAll();
                updateHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private Handler updateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(DownloadActivity.this,R.string.delete_finish,Toast.LENGTH_SHORT).show();
            if(mAdapter!= null){
                mAdapter.reloadData();
            }
        }
    };

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = mDownloadList.getAdapter().getItem(position);
        if(obj instanceof Mission){
            final Mission m = (Mission)obj;
            show(R.string.tip,getString(R.string.stop_mission,m.getSaveName()),new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mBinder.stopMission(m.getMissionID());
                }
            });
        }else if(obj instanceof DownloadRecord){
            final DownloadRecord r = (DownloadRecord)obj;
            String msg = getString(R.string.surely_delete,r.Name);
            show(R.string.tip,msg,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String path = r.SaveDir + r.SaveFileName;
                    File f = new File(path);
                    if(f.exists() && f.isFile()) {
                        f.delete();
                    }
                    new Delete().from(DownloadRecord.class)
                            .where("AnimationId = ?", r.AnimationId)
                            .executeSingle();
                    DownloadAdapter adapter = (DownloadAdapter)mDownloadList.getAdapter();
                    adapter.reloadData();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DownloadAdapter.ViewHolder holder = (DownloadAdapter.ViewHolder)view.getTag();
        Animation animation = holder.animation;
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra("Animation",animation);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    private void show(int title,int message,DialogInterface.OnClickListener positive){
        String msg = getString(message);
        show(title,msg,positive);
    }
    private void show(int title,String message,DialogInterface.OnClickListener positive){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.yes,positive)
                .setNegativeButton(R.string.no,null)
                .create()
                .show();
    }

    private void showHelp(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int cnt = preferences.getInt("ShowDownloadtip",0);
        if(cnt < 3){
            Toast.makeText(mContext,getString(R.string.download_action_tips),Toast.LENGTH_LONG).show();
            preferences.edit().putInt("ShowDownloadtip",cnt+1).commit();
        }
    }


}
