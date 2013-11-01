package com.zhan_dui.animetaste;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;

import com.avos.avoscloud.Parse;
import com.avos.avoscloud.ParseAnalytics;
import com.zhan_dui.auth.SocialPlatform;

public class AuthActivity extends ActionBarActivity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parse.initialize(this,
				"w43xht9daji0uut74pseeiibax8c2tnzxowmx9f81nvtpims",
				"86q8251hrodk6wnf4znistay1mva9rm1xikvp1s9mhp5n7od");
		ParseAnalytics.trackAppOpened(getIntent());
		setContentView(R.layout.activity_auth);
		findViewById(R.id.button1).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			SocialPlatform socialPlatform = new SocialPlatform(
					getApplicationContext());
			socialPlatform.auth(QZone.NAME, mHandler);
			break;

		default:
			break;
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Toast.makeText(AuthActivity.this, msg.what + "", Toast.LENGTH_SHORT)
					.show();
		};
	};
}
