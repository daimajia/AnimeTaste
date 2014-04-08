package com.zhan_dui.animetaste;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.activeandroid.query.Delete;
import com.zhan_dui.adapters.DownloadAdapter;
import com.zhan_dui.download.alfred.missions.Mission;
import com.zhan_dui.modal.Animation;
import com.zhan_dui.modal.DownloadRecord;
import com.zhan_dui.services.DownloadService;

import java.io.File;


/**
 * Created by daimajia on 14-2-11.
 */
public class DownloadActivity extends ActionBarActivity implements AdapterView.OnItemLongClickListener,AdapterView.OnItemClickListener{

    private boolean isConnected = false;
    private ListView mDownloadList = null;
    private DownloadService.DownloadServiceBinder mBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (DownloadService.DownloadServiceBinder)service;
            isConnected = true;
            mDownloadList = (ListView)findViewById(R.id.download_list);
            DownloadService.DownloadServiceBinder binder = (DownloadService.DownloadServiceBinder)service;
            mDownloadList.setAdapter(binder.getMissionAdapter());
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
        setContentView(R.layout.activity_download);
        setTitle(R.string.my_download);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = mDownloadList.getAdapter().getItem(position);
        if(obj instanceof Mission){
            final Mission m = (Mission)obj;
            show(R.string.tip,R.string.stop_mission,new DialogInterface.OnClickListener() {
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
                    adapter.updateList();
                }
            });
        }
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

}
