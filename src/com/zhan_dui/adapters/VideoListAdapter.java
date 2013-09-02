package com.zhan_dui.adapters;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.database.Cursor;
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
import com.zhan_dui.animetaste.R;
import com.zhan_dui.data.VideoDB;
import com.zhan_dui.listener.VideoListItemListener;
import com.zhan_dui.modal.VideoDataFormat;

public class VideoListAdapter extends BaseAdapter {
	private Context mContext;
	private VideoDB mVideoDB;
	private LayoutInflater mLayoutInflater;
	private final Typeface mRobotoTitle;
	private ArrayList<VideoDataFormat> mVideoList;

	private final int mWatchedTitleColor;
	private final int mUnWatchedTitleColor;

	private VideoListAdapter(Context context,
			ArrayList<VideoDataFormat> videoList, Boolean checkIsWatched) {
		mRobotoTitle = Typeface.createFromAsset(context.getAssets(),
				"fonts/Roboto-Bold.ttf");
		mContext = context;
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mVideoList = videoList;
		mVideoDB = new VideoDB(mContext, VideoDB.NAME, null, VideoDB.VERSION);
		mUnWatchedTitleColor = mContext.getResources().getColor(
				R.color.title_unwatched);
		mWatchedTitleColor = mContext.getResources().getColor(
				R.color.title_watched);
		if (checkIsWatched)
			new CheckIsWatchedTask(0, mVideoList.size()).execute();
	}

	public void setFav(int vid, boolean fav) {
		for (int i = 0; i < mVideoList.size(); i++) {
			if (mVideoList.get(i).Id == vid) {
				mVideoList.get(i).setFav(fav);
				break;
			}
		}
		notifyDataSetChanged();
	}

	public void setWatched(VideoDataFormat video) {
		for (int i = 0; i < mVideoList.size(); i++) {
			if (mVideoList.get(i).Id == video.Id) {
				mVideoList.get(i).setWatched(true);
				break;
			}
		}
		notifyDataSetChanged();
	}

	public static VideoListAdapter build(Context context, JSONArray data,
			Boolean checkIsWatched) throws JSONException {
		ArrayList<VideoDataFormat> videos = new ArrayList<VideoDataFormat>();
		for (int i = 0; i < data.length(); i++) {
			videos.add(VideoDataFormat.build(data.getJSONObject(i)));
		}
		return new VideoListAdapter(context, videos, checkIsWatched);
	}

	public static VideoListAdapter build(Context context, Cursor cursor,
			Boolean checkIsWatched) {
		ArrayList<VideoDataFormat> videos = new ArrayList<VideoDataFormat>();
		while (cursor.moveToNext()) {
			videos.add(VideoDataFormat.build(cursor));
		}
		return new VideoListAdapter(context, videos, checkIsWatched);
	}

	public void addVideosFromJsonArray(JSONArray videos) throws JSONException {
		int start = mVideoList.size();
		for (int i = 0; i < videos.length(); i++) {
			mVideoList.add(VideoDataFormat.build(videos.getJSONObject(i)));
		}
		int end = mVideoList.size();
		new CheckIsWatchedTask(start, end).execute();
	}

	@Override
	public int getCount() {
		return mVideoList.size();
	}

	@Override
	public Object getItem(int position) {
		return mVideoList.get(position);
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
			titleTextView.setTypeface(mRobotoTitle);
			contentTextView = (TextView) convertView.findViewById(R.id.content);
			thumbImageView = (ImageView) convertView.findViewById(R.id.thumb);
			holder = new ViewHolder(titleTextView, contentTextView,
					thumbImageView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			titleTextView = holder.titleText;
			contentTextView = holder.contentText;
			thumbImageView = holder.thumbImageView;
		}
		VideoDataFormat video = (VideoDataFormat) getItem(position);
		Picasso.with(mContext).load(video.HomePic)
				.placeholder(R.drawable.placeholder_thumb)
				.error(R.drawable.placeholder_fail).into(thumbImageView);
		titleTextView.setText(video.Name);
		contentTextView.setText(video.Brief);
		convertView.setOnClickListener(new VideoListItemListener(mContext,
				this, video));
		convertView.setOnLongClickListener(new OnLongClickListener() {
			// 保证长按事件传递
			@Override
			public boolean onLongClick(View v) {
				return false;
			}
		});
		if (video.isWatched() == true) {
			titleTextView.setTextColor(mWatchedTitleColor);
		} else {
			titleTextView.setTextColor(mUnWatchedTitleColor);
		}
		return convertView;
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

	public class CheckIsWatchedTask extends AsyncTask<Void, Void, Void> {

		private int mStart;
		private int mEnd;

		public CheckIsWatchedTask(int start, int end) {
			mStart = start;
			mEnd = end;
		}

		@Override
		protected Void doInBackground(Void... params) {
			for (int i = mStart; i < mEnd; i++) {
				VideoDataFormat currentVideo = mVideoList.get(i);
				currentVideo.setWatched(mVideoDB.isWatched(currentVideo));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			notifyDataSetChanged();
		}
	}

}
