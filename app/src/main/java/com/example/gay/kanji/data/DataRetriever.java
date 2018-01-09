package com.example.gay.kanji.data;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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

    // FIXME don't retrieve same kanji twice
    static public void retrieve(WebView wv, Character kanji) {
        stop();

        Log.d(TAG, "retrieve: " + kanji);

        DataTask task = getTask();
        task.init(wv, kanji);

        for (TaskRunnable runnable : task.getRunnables())
            instance.threadPool.execute(runnable);
    }

    static public void stop() {
        Log.d(TAG, "stop()");
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
            () -> {
                WebView wv = task.getWebView();

                String info = formInfo(task);
                wv.loadUrl("javascript:setInfo(\"" + info + "\")");

                String gif = task.getGif();
                if (gif != null)
                    wv.loadUrl("javascript:setGif(\"" + gif + "\")");

                // FIXME no kanji data
                // TODO replace empty gif with static kanji, iff other data is available
            }
        ).sendToTarget();
    }

    private static final String LOADING = "…";

    private static void addLine(List<String> data, String line) {
        if (!NO_DATA.equals(line))
            data.add(line);
    }

    // TODO unit test
    private static String formInfo(DataTask task) {
        List<String> data = new LinkedList<>();
        addLine(data, task.getEtymology());
        addLine(data, formJdic(task));

        // collapse adjacent nulls and replace with LOADING
        ListIterator<String> it = data.listIterator();
        boolean prevWasNull = false;
        while (it.hasNext()) {
            String curr = it.next();
            if (curr == null) {
                if (prevWasNull) it.remove();
                else it.set(LOADING);
                prevWasNull = true;
            } else {
                prevWasNull = false;
            }
        }

        String kanji = task.getKanji().toString();

        // headline
        if (data.isEmpty())
            data.add(kanji);
        else
            data.set(0, kanji + " &ndash; " + data.get(0));

        StringBuilder info = new StringBuilder();

        for (String line : data) {
            info.append("<p>");
            info.append(line);
            info.append("</p>");
        }

        return info.toString();
    }

    private static String formJdic(DataTask task) {
        String on = task.getOn();
        String kun = task.getKun();
        String meaning = task.getMeaning();

        if (on == null || kun == null || meaning == null)
            return null;

        StringBuilder jdic = new StringBuilder();
        appendSpan(jdic, on);
        appendSpan(jdic, highlightSuffixes(kun));
        appendSpan(jdic, meaning);

        return jdic.toString();
    }

    private static void appendSpan(StringBuilder sb, String text) {
        if (text.isEmpty())
            return;
        sb.append("<span>");
        sb.append(text);
        sb.append("</span>");
    }

    private static String highlightSuffixes(String kun) {
        return kun.replaceAll("\\.([^,]+)", "<span class='hlit'>$1</span>");
    }
}
