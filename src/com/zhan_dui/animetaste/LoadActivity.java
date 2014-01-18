package com.zhan_dui.animetaste;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import com.avos.avoscloud.Parse;
import com.avos.avoscloud.ParseAnalytics;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.data.ApiConnector;
import com.zhan_dui.data.VideoDB;
import com.zhan_dui.modal.Advertise;
import com.zhan_dui.modal.Animation;
import com.zhan_dui.modal.Category;
import com.zhan_dui.utils.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadActivity extends ActionBarActivity {
	private Context mContext;
	private VideoDB mVideoDB;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		Parse.initialize(this,
				"w43xht9daji0uut74pseeiibax8c2tnzxowmx9f81nvtpims",
				"86q8251hrodk6wnf4znistay1mva9rm1xikvp1s9mhp5n7od");
		ShareSDK.initSDK(mContext);
		ParseAnalytics.trackAppOpened(getIntent());
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}
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
							init();
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
			init();
		}

	};

    private void init(){
        ApiConnector.instance().getInitData(20,5,2,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode,JSONObject response) {
                super.onSuccess(response);
                if(statusCode == 200 && response.has("data")){
                    try{
                        JSONObject list = response.getJSONObject("data").getJSONObject("list");
                        JSONArray anime = list.getJSONArray("anime");
                        JSONArray category = list.getJSONArray("category");
                        JSONArray advert = list.getJSONArray("advert");
                        JSONArray feature = list.getJSONArray("recommend");
                        ArrayList<Animation> Animations = Animation.build(anime);
                        ArrayList<Category> Categories = new ArrayList<Category>();
                        ArrayList<Advertise> Advertises = new ArrayList<Advertise>();
                        ArrayList<Animation> Recommends = new ArrayList<Animation>();
                        for(int i = 0; i < category.length();i++){
                            Categories.add(Category.build(category.getJSONObject(i)));
                        }
                        for(int i = 0; i < advert.length();i++){
                            Advertises.add(Advertise.build(advert.getJSONObject(i)));
                        }
                        for(int i = 0; i< feature.length();i++){
                            Recommends.add(Animation.build(feature.getJSONObject(i)));
                        }
                        Intent intent = new Intent(LoadActivity.this,
                                StartActivity.class);
                        intent.putParcelableArrayListExtra("Animations",Animations);
                        intent.putParcelableArrayListExtra("Categories",Categories);
                        intent.putParcelableArrayListExtra("Advertises",Advertises);
                        intent.putParcelableArrayListExtra("Recommends",Recommends);
                        intent.putExtra("Success",true);
                        startActivity(intent);
                    }catch(Exception e){
                        e.printStackTrace();
                        error();
                    }
                }else{
                    error();
                }
            }
        });
    }

    private void error(){
        Toast.makeText(mContext, "获取数据出错", Toast.LENGTH_SHORT)
                .show();
        finish();
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
