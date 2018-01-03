package com.example.gay.kanji.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Process;
import android.util.Log;

import com.example.gay.kanji.App;
import com.example.gay.kanji.KanjiContract.KanjiEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

class EtymologyRunnable implements Runnable {

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

    public void run() {

        // setup

        // TODO handle interruption
        task.setThreadEtymology(Thread.currentThread());
        Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);

        // query from cache

        Cursor cursor = null;
        Character kanji = task.getKanji();
        String etymology = null;

        try {
            cursor = App.getReadableDatabase().query(
                KanjiEntry.TABLE,
                new String[] { KanjiEntry.COL_ETYMOLOGY },
                KanjiEntry.COL_SYMBOL + " = ?",
                new String[] { kanji.toString() },
                null, null, null,
                "1"
            );

            if (cursor.moveToNext()) {
                int colEtymology = cursor.getColumnIndex(KanjiEntry.COL_ETYMOLOGY);
                etymology = cursor.getString(colEtymology);
                Log.d(TAG, "Etymology retrieved from cache: " + etymology);
            } else {
                Log.d(TAG, "No cached etymology for " + kanji);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        // retrieve from web

        if (etymology == null) {
            if (App.isConnected()) {
                try {
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

        task.setEtymology(etymology);
    }
}
