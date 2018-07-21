package com.example.gay.kanji.data;

import android.support.v4.util.LruCache;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Cache {

    private static final String TAG = "CACHE";

    private static final int MAX_SIZE = 64;

    private static final LruCache<Character, Data> instance = new LruCache<>(MAX_SIZE);

    private Cache() { }

    static void put(Character kanji, Data data) {
        instance.put(kanji, data);
        log("put", kanji, data);
        onUpdate(kanji, data);
    }

    private static final List<UpdateListener> listeners = new ArrayList<>();

    public static void addUpdateListener(UpdateListener listener) {
        listeners.add(listener);
    }

    public static void removeUpdateListener(UpdateListener listener) {
        if (listener != null)
            listeners.remove(listener);
    }

    private static void onUpdate(Character kanji, Data data) {
        for (UpdateListener l : listeners)
            l.onCacheUpdated(kanji, data);
    }

    public static Data get(Character kanji) {
        Data data = instance.get(kanji);
        log("get", kanji, data);
        return data;
    }

    private static void log(String method, Character kanji, Data data) {
        Log.v(TAG, method + "(" + kanji + ") " + data);
    }

    public interface UpdateListener {
        void onCacheUpdated(Character kanji, Data data);
    }
}
