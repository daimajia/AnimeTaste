package com.zhan_dui.adapters;

import android.content.Context;
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


/**
 * Created by daimajia on 14-3-25.
 */
public class DownloadAdapter extends MissionListenerForAdapter{

    private Context mContext;
    private LayoutInflater mLayoutInflater;


    public DownloadAdapter(Context context) {
        super();
        mContext = context;
        mLayoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if(position > getCount() - 1){
            return null;
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


}
