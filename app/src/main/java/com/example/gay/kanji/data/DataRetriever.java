package com.example.gay.kanji.data;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.gay.kanji.KanjiWebView;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static com.example.gay.kanji.App.JAP_CHAR_RANGE;
import static java.util.concurrent.TimeUnit.SECONDS;

// TODO refactor data retrieval mess
public class DataRetriever {

    private static final String TAG = "RETRV";

    static final String NO_DATA = "";

    private final ConcurrentLinkedQueue<DataTask> tasks = new ConcurrentLinkedQueue<>();

    ConcurrentLinkedQueue<DataTask> getTasks() {
        return tasks;
    }

    private DataTask getTask() {
        DataTask task = tasks.poll();
        return task == null ? new DataTask() : task;
    }

    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor threadPool =
        new ThreadPoolExecutor(4, 4, 1, SECONDS, queue);
    private final Handler handler;

    // For testing only
    DataRetriever(Looper looper) {
        handler = new Handler(looper);
    }

    private static class Singleton {
        static final DataRetriever INSTANCE = new DataRetriever(Looper.getMainLooper());
    }

    public static DataRetriever getInstance() {
        return Singleton.INSTANCE;
    }

    // FIXME don't retrieve same kanji twice
    // TODO smart loading
    // There'd be a loader icon first 1-2 seconds waiting for
    // everything to be loaded, if etymology misses the time and still
    // loading, then everything is revealed, but etymology will have a
    // "Loading..." placeholder.
    public DataTask retrieve(KanjiWebView wv, Character kanji) {
        Log.d(TAG, "retrieve: " + kanji);
        DataTask task = getTask();
        task.init(wv, kanji);
        return task;
    }

    void update(final DataTask task) {
        Log.d(TAG, "update: " + task);
        Message.obtain(
            handler,
            () -> {
                if (task.isStopped())
                    return;

                String info = formInfo(task);
                String gif = task.getGif();

                KanjiWebView wv = task.getWebView();

                if (wv == null)
                    return;

                if (NO_DATA.equals(info) && NO_DATA.equals(gif)) {
                    wv.setInfo("No data"); // TODO skip & dim
                    return;
                }

                wv.setInfo(info);
                wv.setGif(gif);

                // TODO fallback to wikipedia's gif
                // TODO replace unavailable gif with some static kanji
            }
        ).sendToTarget();
    }

    private static final String LOADING = "…";

    private void addLine(List<String> data, String line) {
        if (NO_DATA.equals(line))
            return;

        if (line != null)
            line = line.replace("\"", "\\\"");

        data.add(line);
    }

    String formInfo(DataTask task) {
        List<String> data = new LinkedList<>();
        addLine(data, glue(clean(task.getEtymology())));
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

    private String formJdic(DataTask task) {
        String on = task.getOn();
        String kun = task.getKun();
        String meaning = task.getMeaning();

        if (on == null || kun == null || meaning == null)
            return null;

        StringBuilder jdic = new StringBuilder();
        appendSpan(jdic, glue(on));
        appendSpan(jdic, dimSuffixes(glue(kun)));
        appendSpan(jdic, glue(meaning));

        return jdic.toString();
    }

    private void appendSpan(StringBuilder sb, String text) {
        if (text.isEmpty())
            return;
        sb.append("<span class='section'>");
        sb.append(text);
        sb.append("</span> "); // space at the end is necessary to have even spacing
    }
    /** Cleans some nasty shit retreived from the Internets */
    @Nullable private String clean(String s) {
        return s == null ? null
            : s.replaceAll("(?<=[\\[(])\\s+|\\s+(?=[]),.])", "")
               .replaceAll("([^ ])(?=[(\\[])", "$1 ");
    }

    /** Glues together kanji's so they are not sparsely justified */
    @Nullable private String glue(String s) {
        return s == null ? null : s.replaceAll(
            "([\\[(\\-" + JAP_CHAR_RANGE + "]+[" + JAP_CHAR_RANGE + "][])\\-.," + JAP_CHAR_RANGE
                + "]*|[\\[(\\-" + JAP_CHAR_RANGE + "]*[" + JAP_CHAR_RANGE + "][])\\-.,"
                + JAP_CHAR_RANGE + "]+)",
            "<span class='glue'>$1</span>"
        );
    }

    private String dimSuffixes(String kun) {
        return kun.replaceAll("\\.([" + JAP_CHAR_RANGE + "]+)", "<span class='dim'>$1</span>");
    }

    ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }
}
