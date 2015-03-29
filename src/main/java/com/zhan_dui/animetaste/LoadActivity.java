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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.data.AnimeTasteDB;
import com.zhan_dui.data.ApiConnector;
import com.zhan_dui.model.Advertise;
import com.zhan_dui.model.Animation;
import com.zhan_dui.model.Category;
import com.zhan_dui.model.WatchRecord;
import com.zhan_dui.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.sharesdk.framework.ShareSDK;

public class LoadActivity extends ActionBarActivity {
    private Context mContext;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        google_bug();
        ActiveAndroid.setLoggingEnabled(false);
        ShareSDK.initSDK(mContext);

        updateFromOldVersion();

        setContentView(R.layout.activity_load);

        MobclickAgent.onError(this);
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(
                "only_wifi", true)
                && !NetworkUtils.isWifiConnected(mContext)) {
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
            builder.setNegativeButton(R.string.only_wifi_cancel,
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

    }

    ;

    private void google_bug() {//for support android 2.3
        new PrepareTask(null);
        try {
            ActionBar ab = getSupportActionBar();//support library bug
            if (ab != null) {
                ab.hide();
            }
        } catch (Exception e) {
        }
    }

    private void init() {

        if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            if (uri == null) {
                error();
            }
            String vid = uri.getQueryParameter("vid");
            int animationId = Integer.valueOf(vid);
            ApiConnector.instance().getDetail(animationId, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, final JSONObject response) {
                    super.onSuccess(statusCode, response);
                    try {
                        if (statusCode == 200 && response.has("data") && response.getJSONObject("data").has("result") && response.getJSONObject("data").getBoolean("result")) {
                            final JSONObject anime = response.getJSONObject("data").getJSONObject("anime");
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    MobclickAgent.onEvent(mContext, "yell");
                                    final Intent intent = new Intent(mContext, PlayActivity.class);
                                    Animation animation = Animation.build(anime);
                                    intent.putExtra("Animation", animation);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }
                            }.start();
                        } else {
                            error();
                        }
                    } catch (Exception e) {
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
                }
            });
        } else {
            ApiConnector.instance().getInitData(20, 5, 2, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, final JSONObject response) {
                    super.onSuccess(response);
                    if (statusCode == 200 && response.has("data")) {
                        Message msg = Message.obtain();
                        msg.obj = response;
                        executeHandler.sendMessage(msg);
                    } else {
                        error();
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    super.onFailure(error);
                    error();
                }
            });
        }
    }

    private Handler executeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            JSONObject o = (JSONObject) msg.obj;
            PrepareTask t = new PrepareTask(o);
            t.execute();
        }
    };

    private class PrepareTask extends AsyncTask<Void, Void, Boolean> {
        private JSONObject mSetupResponse;
        private Intent mIntent;
        private boolean mResult = false;

        public PrepareTask(JSONObject setupObject) {
            mSetupResponse = setupObject;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONObject list = mSetupResponse.getJSONObject("data").getJSONObject("list");
                JSONArray animations = list.getJSONArray("anime");
                JSONArray category = list.getJSONArray("category");
                JSONArray advert = null;

                if (list.has("advert"))
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


                for (int i = 0; i < Animations.size(); i++) {
                    Animations.get(i).save();
                }
                for (int i = 0; i < category.length(); i++) {
                    Category cat = Category.build(category.getJSONObject(i));
                    Categories.add(cat);
                    cat.save();
                }

                if (advert != null) {
                    for (int i = 0; i < advert.length(); i++) {
                        Advertise ad = Advertise.build(advert.getJSONObject(i));
                        Advertises.add(ad);
                        ad.save();
                    }
                }

                for (int i = 0; i < feature.length(); i++) {
                    Recommends.add(Animation.build(feature.getJSONObject(i)));
                }
                mIntent = new Intent(LoadActivity.this,
                        StartActivity.class);
                mIntent.putParcelableArrayListExtra("Animations", Animations);
                mIntent.putParcelableArrayListExtra("Categories", Categories);
                mIntent.putParcelableArrayListExtra("Advertises", Advertises);
                mIntent.putParcelableArrayListExtra("Recommends", Recommends);
                mIntent.putExtra("Success", true);
                mResult = true;
            } catch (Exception e) {
                e.printStackTrace();
                mResult = false;
            } finally {
                ActiveAndroid.setTransactionSuccessful();
                ActiveAndroid.endTransaction();
                return mResult;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mResult) {
                startActivity(mIntent);
                finish();
            } else {
                error();
            }
        }
    }

    private Handler errorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(mContext, R.string.get_data_error, Toast.LENGTH_SHORT)
                    .show();
            Intent intent = new Intent(LoadActivity.this, DownloadActivity.class);
            startActivity(intent);
            finish();
        }
    };

    private void error() {
        errorHandler.sendEmptyMessage(0);
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

    /**
     * 从历史版本的数据库中迁移数据
     */
    private void updateFromOldVersion() {

        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("updated", false)) {
            return;
        }

        if (mContext.getDatabasePath(AnimeTasteDB.NAME).exists()) {
            AnimeTasteDB db = new AnimeTasteDB(mContext, AnimeTasteDB.NAME, null, AnimeTasteDB.VERSION);
            Cursor cursor = db.getReadableDatabase().query(true, "Video", null, "isFav=?", new String[]{String.valueOf(true)}, null, null, null, null);
            while (cursor.moveToNext()) {
                Animation animation = Animation.build(cursor);
                animation.save();
            }
            cursor.close();
            db.close();
            Cursor watchCursor = db.getReadableDatabase().query(true, "Watched", null, null, null, null, null, null, null);
            while (watchCursor.moveToNext()) {
                WatchRecord record = new WatchRecord(watchCursor.getInt(watchCursor.getColumnIndex("vid")), true);
                record.save();
            }
            cursor.close();
            db.close();
        }
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("updated", true).commit();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.exit(0);
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
