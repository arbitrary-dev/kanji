package com.example.gay.kanji.data;

public class Data {

    static final String NO_DATA = "";

    public final String info;
    public final String gif;

    Data(String info, String gif) {
        this.info = info;
        this.gif = gif;
    }

    public boolean isEmpty() {
        return NO_DATA.equals(info) && NO_DATA.equals(gif);
    }
}
