package com.example.gay.kanji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.gay.kanji.data.Data;

public class KanjiWebView extends WebView {

    private boolean loaded, current;
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
        getSettings().setJavaScriptEnabled(true);
        loadUrl("file:///android_asset/index-" + (App.isNightMode() ? "night" : "day") + ".html");
        setWebViewClient(webClient);
    }

    private final WebViewClient webClient = new WebViewClient() {

        public void onPageFinished(WebView view, String url) {
            loaded = true;
            update();
        }
    };

    private void update() {
        update(null);
    }

    public void update(Data data) {
        if (data != null) {
            info = data.getInfo();
            gif = data.getGif();
        }

        if (!loaded)
            return;

        loadUrl("javascript:setCurrent(" + current + ")");

        if (info != null)
            loadUrl("javascript:setInfo(\"" + info + "\")");
        if (gif != null)
            loadUrl("javascript:setGif(\"" + gif + "\")");

        setVisibility(VISIBLE);
    }

    public void setCurrent(boolean value) {
        current = value;
        if (loaded)
            loadUrl("javascript:setCurrent(" + current + ")");
    }
}
