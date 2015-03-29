package com.zhan_dui.animetaste;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zhan_dui.model.Animation;

public class BrowserPlayerActivity extends ActionBarActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browserplayer);
        String url = "http://animetaste.net";
        String title = "AnimeTaste";
        if(getIntent().getParcelableExtra("animation") != null){
            Animation animation = getIntent().getParcelableExtra("animation");

            url = animation.Youku;
            title = animation.Name;
        }
        getSupportActionBar().setTitle(title);
        webView = (WebView)findViewById(R.id.browser);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new BrowserClient());
        webView.loadUrl(url);
    }

    private class BrowserClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }
}
