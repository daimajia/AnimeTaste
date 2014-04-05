package com.zhan_dui.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.alfred.defaults.MissionListenerForAdapter;
import com.daimajia.alfred.missions.M3U8Mission;
import com.daimajia.alfred.missions.Mission;
import com.squareup.picasso.Picasso;
import com.zhan_dui.animetaste.R;
import com.zhan_dui.modal.Animation;

import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by daimajia on 14-3-25.
 */
public class DownloadAdapter extends MissionListenerForAdapter{

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private long mPreviousTime;


    public DownloadAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPreviousTime = System.currentTimeMillis();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CircleImageView thumb;
        TextView title,content,progress;
        ViewHolder holder;
        Mission mission = getMission(position);
        Object extra = mission.getExtraInformation(mission.getUri());
        Animation animation = (Animation)extra;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.download_item,null);
            thumb = (CircleImageView)convertView.findViewById(R.id.thumbImage);
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

        Picasso.with(mContext).load(animation.HomePic)
                .placeholder(R.drawable.placeholder_thumb)
                .error(R.drawable.placeholder_fail).into(thumb);
        progress.setText(mission.getReadablePercentage());
        title.setText(animation.Name);
        content.setText(animation.Brief);
        thumb.setAlpha(mission.getPercentage());
        return convertView;
    }

    private static class ViewHolder{
        public TextView title,progress,content;
        public CircleImageView thumb;

        private ViewHolder(TextView title, TextView progress, TextView content, CircleImageView thumb) {
            this.title = title;
            this.progress = progress;
            this.content = content;
            this.thumb = thumb;
        }
    }

    @Override
    public void onPercentageChange(M3U8Mission mission) {
        long currentTime = System.currentTimeMillis();
        long span = TimeUnit.MILLISECONDS.toSeconds(currentTime - mPreviousTime);
        if(span > 1){
            mPreviousTime = currentTime;
            handler.sendEmptyMessage(0);
        }
        super.onPercentageChange(mission);
    }

    @Override
    public void onSuccess(M3U8Mission mission) {
        super.onSuccess(mission);

    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            notifyDataSetChanged();
        }
    };
}
