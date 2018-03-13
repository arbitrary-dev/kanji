package com.example.gay.kanji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.gay.kanji.data.DataRetriever;

public class KanjiWebView extends WebView {

    private static final String TAG = "WEBV";

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
            update();
        }
    };

    public void update() {
        Character kanji = App.getKanji();

        KanjiWebView.this.loadUrl("javascript:setInfo('" + kanji + "')");
        KanjiWebView.this.setVisibility(VISIBLE);

        // TODO smart loading
        // There'd be a loader icon first 1-2 seconds waiting for
        // everything to be loaded, if etymology misses the time and still
        // loading, then everything is revealed, but etymology will have a
        // "Loading..." placeholder.
        DataRetriever.retrieve(KanjiWebView.this, kanji);
    }
}
