package com.example.gay.kanji.data;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.gay.kanji.KanjiWebView;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

// TODO refactor data retrieval mess
public class DataRetriever {

    private static final String TAG = "RETRV";

    static final String NO_DATA = "";

    static final Object lock = new Object();

    private static final ConcurrentLinkedQueue<DataTask> tasks = new ConcurrentLinkedQueue<>();

    static ConcurrentLinkedQueue<DataTask> getTasks() {
        return tasks;
    }

    private static DataTask getTask() {
        DataTask task = tasks.poll();
        return task == null ? new DataTask() : task;
    }

    private static final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private static final ThreadPoolExecutor threadPool =
        new ThreadPoolExecutor(4, 4, 1, SECONDS, queue);
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private DataRetriever() { }

    // FIXME don't retrieve same kanji twice
    // TODO smart loading
    // There'd be a loader icon first 1-2 seconds waiting for
    // everything to be loaded, if etymology misses the time and still
    // loading, then everything is revealed, but etymology will have a
    // "Loading..." placeholder.
    static public DataTask retrieve(KanjiWebView wv, Character kanji) {
        Log.d(TAG, "retrieve: " + kanji);
        DataTask task = getTask();
        task.init(wv, kanji);
        return task;
    }

    static void update(final DataTask task) {
        Log.d(TAG, "update: " + task);
        Message.obtain(
            handler,
            () -> {
                KanjiWebView wv = task.getWebView();

                String info = formInfo(task);
                String gif = task.getGif();

                if (info.length() == "<p>X</p>".length() && NO_DATA.equals(gif))
                    throw new RuntimeException("Fuck you!"); // TODO make more plausible

                wv.setInfo(info);
                wv.setGif(gif);

                // TODO fallback to wikipedia's gif
                // TODO replace unavailable gif with some static kanji
            }
        ).sendToTarget();
    }

    private static final String LOADING = "…";

    private static void addLine(List<String> data, String line) {
        if (NO_DATA.equals(line))
            return;

        if (line != null)
            line = line.replace("\"", "\\\"");

        data.add(line);
    }

    // TODO unit test
    private static String formInfo(DataTask task) {
        List<String> data = new LinkedList<>();
        addLine(data, task.getEtymology());
        addLine(data, formJdic(task));

        // Collapse adjacent nulls and replace with LOADING
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
        appendSpan(jdic, dimSuffixes(kun));
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

    private static String dimSuffixes(String kun) {
        return kun.replaceAll("\\.([^,]+)", "<span class='dim'>$1</span>");
    }

    static ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }
}
