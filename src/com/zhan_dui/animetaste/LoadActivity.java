package com.zhan_dui.animetaste;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.avos.avoscloud.Parse;
import com.avos.avoscloud.ParseAnalytics;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.data.VideoDB;
import com.zhan_dui.modal.DataHandler;
import com.zhan_dui.utils.NetworkUtils;

public class LoadActivity extends ActionBarActivity {
	private Context mContext;
	private VideoDB mVideoDB;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parse.initialize(this,
				"w43xht9daji0uut74pseeiibax8c2tnzxowmx9f81nvtpims",
				"86q8251hrodk6wnf4znistay1mva9rm1xikvp1s9mhp5n7od");
		ParseAnalytics.trackAppOpened(getIntent());
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}
		mContext = this;
		mVideoDB = new VideoDB(mContext, VideoDB.NAME, null, VideoDB.VERSION);

		setContentView(R.layout.activity_load);
		MobclickAgent.onError(this);
		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(
				"only_wifi", true)
				&& NetworkUtils.isWifi(mContext) == false) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
					.setTitle(R.string.only_wifi_title).setMessage(
							R.string.only_wifi_body);
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.only_wifi_ok,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							initLatest();
						}
					});
			builder.setNegativeButton(R.string.obly_wifi_cancel,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			builder.create().show();
		} else {
			initLatest();
		}

	};

	private void initLatest() {
		DataHandler.instance().getList(0, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				super.onSuccess(statusCode, response);

				if (statusCode == 200 && response.has("list")) {
					try {
						initFeatures(response.getJSONArray("list").toString());
					} catch (JSONException e) {
						e.printStackTrace();
						finish();
					}
				} else {
					finish();
				}
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				Toast.makeText(getApplicationContext(), R.string.error_load,
						Toast.LENGTH_SHORT).show();
				if (mVideoDB.getVideosCount() > 5)
					startActivity(new Intent(mContext, StartActivity.class));
				else {
					finish();
				}
				finish();
			}
		});
	}

	private void initFeatures(final String lastestVideosArrayString) {
		DataHandler.instance().getFetures(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				super.onSuccess(statusCode, response);
				try {
					Intent intent = new Intent(LoadActivity.this,
							StartActivity.class);
					intent.putExtra("LoadData", lastestVideosArrayString);
					intent.putExtra("LoadFeatures",
							response.getJSONArray("list").toString());
					startActivity(intent);
				} catch (JSONException e) {
					Toast.makeText(mContext, "获取数据出错", Toast.LENGTH_SHORT)
							.show();
				} finally {
					finish();
				}

			}
		});
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

}
