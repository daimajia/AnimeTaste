package com.zhan_dui.animetaste;

import org.jraf.android.backport.switchwidget.Switch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.baidu.social.core.BaiduSocialException;
import com.baidu.social.core.BaiduSocialListener;
import com.baidu.social.core.Utility;
import com.baidu.sociallogin.BaiduSocialLogin;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.config.SocialLoginConfig;

public class SettingActivity extends ActionBarActivity implements
		OnClickListener, OnCheckedChangeListener, BaiduSocialListener {
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

	private BaiduSocialLogin mSocialLogin;

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
		mSocialLogin = BaiduSocialLogin.getInstance(mContext,
				SocialLoginConfig.BaiduAppKey);

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
//			mSocialLogin.supportWeiBoSso(SocialLoginConfig.WeiboAppKey);
			mSocialLogin.authorize(this, Utility.SHARE_TYPE_SINA_WEIBO, this);
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

	@Override
	public void onApiComplete(String response) {
		Log.i("Weibo Auth", Utility.decodeUnicode(response));
	}

	@Override
	public void onAuthComplete(Bundle bundle) {
		
	}

	@Override
	public void onError(BaiduSocialException exception) {
		Log.e("Weibo Error", exception.toString());
	}

}
