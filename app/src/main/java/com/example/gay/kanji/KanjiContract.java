package com.example.gay.kanji;

import android.provider.BaseColumns;

public final class KanjiContract {

    private KanjiContract() { }

    public static class KanjiEntry implements BaseColumns {
        public static final String TABLE = "kanji";
        public static final String COL_SYMBOL = "symbol";
        public static final String COL_ETYMOLOGY = "etymology";

        public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE + " (" +
            COL_SYMBOL + " TEXT," +
            COL_ETYMOLOGY + " TEXT)";

        public static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE;
    }
}
