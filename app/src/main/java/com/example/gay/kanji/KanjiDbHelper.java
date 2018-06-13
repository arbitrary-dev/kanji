package com.example.gay.kanji;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.gay.kanji.KanjiContract.KanjiEntry;

import static com.example.gay.kanji.KanjiContract.KanjiEntry.SQL_UPDATES;

class KanjiDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "kanji.db";

    KanjiDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(KanjiEntry.SQL_DELETE);
        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int ver = oldVersion + 1; ver <= newVersion; ++ver)
            for (String sql : SQL_UPDATES.get(ver))
                db.execSQL(sql);
    }
}
