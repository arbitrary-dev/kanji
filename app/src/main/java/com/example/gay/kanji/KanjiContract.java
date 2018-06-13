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
        public static final String COL_ON = "oon";
        public static final String COL_KUN = "kun";
        public static final String COL_MEANING = "meaning";

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

            // 2
            update(
                "ALTER TABLE " + TABLE + " RENAME TO " + TABLE + "_old",
                "CREATE TABLE " + TABLE + " (" +
                    COL_SYMBOL + " TEXT CONSTRAINT pk PRIMARY KEY ASC, " +
                    COL_ETYMOLOGY + " TEXT, " +
                    COL_ON + " TEXT, " +
                    COL_KUN + " TEXT, " +
                    COL_MEANING + " TEXT)",
                "INSERT INTO " + TABLE + " (" +
                    COL_SYMBOL + ", " + COL_ETYMOLOGY + ") " +
                    "SELECT " + COL_SYMBOL + ", " + COL_ETYMOLOGY + " FROM " + TABLE + "_old",
                "DROP TABLE " + TABLE + "_old"
            );

            // 3
            update(
                "UPDATE " + TABLE + " SET " + COL_ETYMOLOGY + " = NULL WHERE " + COL_ETYMOLOGY + " = ''",
                "UPDATE " + TABLE + " SET " + COL_ON + " = NULL WHERE " + COL_ON + " = ''",
                "UPDATE " + TABLE + " SET " + COL_KUN + " = NULL WHERE " + COL_KUN + " = ''",
                "UPDATE " + TABLE + " SET " + COL_MEANING + " = NULL WHERE " + COL_MEANING + " = ''",
                "DELETE FROM " + TABLE +
                    " WHERE coalesce(" + COL_ETYMOLOGY + ", " + COL_ON + ", " + COL_KUN +
                    ", " + COL_MEANING + ") IS NULL"
            );
        }
    }
}
