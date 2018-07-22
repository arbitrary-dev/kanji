package com.example.gay.kanji.data;

import org.junit.Test;

public class DataTest {

    @Test
    public void getInfo_emptyData() {
        Data data = new Data(' ');
        data.getInfo();
    }
}
