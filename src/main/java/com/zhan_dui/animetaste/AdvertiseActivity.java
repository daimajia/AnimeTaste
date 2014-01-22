package com.zhan_dui.animetaste;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.modal.Advertise;

/**
 * Created by daimajia on 14-1-17.
 */
public class AdvertiseActivity extends ActionBarActivity {
    private WebView mAdvertiseWebView;
    private AdvertiseClient mAdvertiseClient;
    private Advertise mAdvertise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdvertise = getIntent().getParcelableExtra("Advertise");
        setContentView(R.layout.activity_advertise);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mAdvertise.Name);
        mAdvertiseClient = new AdvertiseClient();
        mAdvertiseWebView = (WebView)findViewById(R.id.recommend_webview);
        mAdvertiseWebView.getSettings().setJavaScriptEnabled(true);
        mAdvertiseWebView.setWebViewClient(mAdvertiseClient);
        mAdvertiseWebView.loadUrl(mAdvertise.Link);
    }

    private class AdvertiseClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            MobclickAgent.onEvent(AdvertiseActivity.this,"ad");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
