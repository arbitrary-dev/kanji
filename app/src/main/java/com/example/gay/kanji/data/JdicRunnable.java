package com.example.gay.kanji.data;

import com.example.gay.kanji.App;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import static com.example.gay.kanji.KanjiContract.KanjiEntry.COL_KUN;
import static com.example.gay.kanji.KanjiContract.KanjiEntry.COL_MEANING;
import static com.example.gay.kanji.KanjiContract.KanjiEntry.COL_ON;
import static com.example.gay.kanji.data.Data.NO_DATA;

class JdicRunnable extends TaskRunnable {

    private static final String TAG = "JDIC";
    String getLoggingTag() { return TAG; }
    String getLoggingData() { return "info"; }

    JdicRunnable(DataTask task) {
        super(task);
    }

    private static final Object lock = new Object();

    private Document retrieveInfo(Character kanji) throws IOException {
        String url = "http://www.edrdg.org/cgi-bin/wwwjdic/wwwjdic?1D";
        Document doc;

        synchronized (lock) {
            logd("Lookup", "on the web");
            doc = Jsoup.connect(url)
                .data("kanjsel", "X")
                .data("ksrchkey", kanji.toString())
                .post();
        }

        // System.out.println(TAG + " retrieveInfo「" + kanji + "」: "
        //     + doc.outputSettings(doc.outputSettings().prettyPrint(true)).html());
        boolean notFound = doc.toString().matches("Match\\[es]:|No kanji matched this key\\.");
        return notFound ? null : doc;
    }

    @Override
    protected void runInner() throws InterruptedException {
        Character kanji = task.data.kanji;

        // TODO refactor db quering to a separate TaskRunnable
        Db db = Db.getFor(getLoggingTag(), getLoggingData(), kanji);
        String[] data = db.query(COL_ON, COL_KUN, COL_MEANING);
        String on = data[0], kun = data[1], meaning = data[2];

        if (on == null || kun == null || meaning == null) {
            checkIfInterrupted();

            if (App.isConnected()) {
                try {
                    checkIfInterrupted();

                    // retrieve from the web

                    Document doc = retrieveInfo(kanji);

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
                        persist(db, "ON", COL_ON, on);

                        checkIfInterrupted();

                        Element elKun = doc.select("font:contains([訓])").first();
                        kun = values(elKun);
                        persist(db, "KUN", COL_KUN, kun);

                        // TODO name reading

                        checkIfInterrupted();

                        Element elMeaning = doc.select("font:contains([英])").first();
                        meaning = values(elMeaning);
                        // fixes Jim Breen's badass HTML skills
                        if (meaning.contains(";"))
                            meaning = meaning.replace(",", "").replace(';', ',');
                        persist(db, "meaning", COL_MEANING, meaning);

                        if (StringUtil.isBlank(on + kun + meaning))
                            logd("No", "on the web");
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

        // Thread.sleep(2000);
        checkIfInterrupted();

        // TODO idx
        task.data.setOn(on);
        task.data.setKun(kun);
        task.data.setMeaning(meaning);
    }

    private void persist(Db db, String data, String column, String value) throws InterruptedException {
        if (StringUtil.isBlank(value))
            return;
        checkIfInterrupted();
        db.persist(data, column, value);
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
