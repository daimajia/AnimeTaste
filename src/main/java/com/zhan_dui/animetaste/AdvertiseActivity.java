package com.zhan_dui.animetaste;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.model.Advertise;

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
        switch (item.getItemId()){
            case R.id.action_open_in_browser:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mAdvertise.Link));
                startActivity(browserIntent);
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.advertise,menu);
        return true;
    }

}
