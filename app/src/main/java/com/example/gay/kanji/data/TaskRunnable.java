package com.example.gay.kanji.data;

import android.os.Process;
import android.util.Log;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

abstract class TaskRunnable implements Runnable {

    final DataTask task;

    TaskRunnable(DataTask task) {
        this.task = task;
    }

    abstract protected void runInner() throws InterruptedException;

    private static final String TAG = "TRUN";

    @Override
    public void run() {
        Log.d(TAG, "run() " + this);
        try {
            checkIfInterrupted();

            // setup
            task.setThread(this, Thread.currentThread());
            Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);

            runInner();
        } catch (InterruptedException e) {
            Log.d(TAG, "interrupted " + this);
            e.printStackTrace();
        } finally {
            Thread.interrupted();
            task.setThread(this, null);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " " + task;
    }

    void checkIfInterrupted() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
    }

    abstract String getLoggingTag();
    String getLoggingData() { return null; }

    private static final int LOG_DEBUG = 0;
    private static final int LOG_ERROR = 1;

    /**
     *  Outputs "{@code <prefix>} {@code <getLoggingData()>} for 「{@code <task.getKanjiAt()>}」
     *  {@code <suffixes[0]>} ... {@code <suffixes[N]>}" to DEBUG log
     */
    void logd(String prefix, String... suffixes) {
        log(LOG_DEBUG, prefix, suffixes);
    }

    /**
     *  Outputs "{@code <prefix>} {@code <getLoggingData()>} for 「{@code <task.getKanjiAt()>}」
     *  {@code <suffixes[0]>} ... {@code <suffixes[N]>}" to ERROR log
     */
    void loge(String prefix, String... suffixes) {
        log(LOG_ERROR, prefix, suffixes);
    }

    private void log(int type, String prefix, String... suffixes) {
        if (prefix == null || prefix.trim().isEmpty())
            throw new IllegalArgumentException("Logging prefix is mandatory");

        StringBuilder sb = new StringBuilder(prefix);

        {
            String data = getLoggingData();
            if (data != null) {
                sb.append(' ');
                sb.append(data);
            }
        }

        sb.append(" for 「");
        sb.append(task.getKanji());
        sb.append('」');

        for (String suffix : suffixes) {
            sb.append(' ');
            sb.append(suffix);
        }

        String tag = getLoggingTag();
        String message = sb.toString();
        switch (type) {
            case LOG_DEBUG:
                Log.d(tag, message);
                break;
            case LOG_ERROR:
                Log.e(tag, message);
                break;
        }
    }
}
