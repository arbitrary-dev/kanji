package com.example.gay.kanji.data;

import com.example.gay.kanji.App;
import com.example.gay.kanji.KanjiContract.KanjiEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import static com.example.gay.kanji.data.DataRetriever.NO_DATA;

class EtymologyRunnable extends TaskRunnable {

    String getLoggingTag() { return "ETYM"; }
    String getLoggingData() { return "etymology"; }

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
        Cache cache = Cache.getFor(getLoggingTag(), getLoggingData(), kanji);
        String etymology = cache.query(KanjiEntry.COL_ETYMOLOGY)[0];

        if (etymology == null) {
            checkIfInterrupted();

            if (App.isConnected()) {
                try {
                    checkIfInterrupted();

                    // retrieve from the web

                    String url = url(kanji);
                    logd("Lookup", "on the web");
                    Document doc = Jsoup.connect(url).get();
                    // TODO integration test
                    Elements es = doc.select("#etymologyLabel p");

                    etymology = es.text().trim();

                    if (etymology.matches(".*[a-zA-Z].*")) {
                        logd("Retrieved", "from the web:", etymology);
                        cache.put(KanjiEntry.COL_ETYMOLOGY, etymology);
                    } else {
                        logd("No", "on the web");
                        etymology = NO_DATA;
                    }
                } catch (IOException e) {
                    loge("Unable to retrieve", ":", e.getMessage());
                    e.printStackTrace();
                    etymology = NO_DATA;
                }
            } else {
                logd("Can't retrieve", ": No Internet connection");
                etymology = NO_DATA;
            }
        }

        checkIfInterrupted();

        task.setEtymology(etymology);
        DataRetriever.update(task);
    }
}
