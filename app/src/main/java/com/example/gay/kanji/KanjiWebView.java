package com.example.gay.kanji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class KanjiWebView extends WebView {

    private static final String TAG = "WEBV";

    private boolean loaded = false;
    private String info, gif;

    public KanjiWebView(Context context) {
        super(context);
        init();
    }

    public KanjiWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KanjiWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        Log.d(TAG, "init()");
        getSettings().setJavaScriptEnabled(true);
        loadUrl("file:///android_asset/index-" + (App.isNightMode() ? "night" : "day") + ".html");
        setWebViewClient(webClient);
    }

    private final WebViewClient webClient = new WebViewClient(){

        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "WebViewClient.onPageFinished()");
            loaded = true;
            update();
        }
    };

    public void setInfo(String info) {
        this.info = info;
        if (loaded)
            loadUrl("javascript:setInfo(\"" + info + "\")");
    }

    public void setGif(String gif) {
        this.gif = gif;
        if (loaded)
            loadUrl("javascript:setGif(\"" + gif + "\")");
    }

    private void update() {
        if (!loaded)
            return;
        if (info != null)
            loadUrl("javascript:setInfo(\"" + info + "\")");
        if (gif != null)
            loadUrl("javascript:setGif(\"" + gif + "\")");
        setVisibility(VISIBLE);
    }
}
