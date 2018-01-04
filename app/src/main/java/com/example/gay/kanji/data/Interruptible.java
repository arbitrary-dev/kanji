package com.example.gay.kanji.data;

import android.os.Process;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

abstract class InterruptibleRunnable implements Runnable {

    abstract protected void runInner() throws InterruptedException;

    @Override
    public void run() {
        try {
            checkIfInterrupted();
            Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
            runInner();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Thread.interrupted();
        }
    }

    void checkIfInterrupted() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
    }
}
