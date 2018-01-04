package com.example.gay.kanji.data;

abstract class InterruptibleRunnable implements Runnable {

    abstract protected void runInner() throws InterruptedException;

    @Override
    public void run() {
        try {
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
