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

    static final String NO_DATA = "";

    private static final DataRetriever instance = new DataRetriever();

    static DataRetriever getInstance() { return instance; }

    private DataTask task;

    private static DataTask getTask() {
        if (instance.task == null)
            instance.task = new DataTask();
        return instance.task;
    }

    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4, 4, 1, SECONDS, queue);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private DataRetriever() { }

    static public void retrieve(WebView wv, Character kanji) {
        Log.d(TAG, "retrieve: " + kanji);

        DataTask task = getTask();

        task.recycle();
        task.init(wv, kanji);

        for (TaskRunnable runnable : task.getRunnables())
            instance.threadPool.execute(runnable);
    }

    static public void stop() {
        DataTask task = instance.task;

        if (task == null)
            return;

        for (TaskRunnable runnable : task.getRunnables()) {
            instance.threadPool.remove(runnable);
            Thread et = task.getThread(runnable);
            if (et != null)
                et.interrupt();
        }

        task.recycle();
    }

    static void update(final DataTask task) {
        Log.d(TAG, "update: " + task);
        Message.obtain(
            instance.handler,
            new Runnable() {

                private static final String LOADING = "...";

                private boolean isAvailable(String s) { return !(s == null || s.isEmpty()); }

                private void append(StringBuilder sb, String text) {
                    if (text.isEmpty())
                        return;
                    sb.append("<span>");
                    sb.append(text);
                    sb.append("</span>");
                }

                @Override
                public void run() {
                    StringBuilder info = new StringBuilder();

                    Character kanji = task.getKanji();
                    String etymology = task.getEtymology();
                    String on = task.getOn(), kun = task.getKun(), meaning = task.getMeaning();
                    boolean loading =
                        etymology == null
                        || on == null || kun == null || meaning == null;

                    boolean e = isAvailable(etymology);
                    boolean okm = isAvailable(on) || isAvailable(kun) || isAvailable(meaning);

                    if (e) {
                        info.append(etymology);
                        info.append("</p>");
                    } else if (loading) {
                        info.append(LOADING);
                        info.append("</p>");
                    }

                    if (okm) {
                        if (e) info.append("<p>");
                        StringBuilder jdic = new StringBuilder();
                        append(jdic, on);
                        append(jdic, highlightSuffixes(kun));
                        append(jdic, meaning);
                        info.append(jdic);
                        info.append("</p>");
                    } else if (e && loading) {
                        info.append("<p>");
                        info.append(LOADING);
                        info.append("</p>");
                    }

                    if (info.length() > 0)
                        info.insert(0, " &ndash; ");

                    info.insert(0, kanji);
                    info.insert(0, "<p>"); // FIXME this is shit, do something about it!

                    WebView wv = task.getWebView();
                    wv.loadUrl("javascript:setInfo(\"" + info + "\")");

                    // GIF

                    String gif = task.getGif();
                    if (gif != null)
                        wv.loadUrl("javascript:setGif(\"" + gif + "\")");
                }

                private String highlightSuffixes(String kun) {
                    return kun.replaceAll("\\.([^,]+)", "<span class='hlit'>$1</span>");
                }
            }
        ).sendToTarget();
    }
}
