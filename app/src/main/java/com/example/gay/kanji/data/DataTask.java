package com.example.gay.kanji.data;

import android.util.Log;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

public class DataTask {

    private static final String TAG = "TASK";

    private final Map<TaskRunnable, Thread> runnable2thread = new ConcurrentHashMap<>(3);

    private static final LinkedBlockingQueue<Runnable> QUEUE = new LinkedBlockingQueue<>();
    private static final ThreadPoolExecutor THREAD_POOL =
        new ThreadPoolExecutor(4, 4 * 64, 10, SECONDS, QUEUE);

    final Data data;

    public DataTask(Data data) {
        this.data = data;
    }

    public void resume(Runnable uiCallback) {
        if (uiCallback == null)
            throw new IllegalArgumentException("uiCallback can't be null!");

        this.uiCallback = uiCallback;

        Log.d(TAG, "resume() " + this);

        updateUi();

        if (data.getGif() == null)
            THREAD_POOL.execute(new KanjiRunnable(this));
        if (data.getEtymology() == null)
            THREAD_POOL.execute(new EtymologyRunnable(this));
        if (data.getOn() == null || data.getKun() == null || data.getMeaning() == null)
            THREAD_POOL.execute(new JdicRunnable(this));
    }

    public void stop() {
        Log.d(TAG, "stop() " + this);

        uiCallback = null;

        for (TaskRunnable runnable : getRunnables()) {
            THREAD_POOL.remove(runnable);
            Thread et = getThread(runnable);
            if (et != null)
                et.interrupt();
        }
    }

    private Set<TaskRunnable> getRunnables() {
        return runnable2thread.keySet();
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
        return data;
    }

    private Runnable uiCallback;

    void updateUi() {
        if (uiCallback != null)
            uiCallback.run();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "「" + data.kanji + "」" + Integer.toHexString(hashCode());
    }
}
