package com.example.gay.kanji.data;

import android.os.Process;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

abstract class TaskRunnable implements Runnable {

    final DataTask task;

    TaskRunnable(DataTask task) {
        this.task = task;
    }

    abstract protected void runInner() throws InterruptedException;

    @Override
    public void run() {
        try {
            checkIfInterrupted();

            // setup
            task.setThread(this, Thread.currentThread());
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
