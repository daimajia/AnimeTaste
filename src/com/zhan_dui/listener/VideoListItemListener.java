package com.zhan_dui.listener;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.zhan_dui.adapters.VideoListAdapter;
import com.zhan_dui.animetaste.PlayActivity;
import com.zhan_dui.data.VideoDB;
import com.zhan_dui.modal.VideoDataFormat;

public class VideoListItemListener implements OnClickListener {

	private VideoDataFormat mData;
	private Context mContext;
	private VideoDB mVideoDB;
	private VideoListAdapter mAdapter;

	public VideoListItemListener(Context context, VideoDataFormat data) {
		mData = data;
		mContext = context;
		mVideoDB = new VideoDB(mContext, VideoDB.NAME, null, VideoDB.VERSION);
		mAdapter = null;
	}

	public VideoListItemListener(Context context, VideoListAdapter adapter,
			VideoDataFormat data) {
		this(context, data);
		mAdapter = adapter;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(mContext, PlayActivity.class);
		intent.putExtra("VideoInfo", mData);
		mContext.startActivity(intent);
		mVideoDB.insertWatched(mData);
		if (mAdapter != null) {
			if (mData.isWatched() == false)
				mAdapter.setWatched(mData);
		}
	}

}
