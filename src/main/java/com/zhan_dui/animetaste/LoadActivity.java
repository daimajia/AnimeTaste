package com.zhan_dui.animetaste;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.avos.avoscloud.Parse;
import com.avos.avoscloud.ParseAnalytics;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.data.AnimeTasteDB;
import com.zhan_dui.data.ApiConnector;
import com.zhan_dui.modal.Advertise;
import com.zhan_dui.modal.Animation;
import com.zhan_dui.modal.Category;
import com.zhan_dui.modal.WatchRecord;
import com.zhan_dui.utils.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoadActivity extends ActionBarActivity {
	private Context mContext;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
        Parse.initialize(this,
                "w43xht9daji0uut74pseeiibax8c2tnzxowmx9f81nvtpims",
                "86q8251hrodk6wnf4znistay1mva9rm1xikvp1s9mhp5n7od");
        ActiveAndroid.setLoggingEnabled(false);
		ShareSDK.initSDK(mContext);
		ParseAnalytics.trackAppOpened(getIntent());
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}

        updateFromOldVersion();

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

        if(getIntent().getAction().equals(Intent.ACTION_VIEW)){
            Uri uri = getIntent().getData();
            if(uri == null){
                error();
            }
            String vid = uri.getQueryParameter("vid");
            int animationId = Integer.valueOf(vid);
            ApiConnector.instance().getDetail(animationId,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode,final JSONObject response) {
                    super.onSuccess(statusCode, response);
                    try{
                        if(statusCode == 200 && response.has("data") && response.getJSONObject("data").has("result") && response.getJSONObject("data").getBoolean("result") == true){
                            final JSONObject anime = response.getJSONObject("data").getJSONObject("anime");
                            new Thread(){
                                @Override
                                public void run() {
                                    super.run();
                                    MobclickAgent.onEvent(mContext,"yell");
                                    final Intent intent = new Intent(mContext,PlayActivity.class);
                                    Animation animation = Animation.build(anime);
                                    intent.putExtra("Animation",animation);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }
                            }.start();
                        }else{
                            error();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        error();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, JSONArray jsonArray) {
                    super.onFailure(throwable, jsonArray);
                    error();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    Log.e("Faild","true");
                }
            });
        }else{
            ApiConnector.instance().getInitData(20,5,2,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode,JSONObject response) {
                    super.onSuccess(response);
                    if(statusCode == 200 && response.has("data")){
                        new PrepareTask(response).execute();
                    }else{
                        error();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, String s) {
                    super.onFailure(throwable, s);
                    Toast.makeText(mContext,R.string.get_data_error,Toast.LENGTH_SHORT).show();
                    List<Animation> animations = new Select().from(Animation.class).orderBy("AnimationId desc").execute();
                    List<Category> categories = new Select().from(Category.class).orderBy("cid asc").execute();
                    List<Advertise> advertises = new Select().from(Advertise.class).orderBy("adid asc").execute();
                    List<Animation> recommends = new Select().from(Animation.class).orderBy("AnimationId desc").limit(5).execute();
                    ArrayList<Animation> Animations = new ArrayList<Animation>();
                    ArrayList<Category> Categories = new ArrayList<Category>();
                    ArrayList<Advertise> Advertises = new ArrayList<Advertise>();
                    ArrayList<Animation> Recommends = new ArrayList<Animation>();
                    Animations.addAll(animations);
                    Categories.addAll(categories);
                    Advertises.addAll(advertises);
                    Recommends.addAll(recommends);
                    Intent mIntent = new Intent(LoadActivity.this,
                            StartActivity.class);
                    mIntent.putParcelableArrayListExtra("Animations",Animations);
                    mIntent.putParcelableArrayListExtra("Categories",Categories);
                    mIntent.putParcelableArrayListExtra("Advertises",Advertises);
                    mIntent.putParcelableArrayListExtra("Recommends",Recommends);
                    mIntent.putExtra("Success",true);
                    startActivity(mIntent);
                }
            });
        }
    }

    private class PrepareTask extends AsyncTask<Void,Void,Boolean>{
        private JSONObject mSetupResponse;
        private Intent mIntent;
        private boolean mResult = false;

        public PrepareTask(JSONObject setupObject){
            mSetupResponse = setupObject;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                JSONObject list = mSetupResponse.getJSONObject("data").getJSONObject("list");
                JSONArray animations = list.getJSONArray("anime");
                JSONArray category = list.getJSONArray("category");
                JSONArray advert = null;

                if(list.has("advert"))
                    advert = list.getJSONArray("advert");

                JSONArray feature = list.getJSONArray("recommend");
                ArrayList<Animation> Animations = Animation.build(animations);
                ArrayList<Category> Categories = new ArrayList<Category>();
                ArrayList<Advertise> Advertises = new ArrayList<Advertise>();
                ArrayList<Animation> Recommends = new ArrayList<Animation>();
                new Delete().from(Animation.class).where("IsFavorite='0'").execute();
                new Delete().from(Category.class).execute();
                new Delete().from(Advertise.class).execute();
                ActiveAndroid.beginTransaction();


                for(int i =0;i<Animations.size();i++){
                    Animations.get(i).save();
                }
                for(int i = 0; i < category.length();i++){
                    Category cat = Category.build(category.getJSONObject(i));
                    Categories.add(cat);
                    cat.save();
                }

                if(advert!=null){
                    for(int i = 0; i < advert.length();i++){
                        Advertise ad = Advertise.build(advert.getJSONObject(i));
                        Advertises.add(ad);
                        ad.save();
                    }
                }

                for(int i = 0; i< feature.length();i++){
                    Recommends.add(Animation.build(feature.getJSONObject(i)));
                }
                ActiveAndroid.setTransactionSuccessful();
                mIntent = new Intent(LoadActivity.this,
                        StartActivity.class);
                mIntent.putParcelableArrayListExtra("Animations",Animations);
                mIntent.putParcelableArrayListExtra("Categories",Categories);
                mIntent.putParcelableArrayListExtra("Advertises",Advertises);
                mIntent.putParcelableArrayListExtra("Recommends",Recommends);
                mIntent.putExtra("Success",true);
                mResult = true;
            }catch(Exception e){
                e.printStackTrace();
                mResult = false;
            }finally {
                ActiveAndroid.endTransaction();
                return mResult;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(mResult){
                startActivity(mIntent);
                finish();
            }else{
                error();
            }
        }
    }

    private void error(){
        Toast.makeText(mContext, R.string.get_data_error, Toast.LENGTH_SHORT)
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

    /***
     * 从历史版本的数据库中迁移数据
     */
    private void updateFromOldVersion(){

        if(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("updated",false)){
            return;
        }

        if(mContext.getDatabasePath(AnimeTasteDB.NAME).exists()){
            AnimeTasteDB db = new AnimeTasteDB(mContext, AnimeTasteDB.NAME, null, AnimeTasteDB.VERSION);
            Cursor cursor =  db.getReadableDatabase().query(true,"Video",null,"isFav=?",new String[]{String.valueOf(true)},null,null,null,null);
            while(cursor.moveToNext()){
                Animation animation = Animation.build(cursor);
                animation.save();
            }
            cursor.close();
            db.close();
            Cursor watchCursor = db.getReadableDatabase().query(true,"Watched",null,null,null,null,null,null,null);
            while(watchCursor.moveToNext()){
                WatchRecord record = new WatchRecord(watchCursor.getInt(watchCursor.getColumnIndex("vid")),true);
                record.save();
            }
            cursor.close();
            db.close();
        }
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("updated",true).commit();
    }
}
