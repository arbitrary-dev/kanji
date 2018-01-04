package com.example.gay.kanji.data;

import android.webkit.WebView;

import java.lang.ref.WeakReference;

class DataTask {

    private final DataRetriever retriever = DataRetriever.getInstance();

    private Thread threadEtymology;
    private String etymology;

    private WeakReference<WebView> wvRef;
    private Character kanji;

    private final Runnable etymologyRunnable = new EtymologyRunnable(this);

    void init(WebView wv, Character kanji) {
        this.wvRef = new WeakReference<>(wv);
        this.kanji = kanji;
    }

    void recycle() {
        setThreadEtymology(null);
        etymology = null;

        if (wvRef != null) {
            wvRef.clear();
            wvRef = null;
        }
        kanji = null;
    }

    Runnable getEtymologyRunnable() {
        return etymologyRunnable;
    }

    WebView getWebView() {
        return wvRef == null ? null : wvRef.get();
    }

    public Character getKanji() {
        return kanji;
    }

    Thread getThreadEtymology() {
        synchronized (retriever) {
            return threadEtymology;
        }
    }

    void setThreadEtymology(Thread threadEtymology) {
        synchronized (retriever) {
            this.threadEtymology = threadEtymology;
        }
    }

    String getEtymology() {
        return etymology;
    }

    void setEtymology(String etymology) {
        this.etymology = etymology;
        DataRetriever.update(this);
    }
}
