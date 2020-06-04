package com.example.gay.kanji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.gay.kanji.data.Data;

public class KanjiWebView extends WebView {

    private boolean webClientIsReady, current;
    private Character kanji;
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
            webClientIsReady = true;
            update();
        }
    };

    private void update() {
        update(null);
    }

    public void update(Data data) {
        if (data != null) {
            kanji = data.kanji;
            info = data.getInfo();
            gif = data.getGif();
        }

        if (!webClientIsReady)
            return;

        loadUrl("javascript:setKanji(\"" + kanji + "\")");
        loadUrl("javascript:setCurrent(" + current + ")");
        loadUrl("javascript:setGif(\"" + gif + "\")");

        if (info != null) {
            loadUrl("javascript:setInfo(\"" + info + "\")");
            if (gif != null)
                loadUrl("javascript:collapseInfoOnUpdate()");
        }

        setVisibility(VISIBLE);
    }

    public void setCurrent(boolean value) {
        current = value;
        if (webClientIsReady)
            loadUrl("javascript:setCurrent(" + current + ")");
    }
}
