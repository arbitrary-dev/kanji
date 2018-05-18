package com.example.gay.kanji.data;

import android.os.Looper;

import org.junit.Test;
import org.mockito.Mock;

public class DataRetrieverTest {

    @Mock
    private Looper looperMock;

    @Test
    public void formInfo_emptyTask() {
        DataRetriever dataRetriever = new DataRetriever(looperMock);
        DataTask task = new DataTask(dataRetriever);
        dataRetriever.formInfo(task);
    }
}
