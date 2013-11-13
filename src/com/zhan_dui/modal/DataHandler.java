package com.zhan_dui.modal;

import java.util.TreeMap;

import android.annotation.SuppressLint;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhan_dui.utils.ApiUtils;

@SuppressLint("DefaultLocale")
public class DataHandler {
	private static DataHandler mInstance;
	private static final String sRequestListUrl = "http://i.animetaste.net/api/animelist_v2/?api_key=%s&timestamp=%d&page=%d&access_token=%s";
	private static final String sRandomeRequestUrl = "http://i.animetaste.net/api/animelist_v3/?api_key=%s&timestamp=%d&order=random&limit=%d&access_token=%s";
	private static final String sFeaturesUrl = "http://i.animetaste.net/api/animelist_v3/?api_key=%s&timestamp=%d&feature=1&access_token=%s";
	private static final String API_KEY = "ios";
	private static final String API_SECRET = "8ce32e9a0072037578899a53e155441f";

	private DataHandler() {

	}

	public static DataHandler instance() {
		if (mInstance == null) {
			mInstance = new DataHandler();
		}
		return mInstance;
	}

	private void get(String request, JsonHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(request, null, handler);
	}

	public void getList(int page, JsonHttpResponseHandler handler) {
		long timeStamp = System.currentTimeMillis() / 1000L;
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("api_key", API_KEY);
		params.put("timestamp", String.valueOf(timeStamp));
		params.put("page", String.valueOf(page));
		String access_token = ApiUtils.getAccessToken(params, API_SECRET);
		String request = String.format(sRequestListUrl, API_KEY, timeStamp,
				page, access_token);
		get(request, handler);
	}

	public void getRandom(int count, JsonHttpResponseHandler handler) {
		long timeStamp = System.currentTimeMillis() / 1000L;
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("api_key", API_KEY);
		params.put("timestamp", String.valueOf(timeStamp));
		params.put("order", "random");
		params.put("limit", String.valueOf(count));
		String access_token = ApiUtils.getAccessToken(params, API_SECRET);
		String request = String.format(sRandomeRequestUrl, API_KEY, timeStamp,
				count, access_token);
		get(request, handler);
	}

	public void getFetures(JsonHttpResponseHandler handler) {
		long timestamp = System.currentTimeMillis() / 1000L;
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("api_key", API_KEY);
		params.put("timestamp", String.valueOf(timestamp));
		String access_token = ApiUtils.getAccessToken(params, API_SECRET);
		String request = String.format(sFeaturesUrl, API_KEY, timestamp,
				access_token);
		get(request, handler);
	}

}
