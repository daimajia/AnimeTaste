package com.zhan_dui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.zhan_dui.animetaste.PlayActivity;
import com.zhan_dui.animetaste.R;
import com.zhan_dui.model.Animation;
import org.json.JSONArray;

import java.util.ArrayList;

public class AnimationListAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private final Typeface mRobotoTitle;
	private ArrayList<Animation> mAnimations;

	private final int mWatchedTitleColor;
	private final int mUnWatchedTitleColor;

    public void removeAllData(){
        mAnimations.clear();
        notifyDataSetChanged();
    }

	private AnimationListAdapter(Context context,
                                 ArrayList<Animation> animations) {
		mRobotoTitle = Typeface.createFromAsset(context.getAssets(),
				"fonts/Roboto-Bold.ttf");
		mContext = context;
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mAnimations = animations;
		mUnWatchedTitleColor = mContext.getResources().getColor(
				R.color.title_unwatched);
		mWatchedTitleColor = mContext.getResources().getColor(
				R.color.title_watched);
	}

    public static AnimationListAdapter build(Context context,ArrayList<Animation> animations){
        return new AnimationListAdapter(context,animations);
    }

    public void addAnimationsFromJsonArray(final JSONArray animationsJsonArray){
        new AddNewAnimationTask(animationsJsonArray).execute();
    }

    private class AddNewAnimationTask extends AsyncTask<Void,Void,Void>{

        private JSONArray animationsJsonArray;
        private ArrayList<Animation> animationsArrayList;
        private ArrayList<Animation> newAnimations;

        public AddNewAnimationTask(JSONArray animations){
            animationsJsonArray = animations;
        }

        public AddNewAnimationTask(ArrayList<Animation> animations){
            animationsArrayList = animations;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(animationsJsonArray!=null){
                newAnimations = Animation.build(animationsJsonArray);
            }else if(animationsArrayList != null){
                newAnimations = animationsArrayList;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAnimations.addAll(newAnimations);
            notifyDataSetChanged();
        }
    }

	@Override
	public int getCount() {
		return mAnimations.size();
	}

	@Override
	public Object getItem(int position) {
		return mAnimations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView titleTextView;
		TextView contentTextView;
		ImageView thumbImageView;
		ViewHolder holder;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.video_item, parent,
					false);
			titleTextView = (TextView) convertView.findViewById(R.id.title);
			contentTextView = (TextView) convertView.findViewById(R.id.content);
			thumbImageView = (ImageView) convertView.findViewById(R.id.thumb);
			titleTextView.setTypeface(mRobotoTitle);
			holder = new ViewHolder(titleTextView, contentTextView,
					thumbImageView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			titleTextView = holder.titleText;
			contentTextView = holder.contentText;
			thumbImageView = holder.thumbImageView;
		}
		Animation animation = (Animation) getItem(position);
		Picasso.with(mContext).load(animation.HomePic)
				.placeholder(R.drawable.placeholder_thumb)
				.error(R.drawable.placeholder_fail).into(thumbImageView);
		titleTextView.setText(animation.Name);
		contentTextView.setText(animation.Brief);
		convertView.setOnClickListener(new AnimationItemOnClickListener(animation));
		convertView.setOnLongClickListener(new OnLongClickListener() {
			// 保证长按事件传递
			@Override
			public boolean onLongClick(View v) {
				return false;
			}
		});
        titleTextView.setTextColor(animation.isWatched()?mWatchedTitleColor:mUnWatchedTitleColor);
		return convertView;
	}

    /**
     * Animation item Click listener
     */
    private class AnimationItemOnClickListener implements View.OnClickListener{
        private Animation mAnimation;

        public AnimationItemOnClickListener(Animation animation){
            this.mAnimation = animation;
        }

        @Override
        public void onClick(View view) {
            mAnimation.setWatched(true);
            notifyDataSetChanged();
            Intent intent = new Intent(mContext, PlayActivity.class);
            intent.putExtra("Animation",mAnimation);
            mContext.startActivity(intent);
        }
    }

	private static class ViewHolder {
		public TextView titleText;
		public TextView contentText;
		public ImageView thumbImageView;

		public ViewHolder(TextView title, TextView content, ImageView image) {
			titleText = title;
			contentText = content;
			thumbImageView = image;
		}
	}
}
