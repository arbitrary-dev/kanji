package com.example.gay.kanji.data;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Cache {

    private static final String TAG = "CACHE";

    private static final int MAX_SIZE = 64;

    private static final LruCache<Character, Data> instance =
        new LruCache<Character, Data>(MAX_SIZE) {

            @Override
            protected Data create(Character kanji) {
                return new Data(kanji);
            }
        };

    private Cache() { }

    private static void checkThread() {
        // FIXME replace with UiCallback
        if (Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalStateException("Should be called in UI thread!");
    }

    public static void put(Character kanji, Data data) {
        checkThread();
        Log.d(TAG, "put: " + data);
        instance.put(kanji, data);
        onUpdate(kanji, data);
    }

    /** @return Empty {@code Data} if kanji not cached */
    @NonNull
    public static Data get(Character kanji) {
        return instance.get(kanji);
    }

    private static void onUpdate(Character kanji, Data data) {
        for (UpdateListener l : listeners)
            l.onCacheUpdated(kanji, data);
    }

    private static final List<UpdateListener> listeners = new ArrayList<>();

    public static void addUpdateListener(UpdateListener listener) {
        listeners.add(listener);
    }

    public static void removeUpdateListener(UpdateListener listener) {
        if (listener != null)
            listeners.remove(listener);
    }

    // FIXME replace with UiCallback
    public interface UpdateListener {
        void onCacheUpdated(Character kanji, Data data);
    }
}
