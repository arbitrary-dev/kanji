package com.example.gay.kanji.data;

import org.junit.Test;

import static com.example.gay.kanji.data.Data.LOADING;
import static com.example.gay.kanji.data.Data.NO_DATA;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class DataTest {

    @Test
    public void correctness() {
        Data data = Data.builder('C')
            .setGif("gif")
            .setEtymology("etymology")
            .setOn("on")
            .setKun("kun")
            .setMeaning("meaning")
            .build();
        assertEquals('C', (char) data.kanji);
        assertEquals("gif", data.getGif());
        assertEquals("etymology", data.getEtymology());
        assertEquals("on", data.getOn());
        assertEquals("kun", data.getKun());
        assertEquals("meaning", data.getMeaning());
    }

    @Test
    public void formInfo_rawData() {
        Data data = new Data(' ');
        data.formInfo();
        assertEquals(LOADING, data.getInfo());
    }

    @Test
    public void buildEmptyData() {
        Data data = Data.builder(' ')
            .setGif(NO_DATA)
            .setEtymology(NO_DATA)
            .setOn(NO_DATA)
            .setKun(NO_DATA)
            .setMeaning(NO_DATA)
            .build();
        assertTrue(data.isEmpty());
        assertTrue(data.isFull());
    }

    @Test
    public void buildFullData() {
        Data.Builder b = Data.builder(' ')
            .setGif(NO_DATA)
            .setEtymology(NO_DATA)
            .setOn(NO_DATA)
            .setKun(NO_DATA)
            .setMeaning(NO_DATA);

        Data data = b.setGif("gif").build();
        assertTrue(data.isFull());
        assertFalse(data.isEmpty());

        data = b.setEtymology("etymology").build();
        assertTrue(data.isFull());
        assertFalse(data.isEmpty());

        data = b.setOn("on").build();
        assertTrue(data.isFull());
        assertFalse(data.isEmpty());

        data = b.setKun("kun").build();
        assertTrue(data.isFull());
        assertFalse(data.isEmpty());

        data = b.setMeaning("meaning").build();
        assertTrue(data.isFull());
        assertFalse(data.isEmpty());

        data = b.setGif("gif")
            .setEtymology("etymology")
            .setOn("on")
            .setKun("kun")
            .setMeaning("meaning")
            .build();
        assertTrue(data.isFull());
        assertFalse(data.isEmpty());
    }

    @Test
    public void buildNotFullData() {
        Data.Builder b = Data.builder(' ')
            .setGif(NO_DATA)
            .setEtymology(NO_DATA)
            .setOn(NO_DATA)
            .setKun(NO_DATA)
            .setMeaning(NO_DATA);

        Data data = b.setGif(null).build();
        assertFalse(data.isFull());
        assertFalse(data.isEmpty());

        data = b.setEtymology(null).build();
        assertFalse(data.isFull());
        assertFalse(data.isEmpty());

        data = b.setOn(null).build();
        assertFalse(data.isFull());
        assertFalse(data.isEmpty());

        data = b.setKun(null).build();
        assertFalse(data.isFull());
        assertFalse(data.isEmpty());

        data = b.setMeaning(null).build();
        assertFalse(data.isFull());
        assertFalse(data.isEmpty());
    }
}
