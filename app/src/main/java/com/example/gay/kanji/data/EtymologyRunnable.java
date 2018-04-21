package com.example.gay.kanji.data;

import com.example.gay.kanji.App;
import com.example.gay.kanji.KanjiContract.KanjiEntry;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

import static com.example.gay.kanji.data.DataRetriever.NO_DATA;
import static org.jsoup.Connection.Method.HEAD;

class EtymologyRunnable extends TaskRunnable {

    String getLoggingTag() { return "ETYM"; }
    String getLoggingData() { return "etymology"; }

    private static final Object cookiesLock = new Object();
    private static Map<String, String> cookies;

    // TODO integration test
    private static Document retrieveData(Character kanji) throws IOException {
        String site = "http://hanziyuan.net";
        String etymPath = "/etymology";

        if (cookies == null)
            synchronized (cookiesLock) {
                if (cookies == null) {
                    Response res = Jsoup.connect(site).method(HEAD).execute();
                    cookies = res.cookies();
                }
            }

        return Jsoup
            .connect(site + etymPath)
            .data("chinese", kanji.toString())
            .data("Bronze", cookies.get("Bronze"))
            .header("Accept-Encoding", "gzip, deflate")
            .header("Referer", site)
            .cookies(cookies)
            .post();
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

                    logd("Lookup", "on the web");
                    Document doc = retrieveData(kanji);
                    Elements es = doc.select("p:matches((decomposition|meaning).+(?<!none)$)");
                    es.select("b").remove();

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
