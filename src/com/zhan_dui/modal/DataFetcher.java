package com.zhan_dui.modal;

import java.util.TreeMap;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhan_dui.utils.ApiUtils;

public class DataFetcher {
	private static DataFetcher mInstance;
	private static final String mRequestListUrl = "http://i.animetaste.net/api/animelist_v2/?api_key=%s&timestamp=%d&page=%d&access_token=%s";
	private static final String API_KEY = "ios";
	private static final String API_SECRET = "8ce32e9a0072037578899a53e155441f";
	
	private DataFetcher() {
		
	}

	public static DataFetcher instance() {
		if (mInstance == null) {
			mInstance = new DataFetcher();
		}
		return mInstance;
	}

	public void getList(int page, JsonHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
		long timeStamp = System.currentTimeMillis() / 1000L;
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("api_key", API_KEY);
		params.put("timestamp", String.valueOf(timeStamp));
		params.put("page", String.valueOf(page));

		String access_token = ApiUtils.getAccessToken(params, API_SECRET);
		String request = String.format(mRequestListUrl, API_KEY, timeStamp,
				page, access_token);
		client.get(request, null, handler);
	}
}
