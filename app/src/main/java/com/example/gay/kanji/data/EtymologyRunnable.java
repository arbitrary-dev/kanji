package com.example.gay.kanji.data;

import com.example.gay.kanji.App;
import com.example.gay.kanji.KanjiContract.KanjiEntry;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

import static com.example.gay.kanji.data.Data.NO_DATA;
import static org.jsoup.Connection.Method.HEAD;

class EtymologyRunnable extends TaskRunnable {

    private static final String TAG = "ETYM";
    String getLoggingTag() { return TAG; }
    String getLoggingData() { return "etymology"; }

    private static final Object lock = new Object();
    private static Map<String, String> cookies;

    String retrieveEtymology(Character kanji) throws IOException {
        String site = "http://hanziyuan.net";
        String etymPath = "/etymology";

        Document doc;
        synchronized (lock) {
            logd("Lookup", "on the web");

            if (cookies == null) {
                Response res = Jsoup.connect(site).method(HEAD).execute();
                cookies = res.cookies();
            }

            doc = Jsoup
                .connect(site + etymPath)
                .data("chinese", kanji.toString())
                .data("Bronze", cookies.get("Bronze"))
                .header("Accept-Encoding", "gzip, deflate")
                .header("Referer", site)
                .header("Chinese", "" + ((int) kanji))
                .cookies(cookies)
                .post();
        }

        // System.out.println(TAG + " retrieveEtymology「" + kanji + "」:\n"
        //     + "COOKIES" + Arrays.toString(cookies.entrySet().toArray()).replaceAll("[]\\[,] ?", "\n")
        //     + "\nDOC\n" + doc.outputSettings(doc.outputSettings().prettyPrint(true)).html());
        Elements es = doc.select(
            "p:matches((?i)(decomposition|meaning|english).+(?<!none|not applicable.)$)");
        es.select("b").remove();

        return es.text().trim();
    }

    EtymologyRunnable(DataTask task) {
        super(task);
    }

    @Override
    protected void runInner() throws InterruptedException {
        Character kanji = task.getKanji();

        // TODO refactor cache quering to a separate TaskRunnable
        Db db = Db.getFor(getLoggingTag(), getLoggingData(), kanji);
        String etymology = db.query(KanjiEntry.COL_ETYMOLOGY)[0];

        if (etymology == null) {
            checkIfInterrupted();

            if (App.isConnected()) {
                try {
                    checkIfInterrupted();

                    etymology = retrieveEtymology(kanji);

                    if (etymology.matches(".*[a-zA-Z].*")) {
                        logd("Retrieved", "from the web:", etymology);
                        db.persist(KanjiEntry.COL_ETYMOLOGY, etymology);
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
        task.updateUi();
    }
}
