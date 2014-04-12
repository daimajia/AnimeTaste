package com.zhan_dui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
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
public class DownloadAdapter extends MissionListenerForAdapter{

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<DownloadRecord> mCompletedMissions;
    private ArrayList<DownloadRecord> mFailureMissions;

    public DownloadAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCompletedMissions = new ArrayList<DownloadRecord>();
        mFailureMissions = new ArrayList<DownloadRecord>();
        updateList();
    }

    @Override
    public int getCount() {
        return mCompletedMissions.size() + onGoingMissions.size() + mFailureMissions.size();
    }

    @Override
    public Object getItem(int position) {
        if(position < onGoingMissions.size()){
            return onGoingMissions.get(position);
        }else if(position < onGoingMissions.size() + mFailureMissions.size()){
            return mFailureMissions.get(position - onGoingMissions.size());
        }else{
            return mCompletedMissions.get(position - onGoingMissions.size() - mFailureMissions.size());
        }
    }

    @Override
    public void onFinish(M3U8Mission mission) {
        super.onFinish(mission);
    }

    public void updateList(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                mCompletedMissions.clear();
                mCompletedMissions.addAll(DownloadRecord.getAllDownloaded());
                mFailureMissions.clear();
                mFailureMissions.addAll(DownloadRecord.getAllFailures());
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
            DownloadRecord r = (DownloadRecord)mission;
            animation = DownloadRecord.getAnimation((DownloadRecord)mission);
            Log.e("TAG,",r.Status + " ");//此处是NULL
            if(r.Status == DownloadRecord.STATUS.SUCCESS){
                progress.setVisibility(View.INVISIBLE);
            }else{
                Log.e("TAG",r.Name +" " + "Not finished");
            }
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
        return convertView;
    }

    public static class ViewHolder{
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
