package com.example.gay.kanji.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.gay.kanji.App;
import com.example.gay.kanji.KanjiContract.KanjiEntry;

import java.util.Arrays;

class Db {

    private final String tag;
    private final String data;
    private final Character kanji;

    private Db(String tag, String data, Character kanji) {
        this.tag = tag;
        this.data = data;
        this.kanji = kanji;
    }

    static Db getFor(String tag, String data, Character kanji) {
        return new Db(tag, data, kanji);
    }

    String[] query(String... cols) throws InterruptedException {
        String[] result = new String[cols.length];

        Log.d(tag, "Lookup 「" + kanji + "」 " + data + " in DB");

        checkIfInterrupted();

        try (Cursor cursor = App.getReadableDatabase().query(
            KanjiEntry.TABLE,
            cols,
            KanjiEntry.COL_SYMBOL + " = ?",
            new String[] { kanji.toString() },
            null, null, null,
            "1"
        )) {
            if (cursor.moveToNext()) {
                boolean found = true;
                for (int i = 0; i < cols.length; ++i) {
                    String colName = cols[i];
                    int col = cursor.getColumnIndex(colName);
                    result[i] = cursor.getString(col);
                    found &= result[i] != null;
                }
                if (found)
                    Log.d(tag, "Found " + data + " in DB for 「" + kanji + "」: "
                        + Arrays.toString(result));
                else
                    Log.d(tag, "No " + data + " in DB for 「" + kanji + "」");
            } else {
                Log.d(tag, "No " + data + " in DB for 「" + kanji + "」");
            }
        }

        return result;
    }

    void persist(String... colsAndVals) throws InterruptedException {
        if (colsAndVals.length % 2 != 0)
            throw new IllegalArgumentException(
                "colsAndVals should contain paired columns and values");

        checkIfInterrupted();

        ContentValues values = new ContentValues();
        values.put(KanjiEntry.COL_SYMBOL, kanji.toString());
        for (int i = 0; i < colsAndVals.length; i += 2) {
            String col = colsAndVals[i];
            String val = colsAndVals[i + 1];
            values.put(col, val);
        }

        checkIfInterrupted();

        SQLiteDatabase db = App.getWritableDatabase();

        checkIfInterrupted();

        long res = db.update(
            KanjiEntry.TABLE,
            values,
            KanjiEntry.COL_SYMBOL + " = ?",
            new String[] { kanji.toString() }
        );

        if (res == 0) {
            checkIfInterrupted();
            res = db.insert(KanjiEntry.TABLE, null, values);
        }

        if (res == -1)
            Log.e(tag, "Failed to persist " + data + " in DB for 「" + kanji + "」");
        else
            Log.d(tag, "Persisted " + data + " in DB for 「" + kanji + "」");
    }

    private void checkIfInterrupted() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
    }
}
