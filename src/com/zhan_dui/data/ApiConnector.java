package com.zhan_dui.data;

import android.annotation.SuppressLint;
import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zhan_dui.utils.ApiUtils;

import java.util.TreeMap;

@SuppressLint("DefaultLocale")
public class ApiConnector {

	private static ApiConnector mInstance;

    private static final String INIT_REQUEST_URL = "http://i.animetaste.net/api/setup/?api_key=%s&timestamp=%s&anime=%d&feature=%d&advert=%d&access_token=%s";
    private static final String ANIMATION_REQUEST_URL = "http://i.animetaste.net/api/animelist_v4/?api_key=%s&timestamp=%s&page=%d&access_token=%s";
    private static final String ANIMATION_RANDOM_URL = "http://i.animetaste.net/api/animelist_v4/?api_key=%s&timestamp=%d&order=random&limit=%d&access_token=%s";

    private static final String API_KEY = "android";
	private static final String API_SECRET = "7763079ba6abf342a99ab5a1dfa87ba8";

	private ApiConnector() {

	}

	public static ApiConnector instance() {
		if (mInstance == null) {
			mInstance = new ApiConnector();
		}
		return mInstance;
	}

	private void get(String request, JsonHttpResponseHandler handler) {
		AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(10000);

		client.get(request, null, handler);
	}

    public void getInitData(int animeCount,int featureCount,int advertiseCount,JsonHttpResponseHandler handler){
        long timeStamp = System.currentTimeMillis()/1000L;
        TreeMap<String,String> params = new TreeMap<String, String>();
        params.put("api_key",API_KEY);
        params.put("timestamp",String.valueOf(timeStamp));
        params.put("anime",String.valueOf(animeCount));
        params.put("feature",String.valueOf(featureCount));
        params.put("advert",String.valueOf(advertiseCount));
        String access_token = ApiUtils.getAccessToken(params,API_SECRET);
        String request = String.format(INIT_REQUEST_URL,API_KEY,timeStamp,animeCount,featureCount,advertiseCount,access_token);
        get(request, handler);
    }

	public void getList(int page, JsonHttpResponseHandler handler) {
		long timeStamp = System.currentTimeMillis() / 1000L;
		TreeMap<String, String> params = new TreeMap<String, String>();
		params.put("api_key", API_KEY);
		params.put("timestamp", String.valueOf(timeStamp));
		params.put("page", String.valueOf(page));
		String access_token = ApiUtils.getAccessToken(params, API_SECRET);
		String request = String.format(ANIMATION_REQUEST_URL, API_KEY, timeStamp,
				page, access_token);
        Log.e("toRequest",request);
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
		String request = String.format(ANIMATION_RANDOM_URL, API_KEY, timeStamp,
				count, access_token);
		get(request, handler);
	}
}
