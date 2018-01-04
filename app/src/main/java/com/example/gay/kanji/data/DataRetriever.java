package com.example.gay.kanji.data;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

public class DataRetriever {

    private static final String TAG = "RETRV";

    private static final DataRetriever instance = new DataRetriever();

    static DataRetriever getInstance() { return instance; }

    private DataTask task;

    private static DataTask getTask() {
        if (instance.task == null)
            instance.task = new DataTask();
        return instance.task;
    }

    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor threadPool =
        new ThreadPoolExecutor(2, 2, 1, SECONDS, queue);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private DataRetriever() { }

    static public void retrieve(WebView wv, Character kanji) {
        Log.d(TAG, "retrieve: " + kanji);

        DataTask task = getTask();

        task.recycle();
        task.init(wv, kanji);

        instance.threadPool.execute(task.etymologyRunnable);
    }

    static void update(final DataTask task) {
        Log.d(TAG, "update: " + task);
        Message.obtain(
            instance.handler,
            new Runnable() {

                private static final String LOADING = "...";

                private boolean isAvailable(String s) { return !(s == null || s.isEmpty()); }

            /*  private void append(StringBuilder sb, String text) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(text);
                } */

                @Override
                public void run() {
                    StringBuilder text = new StringBuilder();

                    Character kanji = task.getKanji();
                    String etymology = task.getEtymology();
                    // TODO on, kun & meaning
                    // String on = "", kun = "", meaning = "";
                    boolean loading = etymology == null;
                    //  || on == null || kun == null || meaning == null;

                    boolean e = isAvailable(etymology);
                    // boolean okm = isAvailable(on) || isAvailable(kun) || isAvailable(meaning);

                    if (e)
                        text.append(etymology);
                    else if (loading)
                        text.append(LOADING);

                /*  if (okm) {
                        if (text.length() > 0)
                            text.append("<br>");
                        StringBuilder jdic = new StringBuilder();
                        append(jdic, on);
                        append(jdic, kun);
                        append(jdic, meaning);
                        text.append(jdic);
                    } else if (e && loading) {
                        text.append("<br>");
                        text.append(LOADING);
                    } */

                    if (text.length() > 0)
                        text.insert(0, " &ndash; ");

                    text.insert(0, kanji);

                    task.getWebView().loadUrl("javascript:setText(\"" + text + "\")");
                }
            }
        ).sendToTarget();
    }
}
