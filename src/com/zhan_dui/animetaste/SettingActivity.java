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
import android.view.MenuItem;
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
	private View mRecommand;
	private View mSuggestion;
	private View mFocusUs;

	private Switch mSwitchOnlyWifi;
	private Switch mSwitchUseHD;
	private SharedPreferences mSharedPreferences;

	private Context mContext;
	private SinaWeibo mWeibo;

	private static final int FAIL_AUTH = 1;
	private static final int FOLLOW = 2;
	private static final int FOLLOW_REPEAT = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_setting);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mRecommand = findViewById(R.id.recommend);
		mSuggestion = findViewById(R.id.suggestion);
		mFocusUs = findViewById(R.id.focus_us);

		mSwitchOnlyWifi = (Switch) findViewById(R.id.switch_wifi);
		mSwitchUseHD = (Switch) findViewById(R.id.switch_hd);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

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
		case R.id.use_hd:

			break;
		case R.id.suggestion:
			Intent intent = new Intent(mContext, FeedbackActivity.class);
			startActivity(intent);
			break;
		case R.id.recommend:
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					getText(R.string.share_title));
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					getText(R.string.share_app_body));
			startActivity(Intent.createChooser(shareIntent,
					getText(R.string.share_via)));
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@SuppressLint("HandlerLeak")
	private Handler mSocialLoginHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FOLLOW:
			case FOLLOW_REPEAT:
				Toast.makeText(mContext, R.string.follow_success,
						Toast.LENGTH_SHORT).show();
				break;
			case FAIL_AUTH:
				Toast.makeText(mContext, R.string.auth_failed,
						Toast.LENGTH_SHORT).show();
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
		if (action == Platform.ACTION_FOLLOWING_USER) {
			mSocialLoginHandler.sendEmptyMessage(FOLLOW_REPEAT);
		}
		if (action == Platform.ACTION_AUTHORIZING) {
			mSocialLoginHandler.sendEmptyMessage(FAIL_AUTH);
		}
	}

}
