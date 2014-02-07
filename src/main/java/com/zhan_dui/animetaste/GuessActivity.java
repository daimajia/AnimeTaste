package com.zhan_dui.animetaste;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhan_dui.adapters.AnimationListAdapter;
import com.zhan_dui.data.ApiConnector;
import com.zhan_dui.modal.Animation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by daimajia on 14-2-7.
 */
public class GuessActivity extends ActionBarActivity implements AbsListView.OnScrollListener{

    private Context mContext;
    private LayoutInflater mInflater;

    private ListView mListView;
    private AnimationListAdapter mAnimationAdapter;
    
    private View mFooterView;
    private boolean mGetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_category);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.guess));
        mContext = this;
        mGetting = false;
        mInflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        mListView = (ListView)findViewById(android.R.id.list);
        if(savedInstanceState != null && savedInstanceState.containsKey("Animations")){
            ArrayList<Animation> animations = savedInstanceState.getParcelableArrayList("Animations");
            mAnimationAdapter = AnimationListAdapter.build(mContext,animations);
        }else{
            mAnimationAdapter = AnimationListAdapter.build(mContext,null);
        }
        mFooterView = mInflater.inflate(R.layout.footer_view,null);
        mFooterView.findViewById(R.id.empty_text).setVisibility(View.INVISIBLE);
        mFooterView.findViewById(R.id.empty_progress).setVisibility(View.VISIBLE);
        mListView.addFooterView(mFooterView);
        mListView.setAdapter(mAnimationAdapter);
        mListView.setOnScrollListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("Animations",mAnimationAdapter.getAnimationData());
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(mGetting){
            return;
        }
        if(view.getLastVisiblePosition() == totalItemCount - 1){
            mGetting = true;
            ApiConnector.instance().getRandom(20,new JsonHttpResponseHandler(){
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAnimationAdapter.addAnimationsFromArrayList(animations);
                                        mAnimationAdapter.notifyDataSetChanged();
                                    }
                                });
                            }catch (Exception e){
                            }
                        }
                    }.start();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    mGetting = false;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
