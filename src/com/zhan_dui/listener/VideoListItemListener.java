package com.zhan_dui.listener;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.zhan_dui.animetaste.PlayActivity;
import com.zhan_dui.modal.DataFormat;

public class VideoListItemListener implements OnClickListener {

	private DataFormat mData;
	private Context mContext;

	public VideoListItemListener(Context context, DataFormat data) {
		mData = data;
		mContext = context;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(mContext, PlayActivity.class);
		intent.putExtra("VideoInfo", mData);
		mContext.startActivity(intent);
	}

}
