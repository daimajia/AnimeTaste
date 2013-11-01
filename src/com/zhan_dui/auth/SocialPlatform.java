package com.zhan_dui.auth;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.ParseException;
import com.avos.avoscloud.ParseObject;
import com.avos.avoscloud.ParseQuery;
import com.avos.avoscloud.SaveCallback;

public class SocialPlatform implements PlatformActionListener {
	private Context mContext;
	private Platform mSocialPlatform;
	private Handler mHandler;
	private SharedPreferences mSharedPreferences;

	public static final int AUTH_CANCEL = 0;
	public static final int AUTH_SUCCESS = 5;
	public static final int AUTH_FAILED = 10;
	public static final int AUTH_SAVE_FAILED = 15;

	public SocialPlatform(Context context) {
		ShareSDK.initSDK(context);
		mContext = context;
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
	}

	public void auth(String PlatformName, Handler handler) {
		mHandler = handler;
		mSocialPlatform = ShareSDK.getPlatform(mContext, PlatformName);
		mSocialPlatform.setPlatformActionListener(this);
		mSocialPlatform.authorize();
	}

	private void saveInformation(final Platform platform, boolean update,
			ParseObject toUpdateobject) {
		final ParseObject object;
		if (update) {
			object = toUpdateobject;
		} else {
			object = new ParseObject("Users");
		}
		object.setFetchWhenSave(true);
		object.put("username", platform.getDb().getUserName());
		object.put("avatar", platform.getDb().getUserIcon());
		object.put("uid", platform.getDb().getUserId());
		object.put("platform", platform.getName());
		object.put("others", platform.getDb().exportData());
		object.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException err) {
				if (err == null) {
					mSharedPreferences
							.edit()
							.putString("objectid", object.getObjectId())
							.putString("username",
									platform.getDb().getUserName())
							.putString("avatar", platform.getDb().getUserIcon())
							.putString("uid", platform.getDb().getUserId())
							.putBoolean("login", true)
							.putString("platform", platform.getName()).commit();
					mHandler.sendEmptyMessage(AUTH_SUCCESS);
				} else {
					mHandler.sendEmptyMessage(AUTH_SAVE_FAILED);
				}
			}
		});
	}

	@Override
	public void onCancel(Platform platform, int action) {
		mHandler.sendEmptyMessage(AUTH_CANCEL);
	}

	@Override
	public void onComplete(final Platform platform, int action,
			HashMap<String, Object> res) {

		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Users");
		query.whereEqualTo("platform", platform.getName());
		query.whereEqualTo("uid", platform.getDb().getUserId());
		query.setLimit(1);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					if (objects.size() > 0) {
						saveInformation(platform, true, objects.get(0));
					} else {
						saveInformation(platform, false, null);
					}
				} else {
					mHandler.sendEmptyMessage(AUTH_SAVE_FAILED);
				}
			}
		});

	}

	@Override
	public void onError(Platform platform, int action, Throwable throwable) {
		mHandler.sendEmptyMessage(AUTH_FAILED);
	}
}
