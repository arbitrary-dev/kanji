package com.example.gay.kanji.data;

import android.util.Log;

class LoadingRunnable extends TaskRunnable {

    private static final String TAG = "LOAD";

    LoadingRunnable(DataTask task) {
        super(task);
    }

    @Override
    protected void runInner() throws InterruptedException {
        Log.d(TAG, "runInner: DataRetriever.update(task)");
        DataRetriever.update(task);
    }
}
