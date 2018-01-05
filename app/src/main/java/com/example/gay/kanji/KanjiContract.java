package com.example.gay.kanji;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public final class KanjiContract {

    private KanjiContract() { }

    public static class KanjiEntry implements BaseColumns {

        public static final String TABLE = "kanji";

        public static final String COL_SYMBOL = "symbol";
        public static final String COL_ETYMOLOGY = "etymology";

        static final String SQL_CREATE =
            "CREATE TABLE " + TABLE + " (" +
                COL_SYMBOL + " TEXT," +
                COL_ETYMOLOGY + " TEXT)";

        static final String SQL_DELETE = "DROP TABLE IF EXISTS " + TABLE;

        static final List<String[]> SQL_UPDATES = new ArrayList<>();

        private static void update(String... sql) {
            SQL_UPDATES.add(sql);
        }

        // Updates
        static {
            // 0
            update("");

            // 1
            update(SQL_CREATE);
        }
    }
}
