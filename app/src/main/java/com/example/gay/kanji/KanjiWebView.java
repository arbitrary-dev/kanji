package com.example.gay.kanji;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.gay.kanji.data.DataRetriever;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_TEXT;

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

            // TODO move sharedText up and iterate over it chars until we found one
            // TODO make error when none of sharedText chars exist in DB
            Character kanji = '字';
            Intent intent = getIntent();
            if (    intent != null
                && ACTION_SEND.equals(intent.getAction())
                && "text/plain".equals(intent.getType())) {
                String sharedText = clean(intent.getStringExtra(EXTRA_TEXT));
                if (sharedText != null) {
                    Log.d(TAG, "sharedText = \"" + sharedText + "\"");
                    kanji = sharedText.charAt(0);
                }
            }

            KanjiWebView.this.loadUrl("javascript:setInfo('" + kanji + "')");
            KanjiWebView.this.setVisibility(VISIBLE);


            // TODO smart loading
            // There'd be a loader icon first 1-2 seconds waiting for
            // everything to be loaded, if etymology misses the time and still
            // loading, then everything is revealed, but etymology will have a
            // "Loading..." placeholder.
            DataRetriever.retrieve(KanjiWebView.this, kanji);
        }
    };

    /** Cleans all non-japanese symbols from the {@code input} */
    private String clean(String input) {
        return input == null ? null
            : input.replaceAll("[^\u3040-\u309f\u30a0-\u30ff\u4e00-\u9faf]", "");
    }

    private Intent getIntent() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity)
                return ((Activity) context).getIntent();
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
