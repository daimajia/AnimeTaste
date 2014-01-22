package com.zhan_dui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class RecommendFragment extends Fragment {

	private ImageView mRecommendImage;

	public RecommendFragment() {
	}

    public static RecommendFragment build(Advertise advertise){
        RecommendFragment f = new RecommendFragment();
        Bundle args = new Bundle();
        args.putParcelable("Advertise",advertise);
        f.setArguments(args);
        return f;
    }

	public static RecommendFragment build(Animation animation) {
		RecommendFragment f = new RecommendFragment();
		Bundle args = new Bundle();
        args.putParcelable("Animation",animation);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        Bundle args = getArguments();
        String RecommendImageUrl = "";

        View RecommendLayout = inflater.inflate(R.layout.recommend_fragment,container,false);
        mRecommendImage = (ImageView)RecommendLayout.findViewById(R.id.recommend_image);

        if(args.containsKey("Advertise")){
            Advertise advertise = args.getParcelable("Advertise");
            RecommendImageUrl = advertise.DetailPic;
            mRecommendImage.setOnClickListener(new AdvertiseItemOnClickListener(advertise));
        }else if(args.containsKey("Animation")){
            Animation animation = args.getParcelable("Animation");
            RecommendImageUrl = animation.DetailPic;
            mRecommendImage.setOnClickListener(new AnimationItemOnClickListener(animation));
        }
        Picasso.with(getActivity().getApplicationContext()).load(RecommendImageUrl).placeholder(R.drawable.big_bg).error(R.drawable.big_bg).into(mRecommendImage);
		return RecommendLayout;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
            Intent intent = new Intent(getActivity(), PlayActivity.class);
            intent.putExtra("Animation",mAnimation);
            startActivity(intent);
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
            Intent intent = new Intent(getActivity(), AdvertiseActivity.class);
            intent.putExtra("Advertise",mAdvertise);
            startActivity(intent);
        }
    }
}
