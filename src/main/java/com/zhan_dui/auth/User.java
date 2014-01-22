package com.zhan_dui.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class User {
	private SharedPreferences mSharedPreferences;

	public User(Context context) {
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
	}

	public boolean isLogin() {
		return mSharedPreferences.getBoolean("login", false);
	}

	public String getUsername() {
		return mSharedPreferences.getString("username", "username");
	}

	public String getAvatar() {
		return mSharedPreferences.getString("avatar", "avatar");
	}
}
