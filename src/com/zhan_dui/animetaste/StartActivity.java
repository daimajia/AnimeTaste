package com.zhan_dui.animetaste;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;
import com.zhan_dui.adapters.ShowGalleryPagerAdapter;
import com.zhan_dui.adapters.VideoListAdapter;
import com.zhan_dui.data.VideoDB;
import com.zhan_dui.modal.DataFetcher;

public class StartActivity extends ActionBarActivity implements
		OnScrollListener {

	private ListView mVideoList;

	private VideoListAdapter mVideoAdapter;
	private Context mContext;
	private int mCurrentPage = 1;
	private Boolean mUpdating = false;

	private ViewPager mShowPager;
	private PageIndicator mShowIndicator;
	private ShowGalleryPagerAdapter mShowAdapter;

	private LayoutInflater mLayoutInflater;
	private View mLoadView;
	private VideoDB mVideoDB;

	private int mDefaultPrepareCount = 15;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mVideoDB = new VideoDB(mContext, VideoDB.TABLE_VIDEO_NAME, null,
				VideoDB.VERSION);
		setContentView(R.layout.activity_start);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		mVideoList = (ListView) findViewById(R.id.videoList);

		mLayoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLoadView = mLayoutInflater.inflate(R.layout.load_item, null);
		mVideoList.setOnScrollListener(this);

		View headerView = mLayoutInflater.inflate(R.layout.gallery_item, null,
				false);
		mVideoList.addHeaderView(headerView);
		mShowPager = (ViewPager) headerView.findViewById(R.id.pager);
		mShowPager.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				PointF downP = new PointF();
				PointF curP = new PointF();
				int act = event.getAction();
				if (act == MotionEvent.ACTION_DOWN
						|| act == MotionEvent.ACTION_MOVE
						|| act == MotionEvent.ACTION_UP) {
					((ViewGroup) v).requestDisallowInterceptTouchEvent(true);
					if (downP.x == curP.x && downP.y == curP.y) {
						return false;
					}
				}
				return false;
			}
		});
		mShowIndicator = (UnderlinePageIndicator) headerView
				.findViewById(R.id.indicator);

		if (getIntent().hasExtra("LoadData")) {
			init(getIntent().getStringExtra("LoadData"));
		} else {
			init();
		}
	}

	public void init() {
		Cursor cursor = mVideoDB.getVideos(mDefaultPrepareCount);
		mShowAdapter = new ShowGalleryPagerAdapter(getSupportFragmentManager(),
				cursor, 4);
		mShowPager.setAdapter(mShowAdapter);
		mVideoAdapter = VideoListAdapter.build(mContext, cursor);
		mVideoList.setAdapter(mVideoAdapter);
		mShowIndicator.setViewPager(mShowPager);
	}

	public void init(String data) {
		try {
			JSONArray videoList = new JSONArray(data);
			if (videoList != null) {
				new AddToDBThread(videoList, true).start();
			}
			mShowAdapter = new ShowGalleryPagerAdapter(
					getSupportFragmentManager(), videoList, 4);
			mShowPager.setAdapter(mShowAdapter);
			mVideoAdapter = VideoListAdapter.build(mContext, videoList);
			mVideoList.setAdapter(mVideoAdapter);
			mShowIndicator.setViewPager(mShowPager);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (mUpdating == false && totalItemCount != 0
				&& view.getLastVisiblePosition() == totalItemCount - 1) {
			DataFetcher.instance().getList(mCurrentPage++,
					new LoadMoreJSONListener());
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	private class LoadMoreJSONListener extends JsonHttpResponseHandler {
		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			super.onSuccess(statusCode, response);
			if (statusCode == 200 && response.has("list")) {
				try {
					if (mCurrentPage < 3) {
						new AddToDBThread(response.getJSONArray("list"), false)
								.start();
					}
					mVideoAdapter.addVideosFromJsonArray(response
							.getJSONArray("list"));
					mVideoAdapter.notifyDataSetChanged();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onFailure(Throwable error, String content) {
			super.onFailure(error, content);
		}

		@Override
		public void onStart() {
			super.onStart();
			mUpdating = true;
		}

		@Override
		public void onFinish() {
			super.onFinish();
			mUpdating = false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_setting) {
			Intent intent = new Intent(mContext, SettingActivity.class);
			startActivity(intent);
			return true;
		}
		if(item.getItemId() == R.id.action_fav){
			Intent intent = new Intent(mContext,FavActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(mContext);
	}

	public class AddToDBThread extends Thread {
		private JSONArray mVideos;
		private Boolean mReoveAllWithoutFav;

		public AddToDBThread(JSONArray videos, boolean removeAllWithoutFav) {
			mVideos = videos;
			mReoveAllWithoutFav = removeAllWithoutFav;
		}

		@Override
		public void run() {
			super.run();
			if (mReoveAllWithoutFav) {
				mVideoDB.removeAllVideosWithoutFav();
			}
			mVideoDB.insertVideos(mVideos);
		}
	}

}
