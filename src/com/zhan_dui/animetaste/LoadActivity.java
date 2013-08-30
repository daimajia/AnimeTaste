package com.zhan_dui.animetaste;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.modal.DataFetcher;

public class LoadActivity extends ActionBarActivity {
	private Context mContext;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		getSupportActionBar().hide();
		mContext = this;
		setContentView(R.layout.activity_load);
		DataFetcher.instance().getList(0, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				super.onSuccess(statusCode, response);
				Intent intent = new Intent(LoadActivity.this,
						StartActivity.class);
				if (statusCode == 200 && response.has("list")) {
					try {
						intent.putExtra("LoadData",
								response.getJSONArray("list").toString());

						startActivity(intent);
						finish();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {

				}
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				Toast.makeText(getApplicationContext(), R.string.error_load,
						Toast.LENGTH_SHORT).show();
				startActivity(new Intent(mContext, StartActivity.class));
				finish();
			}
		});

	};

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
