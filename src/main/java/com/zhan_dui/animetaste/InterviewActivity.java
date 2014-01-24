package com.zhan_dui.animetaste;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by daimajia on 14-1-24.
 */
public class InterviewActivity extends ActionBarActivity{
    private WebView mWebView;
    private InterviewClient mWebClient;
    private Context mContext;
    private SuperActivityToast mSuperToast;
    private static final String AT = "http://touch.animetaste.net";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_interview);
        getSupportActionBar().setTitle(getString(R.string.interview));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mWebView =(WebView)findViewById(R.id.webview_iterview);
        mWebClient = new InterviewClient();
        mWebView.setWebViewClient(mWebClient);
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mSuperToast.setProgress(newProgress);
                if(newProgress == 100){
                    mSuperToast.dismiss();
                }
            }
        });
        mWebView.loadUrl(AT);
    }

    private class InterviewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mSuperToast  = new SuperActivityToast(mContext,SuperToast.Type.PROGRESS_HORIZONTAL);
            mSuperToast.setIndeterminate(true);
            mSuperToast.setText(getString(R.string.web_loading));
            mSuperToast.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mSuperToast.dismiss();
            MobclickAgent.onEvent(mContext, "AT");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.advertise,menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    mSuperToast.dismiss();
                    if(mWebView.canGoBack() == true){
                        mWebView.goBack();
                    }else{
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_open_in_browser:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AT));
                startActivity(browserIntent);
                return true;
        }
        return super.onContextItemSelected(item);
    }
}
