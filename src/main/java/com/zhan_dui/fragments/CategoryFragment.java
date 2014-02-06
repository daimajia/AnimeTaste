package com.zhan_dui.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhan_dui.adapters.AnimationListAdapter;
import com.zhan_dui.animetaste.R;
import com.zhan_dui.data.ApiConnector;
import com.zhan_dui.modal.Animation;
import com.zhan_dui.modal.Category;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by daimajia on 14-2-4.
 */
public class CategoryFragment extends ListFragment implements AbsListView.OnScrollListener{

    private static final int FETCH_COUNT = 10;

    private enum STATUS{
        IDLE,GETTING,END
    }

    private Category mCategory;
    private AnimationListAdapter mAnimationAdapter;

    private LayoutInflater mInflater;
    private View mFooterView;

    private STATUS mStatus = STATUS.IDLE;

    private int mCurrentPage = 1;

    public static CategoryFragment newInstance(Category category){
        Bundle bundle = new Bundle();
        bundle.putParcelable("Category",category);
        CategoryFragment categoryFragment = new CategoryFragment();
        categoryFragment.setArguments(bundle);
        return categoryFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CurrentPage",mCurrentPage);
        outState.putSerializable("STATUS",mStatus);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategory = getArguments().getParcelable("Category");
        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey("CurrentPage")){
                mCurrentPage = savedInstanceState.getInt("CurrentPage");
            }
            if(savedInstanceState.containsKey("STATUS")){
                mStatus = (STATUS)savedInstanceState.getSerializable("STATUS");
            }
        }
        if(getArguments().containsKey("Animations")){
            ArrayList<Animation> animations = getArguments().getParcelableArrayList("Animations");
            mAnimationAdapter = AnimationListAdapter.build(getActivity(),animations);
        }else{
            mAnimationAdapter = AnimationListAdapter.build(getActivity(),null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        return inflater.inflate(R.layout.fragment_category,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(mAnimationAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFooterView = mInflater.inflate(R.layout.footer_view,null);
        getListView().addFooterView(mFooterView);
        getListView().setAdapter(mAnimationAdapter);
        getListView().setOnScrollListener(this);
        if(mStatus != STATUS.END){
            mFooterView.findViewById(R.id.empty_text).setVisibility(View.INVISIBLE);
            mFooterView.findViewById(R.id.empty_progress).setVisibility(View.VISIBLE);
        }else{
            mFooterView.findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
            mFooterView.findViewById(R.id.empty_progress).setVisibility(View.INVISIBLE);
        }
    }

    protected class DataReachListener extends JsonHttpResponseHandler{

        @Override
        public void onStart() {
            super.onStart();
            mStatus = STATUS.GETTING;
        }

        @Override
        public void onSuccess(final JSONObject response) {
            super.onSuccess(response);
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    try{
                        JSONArray animes = response.getJSONObject("data").getJSONObject("list").getJSONArray("anime");
                        final ArrayList<Animation> animations = Animation.build(animes);
                        if(animations.size() < FETCH_COUNT){
                            mStatus = STATUS.END;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(mStatus == STATUS.END){
                                    mFooterView.findViewById(R.id.empty_progress).setVisibility(View.INVISIBLE);
                                    mFooterView.findViewById(R.id.empty_text).setVisibility(View.VISIBLE);
                                }
                                mAnimationAdapter.addAnimationsFromArrayList(animations);
                                mAnimationAdapter.notifyDataSetChanged();
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        @Override
        public void onFinish() {
            super.onFinish();
            mStatus = STATUS.IDLE;
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mStatus == STATUS.GETTING || mStatus == STATUS.END){
            return;
        }
        if(view.getLastVisiblePosition() == totalItemCount - 1){
            mStatus = STATUS.GETTING;
            ApiConnector.instance().getCategory(mCategory.cid,mCurrentPage++,FETCH_COUNT,new DataReachListener());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        getArguments().putParcelableArrayList("Animations",mAnimationAdapter.getAnimationData());
    }

}
