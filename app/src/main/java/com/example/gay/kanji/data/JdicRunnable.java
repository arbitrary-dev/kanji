package com.example.gay.kanji.data;

import com.example.gay.kanji.App;
import com.example.gay.kanji.KanjiContract.KanjiEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import static com.example.gay.kanji.data.DataRetriever.NO_DATA;

class JdicRunnable extends TaskRunnable {

    String getLoggingTag() { return "JDIC"; }
    String getLoggingData() { return "info"; }

    JdicRunnable(DataTask task) {
        super(task);
    }

    private static Document post(Character kanji) throws IOException {
        String url = "http://www.edrdg.org/cgi-bin/wwwjdic/wwwjdic?1D";
        Document doc =
            Jsoup.connect(url)
                 .data("kanjsel", "X")
                 .data("ksrchkey", kanji.toString())
                 .post();
        boolean notFound = doc.toString().contains("No kanji matched this key.");
        return notFound ? null : doc;
    }

    @Override
    protected void runInner() throws InterruptedException {
        Character kanji = task.getKanji();

        // TODO refactor cache quering to a separate TaskRunnable
        Cache cache = Cache.getFor(getLoggingTag(), getLoggingData(), kanji);
        String[] cached = cache.query(KanjiEntry.COL_ON, KanjiEntry.COL_KUN, KanjiEntry.COL_MEANING);
        String on = cached[0], kun = cached[1], meaning = cached[2];

        if (on == null || kun == null || meaning == null) {
            checkIfInterrupted();

            if (App.isConnected()) {
                try {
                    checkIfInterrupted();

                    // retrieve from the web

                    logd("Lookup", "on the web");
                    Document doc = post(kanji);

                    if (doc == null) {
                        logd("No", "on the web");
                        on = NO_DATA;
                        kun = NO_DATA;
                        meaning = NO_DATA;
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
                        // fixes Jim Breen's badass HTML skills
                        if (meaning.contains(";"))
                            meaning = meaning.replace(",", "").replace(';', ',');

                        logd("Retrieved", String.format(
                            "from the web: ON = %s; KUN = %s; meaning = %s",
                            on, kun, meaning
                        ));

                        cache.put(
                            KanjiEntry.COL_ON, on,
                            KanjiEntry.COL_KUN, kun,
                            KanjiEntry.COL_MEANING, meaning
                        );
                    }
                } catch (IOException e) {
                    loge("Unable to retrieve", ":", e.getMessage());
                    e.printStackTrace();
                    on = NO_DATA;
                    kun = NO_DATA;
                    meaning = NO_DATA;
                }
            } else {
                logd("Can't retrieve", ": No Internet connection");
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
        task.updateUi();
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
