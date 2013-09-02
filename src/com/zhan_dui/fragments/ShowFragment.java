package com.zhan_dui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zhan_dui.animetaste.R;
import com.zhan_dui.listener.VideoListItemListener;
import com.zhan_dui.modal.VideoDataFormat;

@SuppressLint("ValidFragment")
public class ShowFragment extends Fragment {

	private final VideoDataFormat mVideoInfo;
	private ImageView mShowImageView;

	private ShowFragment() {
		mVideoInfo = null;
	}

	private ShowFragment(VideoDataFormat VideoInfo) {
		mVideoInfo = VideoInfo;
	}

	public static ShowFragment newInstance(VideoDataFormat VideoInfo) {
		return new ShowFragment(VideoInfo);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater
				.inflate(R.layout.fragment_show, container, false);
		mShowImageView = (ImageView) layout.findViewById(R.id.show_image);
		mShowImageView.setImageResource(R.drawable.big_bg);
		Picasso.with(getActivity().getApplicationContext())
				.load(mVideoInfo.DetailPic).placeholder(R.drawable.big_bg)
				.error(R.drawable.big_bg).into(mShowImageView);
		mShowImageView.setTag(mVideoInfo);
		mShowImageView.setOnClickListener(new VideoListItemListener(
				getActivity(), mVideoInfo));
		return layout;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}
