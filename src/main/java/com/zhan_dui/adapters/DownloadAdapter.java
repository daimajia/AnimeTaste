package com.zhan_dui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zhan_dui.animetaste.PlayActivity;
import com.zhan_dui.animetaste.R;
import com.zhan_dui.download.alfred.defaults.MissionListenerForAdapter;
import com.zhan_dui.download.alfred.missions.M3U8Mission;
import com.zhan_dui.download.alfred.missions.Mission;
import com.zhan_dui.modal.Animation;
import com.zhan_dui.modal.DownloadRecord;

import java.util.ArrayList;


/**
 * Created by daimajia on 14-3-25.
 */
public class DownloadAdapter extends MissionListenerForAdapter implements View.OnClickListener {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<DownloadRecord> mCompletedMissions;
    private ArrayList<DownloadRecord> mCanceledMissions;

    public DownloadAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCompletedMissions = new ArrayList<DownloadRecord>();
        mCanceledMissions = new ArrayList<DownloadRecord>();
        updateList();
    }

    @Override
    public int getCount() {
        return mCompletedMissions.size() + onGoingMissions.size() + mCanceledMissions.size();
    }

    @Override
    public Object getItem(int position) {
        if(position < onGoingMissions.size()){
            return onGoingMissions.get(position);
        }else if(position < onGoingMissions.size() + mCanceledMissions.size()){
            return mCanceledMissions.get(position - onGoingMissions.size());
        }else{
            return mCompletedMissions.get(position - onGoingMissions.size() - mCanceledMissions.size());
        }
    }

    @Override
    public void onFinish(M3U8Mission mission) {
        super.onFinish(mission);
    }

    private void updateList(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                mCompletedMissions.clear();
                mCompletedMissions.addAll(DownloadRecord.getAllDownloaded());
                mCanceledMissions.clear();
                mCanceledMissions.addAll(DownloadRecord.getAllCanceled());
                updateUI();
            }
        }.start();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView thumb;
        TextView title,content,progress;
        ViewHolder holder;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.download_item,null);
            thumb = (ImageView)convertView.findViewById(R.id.thumbImage);
            title = (TextView)convertView.findViewById(R.id.title);
            content = (TextView)convertView.findViewById(R.id.content);
            progress = (TextView)convertView.findViewById(R.id.progress);
            holder = new ViewHolder(title,progress,content,thumb);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
            title = holder.title;
            content = holder.content;
            progress = holder.progress;
            thumb = holder.thumb;
        }

        Object mission = getItem(position);
        Animation animation = null;
        if(mission instanceof DownloadRecord){
            animation = DownloadRecord.getAnimation((DownloadRecord)mission);
            progress.setVisibility(View.INVISIBLE);
        }else if(mission instanceof M3U8Mission){
            Mission m = (M3U8Mission)mission;
            Object extra =m.getExtraInformation(m.getUri());
            animation = (Animation)extra;
            if(m.isDone()){
                progress.setVisibility(View.INVISIBLE);
            }else{
                progress.setVisibility(View.VISIBLE);
                progress.setText(m.getReadablePercentage());
            }
        }
        holder.animation = animation;
        Picasso.with(mContext).load(animation.HomePic)
                .placeholder(R.drawable.placeholder_thumb)
                .error(R.drawable.placeholder_fail).into(thumb);
        title.setText(animation.Name);
        content.setText(animation.Brief);
        convertView.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        ViewHolder holder = (ViewHolder)v.getTag();
        Animation animation = holder.animation;
        Intent intent = new Intent(mContext, PlayActivity.class);
        intent.putExtra("Animation",animation);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private static class ViewHolder{
        public Animation animation;
        public TextView title,progress,content;
        public ImageView thumb;

        private ViewHolder(TextView title, TextView progress, TextView content, ImageView thumb) {
            this.title = title;
            this.progress = progress;
            this.content = content;
            this.thumb = thumb;
        }
    }

    @Override
    public void onSuccess(M3U8Mission mission) {
        super.onSuccess(mission);
    }
}
