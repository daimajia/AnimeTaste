package com.zhan_dui.adapters;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.zhan_dui.fragments.ShowFragment;
import com.zhan_dui.modal.VideoDataFormat;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ShowGalleryPagerAdapter extends FragmentPagerAdapter {

	private ArrayList<VideoDataFormat> mShow = new ArrayList<VideoDataFormat>();
	
	public ShowGalleryPagerAdapter(FragmentManager fm,
			ArrayList<VideoDataFormat> VideoShowList) {
		super(fm);
		mShow = VideoShowList;
	}
	
	public ShowGalleryPagerAdapter(FragmentManager fm, JSONArray VideoList,
			int showCount) {
		super(fm);
		int count = VideoList.length();
		if (showCount > count) {
			showCount = count;
		}
		for (int i = 0; i < showCount; i++) {
			try {
				mShow.add(VideoDataFormat.build(VideoList.getJSONObject(i)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public ShowGalleryPagerAdapter(FragmentManager fm, Cursor videolist,
			int showCount) {
		super(fm);
		if (showCount > videolist.getCount()) {
			showCount = videolist.getCount();
		}
		int counter = 0;
		while (videolist.moveToNext()) {
			mShow.add(VideoDataFormat.build(videolist));
			counter++;
			if (counter >= showCount) {
				break;
			}
		}

	}

	private ShowGalleryPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return ShowFragment.newInstance(mShow.get(position));
	}

	@Override
	public int getCount() {
		return mShow.size();
	}

}
