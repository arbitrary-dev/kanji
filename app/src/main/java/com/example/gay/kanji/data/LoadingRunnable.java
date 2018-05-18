package com.example.gay.kanji.data;

class LoadingRunnable extends TaskRunnable {

    String getLoggingTag() { return "LOAD"; }

    LoadingRunnable(DataTask task) {
        super(task);
    }

    @Override
    protected void runInner() throws InterruptedException {
        logd("Loading initialized");
        task.updateUi();
    }
}
