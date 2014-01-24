package com.zhan_dui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.zhan_dui.animetaste.AdvertiseActivity;
import com.zhan_dui.animetaste.PlayActivity;
import com.zhan_dui.animetaste.R;
import com.zhan_dui.modal.Advertise;
import com.zhan_dui.modal.Animation;

import java.util.ArrayList;

public class RecommendAdapter extends PagerAdapter {

    private ArrayList<Advertise> mAdvertises = new ArrayList<Advertise>();
	private ArrayList<Animation> mRecommends = new ArrayList<Animation>();
    private int mAdvertiseCount;
    private int mRecommendCount;
    private int mTotalCount;

    private Context mContext;
    private LayoutInflater mInflater;

    public RecommendAdapter(Context context,ArrayList<Advertise> Ads,ArrayList<Animation> Recommends){
        mAdvertises = Ads;
        mRecommends = Recommends;
        mAdvertiseCount = mAdvertises == null ? 0 : mAdvertises.size();
        mRecommendCount = mRecommends == null ? 0 : mRecommends.size();
        mTotalCount = mAdvertiseCount + mRecommendCount;
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View recommendView = mInflater.inflate(R.layout.recommend_view,container,false);
        ImageView recommendImage = (ImageView)recommendView.findViewById(R.id.recommend_image);
        String imageUrl;
        if(position<mAdvertiseCount){
            imageUrl = mAdvertises.get(position).DetailPic;
            recommendImage.setOnClickListener(new AdvertiseItemOnClickListener(mAdvertises.get(position)));
        }else{
            imageUrl = mRecommends.get(position - mAdvertiseCount).DetailPic;
            recommendImage.setOnClickListener(new AnimationItemOnClickListener(mRecommends.get(position - mAdvertiseCount)));
        }
        Picasso.with(mContext).load(imageUrl).placeholder(R.drawable.big_bg).error(R.drawable.big_bg).into(recommendImage);
        container.addView(recommendView);
        return  recommendView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
	public int getCount() {
		return mTotalCount;
	}

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
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
            Intent intent = new Intent(mContext, PlayActivity.class);
            intent.putExtra("Animation",mAnimation);
            mContext.startActivity(intent);
        }
    }

    /**
     * Advertise item click listener
     */
    private class AdvertiseItemOnClickListener implements  View.OnClickListener{

        private Advertise mAdvertise;

        private AdvertiseItemOnClickListener(Advertise mAdvertise) {
            this.mAdvertise = mAdvertise;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, AdvertiseActivity.class);
            intent.putExtra("Advertise",mAdvertise);
            mContext.startActivity(intent);
        }
    }
}
