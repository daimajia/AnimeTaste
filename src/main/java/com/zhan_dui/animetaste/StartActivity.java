package com.zhan_dui.animetaste;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.astuetz.PagerSlidingTabStrip;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.zhan_dui.adapters.CategoryAdapter;
import com.zhan_dui.modal.Category;
import com.zhan_dui.utils.ViewUtils;

import java.util.*;

public class StartActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private ListView mDrawerList;
    private LinearLayout mDrawer;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private SimpleAdapter mDrawerAapter;

	private Context mContext;

	private LayoutInflater mLayoutInflater;

    private CategoryAdapter mCategoryAdapter;
    private PagerSlidingTabStrip mTabs;
    private ViewPager mPager;

	private SharedPreferences mSharedPreferences;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerList = (ListView)findViewById(R.id.function_list);
        mDrawer = (LinearLayout)findViewById(R.id.drawer);
		mLayoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mTabs = (PagerSlidingTabStrip)findViewById(R.id.tabs);
        mPager = (ViewPager)findViewById(R.id.pager);

		if (getIntent().hasExtra("Success")) {
			init(getIntent());
		} else{
            Toast.makeText(mContext,R.string.init_failed,Toast.LENGTH_SHORT).show();
            finish();
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
        ViewUtils.setListViewHeightBasedOnChildren(mDrawerList);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public void init(Intent intent){
        ArrayList<Category> Categories = intent.getParcelableArrayListExtra("Categories");
        mCategoryAdapter = new CategoryAdapter(Categories,getSupportFragmentManager());
        mPager.setAdapter(mCategoryAdapter);
        mTabs.setViewPager(mPager);
    }

    private List<Map<String,Object>> getDrawerItems(){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("img",R.drawable.drawer_light);
        map.put("title",getString(R.string.guess));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img",R.drawable.drawer_all);
        map.put("title",getString(R.string.latest));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img",R.drawable.drawer_heart);
        map.put("title",getString(R.string.my_fav));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img",R.drawable.drawer_chat);
        map.put("title",getString(R.string.interview));
        list.add(map);
        return list;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String title = ((TextView)view.findViewById(R.id.item_name)).getText().toString();
        if(title.equals(getString(R.string.guess))){
        }else if(title.equals(getString(R.string.my_fav))){
            Intent intent = new Intent(mContext,FavoriteActivity.class);
            startActivity(intent);
        }else if(title.equals(getString(R.string.latest))){
        }else if(title.equals(getString(R.string.interview))){
            Intent intent = new Intent(mContext,InterviewActivity.class);
            startActivity(intent);
        }
        mDrawerLayout.closeDrawers();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_setting) {
			Intent intent = new Intent(mContext, SettingActivity.class);
			startActivity(intent);
			return true;
		}
        if(item.getItemId() == android.R.id.home){
            if(mDrawerLayout.isDrawerOpen(mDrawerLayout.getChildAt(1)))
                mDrawerLayout.closeDrawers();
            else{
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    public void showWhatsNew(){
        boolean showed = mSharedPreferences.getBoolean("showed15",false);
        if(!showed){
            Toast.makeText(mContext,R.string.intro_drawer,Toast.LENGTH_SHORT).show();
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
                   mSharedPreferences.edit().putBoolean("showed15",true).commit();
                }
            },3000);
        }
    }
}
