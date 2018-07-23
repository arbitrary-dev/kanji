package com.example.gay.kanji.pager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.gay.kanji.KanjiWebView;
import com.example.gay.kanji.data.Cache;
import com.example.gay.kanji.data.Data;
import com.example.gay.kanji.data.DataTask;

class UiCallback implements Runnable {

    private static final String TAG = "UICALB";

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final DataTask task;
    private final KanjiWebView webView;

    UiCallback(DataTask task, KanjiWebView webView) {
        this.task = task;
        this.webView = webView;
    }

    @Override
    public void run() {
        Log.d(TAG, "UiCallback.run() " + task);
        Message.obtain(
            handler,
            () -> {
                if (!webView.isAttachedToWindow())
                    return;

                Data data = task.getData();
                Cache.put(data.kanji, data);

                if (!webView.isAttachedToWindow())
                    return;

                webView.update(data);

                // TODO fallback to wikipedia's gif
                // TODO replace unavailable gif with some static kanji
            }
        ).sendToTarget();
    }
}
