package com.zhan_dui.animetaste;

import java.util.HashMap;

import org.jraf.android.backport.switchwidget.Switch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;

import com.umeng.analytics.MobclickAgent;

public class SettingActivity extends ActionBarActivity implements
		OnClickListener, OnCheckedChangeListener, PlatformActionListener {
	private View mOnlyForWifi;
	private View mClearCache;
	private View mRecommand;
	private View mSuggestion;
	private View mFocusUs;
	private View mUseHD;

	private Switch mSwitchOnlyWifi;
	private Switch mSwitchUseHD;
	private SharedPreferences mSharedPreferences;

	private Context mContext;
	private SinaWeibo mWeibo;

	private static final int AUTH = 1;
	private static final int FOLLOW = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_setting);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mClearCache = findViewById(R.id.clear_cache);
		mUseHD = findViewById(R.id.use_hd);
		mRecommand = findViewById(R.id.recommend);
		mSuggestion = findViewById(R.id.suggestion);
		mFocusUs = findViewById(R.id.focus_us);

		mSwitchOnlyWifi = (Switch) findViewById(R.id.switch_wifi);
		mSwitchUseHD = (Switch) findViewById(R.id.switch_hd);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		mClearCache.setOnClickListener(this);
		mRecommand.setOnClickListener(this);
		mSuggestion.setOnClickListener(this);
		mFocusUs.setOnClickListener(this);
		mSwitchOnlyWifi.setOnCheckedChangeListener(this);
		mSwitchUseHD.setOnCheckedChangeListener(this);

		mSwitchOnlyWifi.setChecked(mSharedPreferences.getBoolean("only_wifi",
				true));
		mSwitchUseHD.setChecked(mSharedPreferences.getBoolean("use_hd", false));
		ShareSDK.initSDK(mContext);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.only_for_wifi:
			break;
		case R.id.clear_cache:
			break;
		case R.id.use_hd:

			break;
		case R.id.suggestion:
			Intent intent = new Intent(mContext, FeedbackActivity.class);
			startActivity(intent);
			break;
		case R.id.recommend:

			break;
		case R.id.focus_us:
			mWeibo = new SinaWeibo(mContext);
			mWeibo.setPlatformActionListener(this);
			mWeibo.authorize();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.switch_wifi:
			mSharedPreferences.edit().putBoolean("only_wifi", isChecked)
					.commit();
			break;
		case R.id.switch_hd:
			mSharedPreferences.edit().putBoolean("use_hd", isChecked).commit();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(mContext);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(mContext);
	}

	@SuppressLint("HandlerLeak")
	private Handler mSocialLoginHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FOLLOW:
				Toast.makeText(mContext, "关注成功", Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onCancel(Platform platform, int action) {

	}

	@Override
	public void onComplete(Platform platform, int action,
			HashMap<String, Object> res) {
		if (action == Platform.ACTION_AUTHORIZING) {
			platform.followFriend("AnimeTaste全球动画精选");
		}
		if (action == Platform.ACTION_FOLLOWING_USER) {
			mSocialLoginHandler.sendEmptyMessage(FOLLOW);
		}
	}

	@Override
	public void onError(Platform platform, int action, Throwable throwable) {

	}

}
