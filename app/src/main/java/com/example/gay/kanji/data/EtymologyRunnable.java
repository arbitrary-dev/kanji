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

import static com.example.gay.kanji.data.DataRetriever.NO_DATA;

class EtymologyRunnable extends TaskRunnable {

    private static final String TAG = "ETYM";

    private static String url(Character kanji) {
        return "http://www.chineseetymology.org/CharacterEtymology.aspx?characterInput=" + kanji;
    }

    EtymologyRunnable(DataTask task) {
        super(task);
    }

    @Override
    protected void runInner() throws InterruptedException {

        // query from cache

        Character kanji = task.getKanji();
        String etymology = null;

        checkIfInterrupted();

        Log.d(TAG, "Lookup 「" + kanji + "」 etymology in cache");
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

                    String url = url(kanji);
                    Log.d(TAG, "Lookup 「" + kanji + "」 etymology at " + url);
                    Document doc = Jsoup.connect(url).get();
                    // TODO integration test
                    Elements es = doc.select("#etymologyLabel p");

                    etymology = es.text().trim();

                    if (!etymology.isEmpty()) {
                        Log.d(TAG, "Etymology retrieved from web: " + etymology);

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
                etymology = NO_DATA;
            }
        }

        checkIfInterrupted();

        task.setEtymology(etymology);
        DataRetriever.update(task);
    }
}
