package com.example.gay.kanji.data;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.gay.kanji.KanjiWebView;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DataTask {

    private static final String TAG = "TASK";

    private WeakReference<KanjiWebView> wvRef;
    private boolean stopped;

    private Character kanji;
    // TODO Halpern NJECD Index
    // private Integer idx; // Halpern NJECD Index
    private String gif;
    private String etymology;
    private String on, kun, meaning;

    private final DataRetriever dataRetriever;
    private final Map<TaskRunnable, Thread> runnable2thread = new LinkedHashMap<>();

    DataTask() {
        this(DataRetriever.getInstance());
    }

    DataTask(DataRetriever dataRetriever) {
        this.dataRetriever = dataRetriever;

        runnable2thread.put(new LoadingRunnable(this), null);
        runnable2thread.put(new KanjiRunnable(this), null);
        runnable2thread.put(new EtymologyRunnable(this), null);
        runnable2thread.put(new JdicRunnable(this), null);
    }

    void init(KanjiWebView wv, Character kanji) {
        Log.d(TAG, "init: " + kanji);

        stopped = false;

        this.wvRef = new WeakReference<>(wv);
        this.kanji = kanji;

        Data cached = Cache.get(kanji);
        if (cached == null) {
            for (TaskRunnable runnable : getRunnables())
                dataRetriever.getThreadPool().execute(runnable);
        } else {
            dataRetriever.update(this);
            stopped = true;
        }
    }

    // TODO unit test
    public void stop() {
        Log.d(TAG, "stop() " + this);

        stopped = true;

        for (TaskRunnable runnable : getRunnables()) {
            dataRetriever.getThreadPool().remove(runnable);
            Thread et = getThread(runnable);
            if (et != null)
                et.interrupt();
            setThread(runnable, null);
        }

        if (wvRef != null) {
            wvRef.clear();
            wvRef = null;
        }

        kanji = null;
        // idx = null;
        gif = null;
        etymology = null;
        on = null;
        kun = null;
        meaning = null;

        dataRetriever.tasks.add(this);
    }

    boolean isStopped() {
        return stopped;
    }

    @Nullable
    KanjiWebView getWebView() {
        return wvRef == null ? null : wvRef.get();
    }

    public Character getKanji() {
        return kanji;
    }

    private Set<TaskRunnable> getRunnables() {
        return runnable2thread.keySet();
    }

    private Thread getThread(TaskRunnable runnable) {
        synchronized (dataRetriever) {
            return runnable2thread.get(runnable);
        }
    }

    void setThread(TaskRunnable runnable, Thread thread) {
        synchronized (dataRetriever) {
            runnable2thread.put(runnable, thread);
        }
    }

    String getEtymology() {
        return etymology;
    }

    void setEtymology(String etymology) {
        this.etymology = etymology;
    }

    String getGif() {
        return gif;
    }

    void setGif(String gif) {
        this.gif = gif;
    }
/*
    Integer getIdx() {
        return idx;
    }

    void setIdx(Integer idx) {
        this.idx = idx;
    }*/

    @Nullable String getOn() {
        return on;
    }

    void setOn(String on) {
        this.on = on;
    }

    @Nullable String getKun() {
        return kun;
    }

    void setKun(String kun) {
        this.kun = kun;
    }

    String getMeaning() {
        return meaning;
    }

    void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    void updateUi() {
        dataRetriever.update(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "「" + kanji + "」" + Integer.toHexString(hashCode());
    }
}
