package com.example.gay.kanji.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.gay.kanji.App;
import com.example.gay.kanji.KanjiContract.KanjiEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

class EtymologyRunnable extends InterruptibleRunnable {

    private static final String TAG = "ETYM";

    private static String link(Character kanji) {
        return String.format(
            "http://www.chineseetymology.org/CharacterEtymology.aspx?characterInput=%s",
            kanji
        );
    }

    private final DataTask task;

    EtymologyRunnable(DataTask task) {
        this.task = task;
    }

    @Override
    protected void runInner() throws InterruptedException {

        // setup

        task.setThreadEtymology(Thread.currentThread());

        // query from cache

        Character kanji = task.getKanji();
        String etymology = null;

        checkIfInterrupted();

        try (Cursor cursor = App.getReadableDatabase().query(
            KanjiEntry.TABLE,
            new String[] { KanjiEntry.COL_ETYMOLOGY },
            KanjiEntry.COL_SYMBOL + " = ?",
            new String[] { kanji.toString() },
            null, null, null,
            "1"
        )) {
            if (cursor.moveToNext()) {
                int colEtymology = cursor.getColumnIndex(KanjiEntry.COL_ETYMOLOGY);
                etymology = cursor.getString(colEtymology);
                Log.d(TAG, "Etymology retrieved from cache: " + etymology);
            } else {
                Log.d(TAG, "No cached etymology for " + kanji);
            }
        }

        // retrieve from web

        if (etymology == null) {
            checkIfInterrupted();

            if (App.isConnected()) {
                try {
                    checkIfInterrupted();

                    Document doc = Jsoup.connect(link(kanji)).get();
                    // TODO integration test
                    Elements es = doc.select("#etymologyLabel p");

                    etymology = es.text().trim();

                    if (!etymology.isEmpty()) {
                        Log.d(TAG, "Etymology retrieved from Internet: " + etymology);

                        // cache

                        ContentValues values = new ContentValues();
                        values.put(KanjiEntry.COL_SYMBOL, kanji.toString());
                        values.put(KanjiEntry.COL_ETYMOLOGY, etymology);

                        checkIfInterrupted();

                        long id = App.getWritableDatabase().insert(KanjiEntry.TABLE, null, values);
                        if (id == -1)
                            Log.e(TAG, "Failed to cache etymology for " + kanji);
                        else
                            Log.d(TAG, "Cached etymology for " + kanji);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Can't retrieve etymology: No Internet connection");
            }
        }

        checkIfInterrupted();

        task.setEtymology(etymology);
    }
}
