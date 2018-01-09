package com.example.gay.kanji.data;

import android.util.Log;

import com.example.gay.kanji.App;
import com.example.gay.kanji.KanjiContract.KanjiEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import static com.example.gay.kanji.data.DataRetriever.NO_DATA;

class JdicRunnable extends TaskRunnable {

    private static final String TAG = "JDIC";
    private static final String DATA = "info";

    JdicRunnable(DataTask task) {
        super(task);
    }

    private static Document post(Character kanji) throws IOException {
        Log.d(TAG, "Lookup 「" + kanji + "」 " + DATA + " on web");
        String url = "http://www.edrdg.org/cgi-bin/wwwjdic/wwwjdic?1D";
        Document doc =
            Jsoup.connect(url)
                 .data("kanjsel", "X")
                 .data("ksrchkey", kanji.toString())
                 .post();
        boolean notFound = doc.toString().contains("Try again?");
        return notFound ? null : doc;
    }

    @Override
    protected void runInner() throws InterruptedException {
        Character kanji = task.getKanji();

        Cache cache = Cache.getFor(TAG, DATA, kanji);
        String[] cached = cache.query(KanjiEntry.COL_ON, KanjiEntry.COL_KUN, KanjiEntry.COL_MEANING);
        String on = cached[0], kun = cached[1], meaning = cached[2];

        if (on == null || kun == null || meaning == null) {
            checkIfInterrupted();

            if (App.isConnected()) {
                try {
                    checkIfInterrupted();

                    // retrieve from web

                    Document doc = post(kanji);

                    if (doc == null) {
                        Log.d(TAG, "No " + DATA + " for 「" + kanji + "」 on web");
                    } else {
                        // TODO idx

                        checkIfInterrupted();

                        Element elOn = doc.select("font:contains([音])").first();
                        on = values(elOn);

                        checkIfInterrupted();

                        Element elKun = doc.select("font:contains([訓])").first();
                        kun = values(elKun);

                        // TODO name reading

                        checkIfInterrupted();

                        Element elMeaning = doc.select("font:contains([英])").first();
                        meaning = values(elMeaning);
                        // fix Jim Breen's badass HTML skills
                        if (meaning.contains(";"))
                            meaning = meaning.replace(",", "").replace(';', ',');

                        Log.d(TAG, String.format(
                            "Retrieved " + DATA + " from web: ON = %s; KUN = %s; meaning = %s",
                            on, kun, meaning
                        ));

                        cache.put(
                            KanjiEntry.COL_ON, on,
                            KanjiEntry.COL_KUN, kun,
                            KanjiEntry.COL_MEANING, meaning
                        );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Can't retrieve " + DATA + ": No Internet connection");
                on = NO_DATA;
                kun = NO_DATA;
                meaning = NO_DATA;
            }
        }

        checkIfInterrupted();

        // TODO idx
        task.setOn(on);
        task.setKun(kun);
        task.setMeaning(meaning);
        DataRetriever.update(task);
    }

    private String values(Element el) {
        if (el == null)
            return NO_DATA;

        StringBuilder sb = new StringBuilder();
        String txt;

        while ( (el = el.nextElementSibling()) != null
                && el.tagName().equals("b")
                && !(txt = el.text().trim()).isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(txt);
        }

        return sb.toString();
    }
}
