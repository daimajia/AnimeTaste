package com.zhan_dui.animetaste;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;
import com.zhan_dui.adapters.AnimationListAdapter;
import com.zhan_dui.adapters.RecommendAdapter;
import com.zhan_dui.data.AnimeTasteDB;
import com.zhan_dui.data.ApiConnector;
import com.zhan_dui.modal.Advertise;
import com.zhan_dui.modal.Animation;
import com.zhan_dui.modal.Category;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartActivity extends ActionBarActivity implements
		OnScrollListener,AdapterView.OnItemClickListener,OnTouchListener {

	private ListView mVideoList;
    private ListView mDrawerList;
    private LinearLayout mDrawer;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private SimpleAdapter mDrawerAapter;

	private AnimationListAdapter mVideoAdapter;
	private Context mContext;
	private int mCurrentPage = 3;
	private Boolean mUpdating = false;

	private ViewPager mRecommendPager;
	private PageIndicator mRecommendIndicator;
	private RecommendAdapter mRecommendAdapter;

	private LayoutInflater mLayoutInflater;
	private AnimeTasteDB mAnimeTasteDB;

	private SharedPreferences mSharedPreferences;

	private int mDefaultPrepareCount = 15;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		mAnimeTasteDB = new AnimeTasteDB(mContext, AnimeTasteDB.NAME, null, AnimeTasteDB.VERSION);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

		mVideoList = (ListView) findViewById(R.id.videoList);
        mDrawerList = (ListView)findViewById(R.id.function_list);
        mDrawer = (LinearLayout)findViewById(R.id.drawer);
		mLayoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mVideoList.setOnScrollListener(this);
        mDrawer.setOnTouchListener(this);

		View headerView = mLayoutInflater.inflate(R.layout.gallery_item, null,
				false);
		mVideoList.addHeaderView(headerView);
		mRecommendPager = (ViewPager) headerView.findViewById(R.id.pager);
		mRecommendPager.setOnTouchListener(new OnTouchListener() {
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
		mRecommendIndicator = (UnderlinePageIndicator) headerView
				.findViewById(R.id.indicator);

		if (getIntent().hasExtra("Success")) {
			init(getIntent());
		} else {
			init();
		}
        mDrawerAapter = new SimpleAdapter(this,getDrawerItems(),R.layout.drawer_item,new String[]{"img","title"},new int[]{R.id.item_icon,R.id.item_name});
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.drawable.ic_drawer,R.string.app_name,R.string.app_name){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setAdapter(mDrawerAapter);
        mDrawerList.setOnItemClickListener(this);

		rateForUsOrCheckUpdate();
	}

	public void rateForUsOrCheckUpdate() {
		if (mSharedPreferences.getInt("playcount", 0) > 10
				&& mSharedPreferences.getBoolean("sharedApp", false) == false) {
			AlertDialog.Builder builder = new Builder(mContext);
			builder.setMessage(R.string.rate_share_message);
			builder.setTitle(R.string.rate_share_title);
			builder.setPositiveButton(R.string.rate_share_i_do,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent shareIntent = new Intent(Intent.ACTION_SEND);
							shareIntent.setType("text/plain");
							shareIntent.putExtra(
									android.content.Intent.EXTRA_SUBJECT,
									getText(R.string.share_title));
							shareIntent.putExtra(
									android.content.Intent.EXTRA_TEXT,
									getText(R.string.share_app_body));
							startActivity(Intent.createChooser(shareIntent,
									getText(R.string.share_via)));
							MobclickAgent.onEvent(mContext, "need_share");
						}
					});
			builder.setNegativeButton(R.string.rate_share_sorry, null);
			builder.show();
			mSharedPreferences.edit().putBoolean("sharedApp", true).commit();
		}else{
            UmengUpdateAgent.update(this);
        }
	}

	public void init() {
//		Cursor cursor = mAnimeTasteDB.getAnimations(mDefaultPrepareCount);
//		mRecommendAdapter = new RecommendAdapter(getSupportFragmentManager(),
//				cursor, 4);
//		mRecommendPager.setAdapter(mRecommendAdapter);
//		mVideoAdapter = AnimationListAdapter.build(mContext, cursor, true);
//		mVideoList.setAdapter(mVideoAdapter);
//		mRecommendIndicator.setViewPager(mRecommendPager);
	}

    public void init(Intent intent){
        ArrayList<Animation> Animations = intent.getParcelableArrayListExtra("Animations");
        ArrayList<Category> Categories = intent.getParcelableArrayListExtra("Categories");
        ArrayList<Advertise> Advertises = intent.getParcelableArrayListExtra("Advertises");
        ArrayList<Animation> Recommends = intent.getParcelableArrayListExtra("Recommends");
        mRecommendAdapter = new RecommendAdapter(getSupportFragmentManager(),Advertises,Recommends);
        mRecommendPager.setAdapter(mRecommendAdapter);
        mVideoAdapter = AnimationListAdapter.build(mContext, Animations);
        mVideoList.setAdapter(mVideoAdapter);
        mRecommendIndicator.setViewPager(mRecommendPager);
    }

    private List<Map<String,Object>> getDrawerItems(){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("img",R.drawable.drawer_light);
        map.put("title",getString(R.string.guess));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img",R.drawable.drawer_all);
        map.put("title",getString(R.string.all));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img",R.drawable.drawer_heart);
        map.put("title",getString(R.string.my_fav));
        list.add(map);
        return list;
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
			mUpdating = true;
			ApiConnector.instance().getList(mCurrentPage++,
					new LoadMoreJSONListener());
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String title = ((TextView)view.findViewById(R.id.item_name)).getText().toString();
        if(title.equals(getString(R.string.guess))){

        }else if(title.equals(getString(R.string.my_fav))){

        }else if(title.equals(getString(R.string.all))){
            mDrawerLayout.closeDrawers();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    private class LoadMoreJSONListener extends JsonHttpResponseHandler {

		private View mFooterView;

		public LoadMoreJSONListener() {
			mUpdating = true;
		}

		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			super.onSuccess(statusCode, response);
			if (statusCode == 200 && response.has("data")) {
				try {
					mVideoAdapter.addAnimationsFromJsonArray(response
							.getJSONObject("data").getJSONObject("list").getJSONArray("anime"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onFailure(Throwable error, String content) {
			super.onFailure(error, content);
			mCurrentPage--;
		}

		@Override
		public void onStart() {
			super.onStart();
			mFooterView = mLayoutInflater.inflate(R.layout.load_item, null);
			mVideoList.addFooterView(mFooterView);
		}

		@Override
		public void onFinish() {
			super.onFinish();
			mUpdating = false;
			mVideoList.removeFooterView(mFooterView);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_setting) {
			Intent intent = new Intent(mContext, SettingActivity.class);
			startActivity(intent);
			return true;
		}
		if (item.getItemId() == R.id.action_fav) {
			Intent intent = new Intent(mContext, FavoriteActivity.class);
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

}
