package com.example.gay.kanji.data;

import android.webkit.WebView;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class DataTask {

    private final DataRetriever retriever = DataRetriever.getInstance();

    private WeakReference<WebView> wvRef;

    private Character kanji;
    // TODO idx
    // private Integer idx; // Halpern NJECD Index
    private String gif;
    private String etymology;
    private String on, kun, meaning;

    private final Map<TaskRunnable, Thread> runnable2thread = new LinkedHashMap<>();

    DataTask() {
        runnable2thread.put(new LoadingRunnable(this), null);
        runnable2thread.put(new KanjiRunnable(this), null);
        runnable2thread.put(new EtymologyRunnable(this), null);
        runnable2thread.put(new WwwjdicRunnable(this), null);
    }

    void init(WebView wv, Character kanji) {
        this.wvRef = new WeakReference<>(wv);
        this.kanji = kanji;
    }

    // TODO unit test
    void recycle() {
        for (TaskRunnable runnable : getRunnables())
            setThread(runnable, null);

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
    }

    WebView getWebView() {
        return wvRef == null ? null : wvRef.get();
    }

    public Character getKanji() {
        return kanji;
    }

    Set<TaskRunnable> getRunnables() {
        return runnable2thread.keySet();
    }

    Thread getThread(TaskRunnable runnable) {
        synchronized (retriever) {
            return runnable2thread.get(runnable);
        }
    }

    void setThread(TaskRunnable runnable, Thread thread) {
        synchronized (retriever) {
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

    String getOn() {
        return on;
    }

    void setOn(String on) {
        this.on = on;
    }

    String getKun() {
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
}
