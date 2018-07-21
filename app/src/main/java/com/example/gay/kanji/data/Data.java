package com.example.gay.kanji.data;

public class Data {

    public static final String NO_DATA = "";

    public final String info;
    public final String gif;

    public Data(String info, String gif) {
        this.info = info;
        this.gif = gif;
    }

    public boolean isEmpty() {
        return NO_DATA.equals(info) && NO_DATA.equals(gif);
    }

    @Override
    public String toString() {
        return isEmpty() ? "EMPTY" : getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
