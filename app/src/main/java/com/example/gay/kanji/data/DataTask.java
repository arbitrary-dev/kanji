package com.example.gay.kanji.data;

import android.util.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

public class DataTask {

    private static final String TAG = "TASK";

    private final Set<TaskRunnable> executedRunnables = new HashSet<>();
    private final Map<TaskRunnable, Thread> runnable2thread = new ConcurrentHashMap<>(3);

    private static final LinkedBlockingQueue<Runnable> QUEUE = new LinkedBlockingQueue<>();
    private static final ThreadPoolExecutor THREAD_POOL =
        new ThreadPoolExecutor(4, 4 * 64, 10, SECONDS, QUEUE);

    final Character kanji;
    volatile String gif;
    volatile String etymology;
    volatile String on, kun, meaning;

    public DataTask(Data data) {
        kanji = data.kanji;
        gif = data.getGif();
        etymology = data.getEtymology();
        on = data.getOn();
        kun = data.getKun();
        meaning = data.getMeaning();
    }

    public void resume(Runnable uiCallback) {
        if (uiCallback == null)
            throw new IllegalArgumentException("uiCallback can't be null!");

        this.uiCallback = uiCallback;

        Log.d(TAG, "resume() " + this);

        updateUi();

        if (gif == null)
            executeRunnable(new KanjiRunnable(this));
        if (etymology == null)
            executeRunnable(new EtymologyRunnable(this));
        if (on == null || kun == null || meaning == null)
            executeRunnable(new JdicRunnable(this));
    }

    public void stop() {
        Log.d(TAG, "stop() " + this);

        uiCallback = null;

        for (TaskRunnable runnable : executedRunnables) {
            THREAD_POOL.remove(runnable);
            Thread et = getThread(runnable);
            if (et != null)
                et.interrupt();
        }
        executedRunnables.clear();
    }

    private void executeRunnable(TaskRunnable runnable) {
        THREAD_POOL.execute(runnable);
        executedRunnables.add(runnable);
    }

    private Thread getThread(TaskRunnable runnable) {
        return runnable2thread.get(runnable);
    }

    void setThread(TaskRunnable runnable, Thread thread) {
        runnable2thread.put(runnable, thread);
    }

    void removeThread(TaskRunnable runnable) {
        runnable2thread.remove(runnable);
    }

    public Data getData() {
        return Data.builder(kanji)
            .setGif(gif)
            .setEtymology(etymology)
            .setOn(on)
            .setKun(kun)
            .setMeaning(meaning)
            .build();
    }

    private Runnable uiCallback;

    void updateUi() {
        if (uiCallback != null)
            uiCallback.run();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "「" + kanji + "」" + Integer.toHexString(hashCode());
    }
}
