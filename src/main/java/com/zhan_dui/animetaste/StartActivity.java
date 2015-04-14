package com.zhan_dui.animetaste;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;
import com.zhan_dui.adapters.AnimationListAdapter;
import com.zhan_dui.adapters.RecommendAdapter;
import com.zhan_dui.data.ApiConnector;
import com.zhan_dui.model.Advertise;
import com.zhan_dui.model.Animation;
import com.zhan_dui.model.Category;
import com.zhan_dui.utils.ViewUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends ActionBarActivity implements
        OnScrollListener, AdapterView.OnItemClickListener, OnTouchListener {

    private ListView mVideoList;
    private ListView mDrawerList;
    private ListView mCategoryList;
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
    private View mFooterView;
    private TextView mLoadText;
    private ProgressBar mLoadProgress;

    private ApiConnector.RequestType mPreviousType = ApiConnector.RequestType.ALL;
    private ApiConnector.RequestType mType = ApiConnector.RequestType.ALL;

    private final int RandomCount = 10;
    private final int CategoryCount = 10;
    private int mPreviousCategoryId;
    private int mCategoryId;

    private boolean mIsEnd;

    private ArrayList<Advertise> mAdvertises;

    private SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        mVideoList = (ListView) findViewById(R.id.videoList);
        mDrawerList = (ListView) findViewById(R.id.function_list);
        mDrawer = (LinearLayout) findViewById(R.id.drawer);
        mLayoutInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCategoryList = (ListView) findViewById(R.id.category_list);

        mFooterView = mLayoutInflater.inflate(R.layout.load_item, null);
        mLoadProgress = (ProgressBar) mFooterView.findViewById(R.id.loading);
        mLoadText = (TextView) mFooterView.findViewById(R.id.load_text);
        mVideoList.addFooterView(mFooterView);

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
            Toast.makeText(mContext, R.string.init_failed, Toast.LENGTH_SHORT).show();
            finish();
        }
        mDrawerAapter = new SimpleAdapter(this, getDrawerItems(), R.layout.drawer_item, new String[]{"img", "title"}, new int[]{R.id.item_icon, R.id.item_name});
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_action_navigation_menu, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mPreviousType != mType || mPreviousCategoryId != mCategoryId) {
                    mCurrentPage = 1;
                    mIsEnd = false;
                    mVideoAdapter.removeAllData();
                    mFooterView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                    mFooterView.findViewById(R.id.load_text).setVisibility(View.INVISIBLE);
                    triggerApiConnector();
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mPreviousType = mType;
                mPreviousCategoryId = mCategoryId;
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setAdapter(mDrawerAapter);
        mDrawerList.setOnItemClickListener(this);
        ViewUtils.setListViewHeightBasedOnChildren(mDrawerList);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_USE_LOGO|ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setLogo(R.drawable.rsz_ab_icon);
        rateForUsOrCheckUpdate();
        showWhatsNew();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    public void rateForUsOrCheckUpdate() {
        if (mSharedPreferences.getInt("playcount", 0) > 10
                && !mSharedPreferences.getBoolean("sharedApp", false)) {
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

        } else {
            UmengUpdateAgent.update(this);
        }
    }

    public void init(Intent intent) {
        ArrayList<Animation> Animations = intent.getParcelableArrayListExtra("Animations");
        ArrayList<Category> Categories = intent.getParcelableArrayListExtra("Categories");
        mAdvertises = intent.getParcelableArrayListExtra("Advertises");
        ArrayList<Animation> Recommends = intent.getParcelableArrayListExtra("Recommends");
        mRecommendAdapter = new RecommendAdapter(mContext, mAdvertises, Recommends);
        mRecommendPager.setAdapter(mRecommendAdapter);
        mRecommendIndicator.setViewPager(mRecommendPager);
        mVideoAdapter = AnimationListAdapter.build(mContext, Animations);
        mVideoList.setAdapter(mVideoAdapter);
        CategoryListAdapter categoryListAdapter = new CategoryListAdapter(mContext, Categories);
        mCategoryList.setAdapter(categoryListAdapter);
        ViewUtils.setListViewHeightBasedOnChildren(mCategoryList);

    }

    private List<Map<String, Object>> getDrawerItems() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("img", R.drawable.drawer_light);
        map.put("title", getString(R.string.guess));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.drawer_all);
        map.put("title", getString(R.string.latest));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.drawer_heart);
        map.put("title", getString(R.string.my_fav));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.drawer_download);
        map.put("title", getString(R.string.my_download));
        list.add(map);
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    public void triggerApiConnector() {
        if (mCurrentPage == 1) {
            switch (mType) {
                case ALL:
                    ApiConnector.instance().getALLRecommend(4, new LoadRecommendListener());
                    break;
                case CATEGORY:
                    ApiConnector.instance().getCategoryRecommend(mCategoryId, 4, new LoadRecommendListener());
                    break;
                default:
            }
        }

        switch (mType) {
            case ALL:
                ApiConnector.instance().getList(mCurrentPage++, new LoadMoreJSONListener());
                break;
            case RANDOM:
                ApiConnector.instance().getRandom(RandomCount, new LoadMoreJSONListener());
                break;
            case CATEGORY:
                ApiConnector.instance().getCategory(mCategoryId, mCurrentPage++, CategoryCount, new LoadMoreJSONListener());
            default:
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (mUpdating) {
            return;
        }
        if (mUpdating == false && totalItemCount != 0
                && firstVisibleItem + visibleItemCount >= totalItemCount && !mIsEnd) {
            mUpdating = true;
            triggerApiConnector();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String title = ((TextView) view.findViewById(R.id.item_name)).getText().toString();
        if (title.equals(getString(R.string.guess))) {
            mType = ApiConnector.RequestType.RANDOM;
        } else if (title.equals(getString(R.string.my_fav))) {
            Intent intent = new Intent(mContext, FavoriteActivity.class);
            startActivity(intent);
        } else if (title.equals(getString(R.string.latest))) {
            mType = ApiConnector.RequestType.ALL;
        } else if (title.equals(getString(R.string.my_download))) {
            Intent intent = new Intent(mContext, DownloadActivity.class);
            startActivity(intent);
        }
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    private class LoadRecommendListener extends JsonHttpResponseHandler {
        @Override
        public void onSuccess(final JSONObject response) {
            super.onSuccess(response);
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        JSONArray animes = response.getJSONObject("data").getJSONObject("list").getJSONArray("anime");
                        final ArrayList<Animation> Recommends = Animation.build(animes);
                        if (mType == ApiConnector.RequestType.ALL)
                            mRecommendAdapter = new RecommendAdapter(mContext, mAdvertises, Recommends);
                        else
                            mRecommendAdapter = new RecommendAdapter(mContext, null, Recommends);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRecommendPager.setAdapter(mRecommendAdapter);
                                mRecommendIndicator.setViewPager(mRecommendPager);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, R.string.load_recommends_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();
        }

        @Override
        public void onFailure(Throwable throwable) {
            super.onFailure(throwable);
            Toast.makeText(mContext, R.string.get_data_error, Toast.LENGTH_SHORT).show();
        }
    }

    private class LoadMoreJSONListener extends JsonHttpResponseHandler {

        public LoadMoreJSONListener() {
            mUpdating = true;
        }

        @Override
        public void onSuccess(int statusCode, JSONObject response) {
            super.onSuccess(statusCode, response);
            if (statusCode == 200 && response.has("data")) {
                try {
                    if (response.getJSONObject("data").getJSONObject("list").getJSONArray("anime").isNull(1)) {
                        mIsEnd = true;
                        mLoadProgress.setVisibility(View.INVISIBLE);
                        mLoadText.setText(R.string.end);
                        mLoadText.setVisibility(View.VISIBLE);
                    } else {
                        mVideoAdapter.addAnimationsFromJsonArray(response.getJSONObject("data").getJSONObject("list").getJSONArray("anime"));
                        mLoadProgress.setVisibility(View.VISIBLE);
                        mLoadText.setVisibility(View.INVISIBLE);
                        mVideoList.setOnScrollListener(StartActivity.this);
                        mFooterView.setOnClickListener(null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(Throwable error) {
            super.onFailure(error);
            mCurrentPage--;
            mLoadText.setText(R.string.touch_to_retry);
            mLoadText.setVisibility(View.VISIBLE);
            mLoadProgress.setVisibility(View.INVISIBLE);
            mVideoList.setOnScrollListener(null);
            mFooterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    triggerApiConnector();
                }
            });
        }

        @Override
        public void onStart() {
            super.onStart();
            mLoadText.setVisibility(View.INVISIBLE);
            mLoadProgress.setVisibility(View.VISIBLE);
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
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerLayout.getChildAt(1)))
                mDrawerLayout.closeDrawers();
            else {
                mDrawerLayout.openDrawer(mDrawerLayout.getChildAt(1));
            }
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

    protected class CategoryListAdapter extends BaseAdapter {

        private ArrayList<Category> mCategories;
        private LayoutInflater mInflater;

        public CategoryListAdapter(Context context, ArrayList<Category> categories) {
            mCategories = categories;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mCategories.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            convertView = mInflater.inflate(R.layout.category_item, null);
            TextView name = (TextView) convertView.findViewById(R.id.category_name);
            final int id = mCategories.get(i).cid;
            name.setText(mCategories.get(i).Name);
            convertView.setTag(mCategories.get(i));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mType = ApiConnector.RequestType.CATEGORY;
                    mCategoryId = id;
                    mDrawerLayout.closeDrawers();
                }
            });
            return convertView;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    public void showWhatsNew() {
        boolean showed = mSharedPreferences.getBoolean("showed15", false);
        if (!showed) {
            Toast.makeText(mContext, R.string.intro_drawer, Toast.LENGTH_SHORT).show();
            mDrawerLayout.openDrawer(mDrawerLayout.getChildAt(1));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.closeDrawers();
                        }
                    });
                    mSharedPreferences.edit().putBoolean("showed15", true).commit();
                }
            }, 3000);
        }
    }

}
