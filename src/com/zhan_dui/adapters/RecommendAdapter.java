package com.zhan_dui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.zhan_dui.fragments.RecommendFragment;
import com.zhan_dui.modal.Advertise;
import com.zhan_dui.modal.Animation;

import java.util.ArrayList;

public class RecommendAdapter extends FragmentPagerAdapter {

    private ArrayList<Advertise> mAdvertises = new ArrayList<Advertise>();
	private ArrayList<Animation> mRecommends = new ArrayList<Animation>();
    private int mAdvertiseCount;
    private int mRecommendCount;
    private int mTotalCount;
    public RecommendAdapter(FragmentManager fm,ArrayList<Advertise> Ads,ArrayList<Animation> recommends){
        super(fm);
        mAdvertises = Ads;
        mRecommends = recommends;
        mAdvertiseCount = mAdvertises.size();
        mRecommendCount = mRecommends.size();
        mTotalCount = mAdvertiseCount + mRecommendCount;
    }
//
//	public RecommendAdapter(FragmentManager fm, JSONArray VideoList,
//                            int showCount) {
//		super(fm);
//		int count = VideoList.length();
//		if (showCount > count) {
//			showCount = count;
//		}
//		for (int i = 0; i < showCount; i++) {
//			try {
//				mShow.add(Animation.build(VideoList.getJSONObject(i)));
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public RecommendAdapter(FragmentManager fm, Cursor videolist,
//                            int showCount) {
//		super(fm);
//		if (showCount > videolist.getCount()) {
//			showCount = videolist.getCount();
//		}
//		int counter = 0;
//		while (videolist.moveToNext()) {
//			mShow.add(Animation.build(videolist));
//			counter++;
//			if (counter >= showCount) {
//				break;
//			}
//		}
//
//	}

	@Override
	public Fragment getItem(int position) {
        if(position < mAdvertiseCount){
            return RecommendFragment.build(mAdvertises.get(position));
        }else{
            return RecommendFragment.build(mRecommends.get(position - mAdvertiseCount));
        }
	}

	@Override
	public int getCount() {
		return mTotalCount;
	}

}
