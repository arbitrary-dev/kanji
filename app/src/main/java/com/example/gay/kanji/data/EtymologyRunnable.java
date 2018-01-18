package com.example.gay.kanji.data;

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
    private static final String DATA = "etymology";

    private static String url(Character kanji) {
        return "http://www.chineseetymology.org/CharacterEtymology.aspx?characterInput=" + kanji;
    }

    EtymologyRunnable(DataTask task) {
        super(task);
    }

    @Override
    protected void runInner() throws InterruptedException {
        Character kanji = task.getKanji();

        // TODO refactor cache quering to a separate TaskRunnable
        Cache cache = Cache.getFor(TAG, DATA, kanji);
        String etymology = cache.query(KanjiEntry.COL_ETYMOLOGY)[0];

        if (etymology == null) {
            checkIfInterrupted();

            if (App.isConnected()) {
                try {
                    checkIfInterrupted();

                    // retrieve from the web

                    String url = url(kanji);
                    Log.d(TAG, "Lookup 「" + kanji + "」 " + DATA + " on the web");
                    Document doc = Jsoup.connect(url).get();
                    // TODO integration test
                    Elements es = doc.select("#etymologyLabel p");

                    etymology = es.text().trim();

                    if (etymology.matches(".*[a-zA-Z].*")) {
                        Log.d(TAG, "Retrieved " + DATA + " from the web: " + etymology);
                        cache.put(KanjiEntry.COL_ETYMOLOGY, etymology);
                    } else {
                        Log.w(TAG, "No " + DATA + " for 「" + kanji + "」 on the web");
                        etymology = NO_DATA;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    etymology = NO_DATA;
                }
            } else {
                Log.w(TAG, "Can't retrieve " + DATA + ": No Internet connection");
                etymology = NO_DATA;
            }
        }

        checkIfInterrupted();

        task.setEtymology(etymology);
        DataRetriever.update(task);
    }
}
