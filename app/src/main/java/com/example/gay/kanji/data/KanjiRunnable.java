package com.example.gay.kanji.data;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.gay.kanji.App;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.example.gay.kanji.data.Data.NO_DATA;

public class KanjiRunnable extends TaskRunnable {

    private static final String TAG = "KNJ";

    String getLoggingTag() { return TAG; }

    String getLoggingData() { return "gif"; }

    KanjiRunnable(DataTask task) {
        super(task);
    }

    private static final Object lock = new Object();

    @Override
    protected void runInner() throws InterruptedException {
        checkIfInterrupted();
        String gif = lookup(task.kanji);
        checkIfInterrupted();
        task.gif = gif;
    }

    private String lookup(Character kanji) throws InterruptedException {
        checkIfInterrupted();

        File targetFile = new File(App.getContext().getCacheDir(), kanji + ".gif");
        String result = lookupCached(targetFile);

        if (result.equals(NO_DATA))
            result = lookupDistroZip(targetFile);

        if (result.equals(NO_DATA))
            result = lookupOnline(kanji, targetFile);

        return result;
    }

    private String lookupCached(File targetFile) {
        if (targetFile.exists()) {
            String absPath = targetFile.getAbsolutePath();
            logd("Found cached");
            return absPath;
        } else {
            return NO_DATA;
        }
    }

    private void checkParentDirectory(File targetFile) {
        File parent = targetFile.getParentFile();
        if (!parent.exists()) {
            String path = parent.getAbsolutePath();
            loge("No folder to store", "at", path);
            throw new RuntimeException("Target path unavailable at " + path);
        }
    }

    private String lookupDistroZip(File targetFile) throws InterruptedException {
        checkParentDirectory(targetFile);

        try {
            checkIfInterrupted();

            InputStream zipFile = App.openKanjiZip();

            try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipFile))) {
                ZipEntry ze;
                int count;
                while ((ze = zis.getNextEntry()) != null) {
                    checkIfInterrupted();

                    if (!ze.getName().equals(targetFile.getName()))
                        continue;

                    logd("Found", "in distro zip");

                    try (FileOutputStream fout = new FileOutputStream(targetFile)) {
                        byte[] buffer = new byte[8192];
                        while ((count = zis.read(buffer)) != -1) {
                            checkIfInterrupted();
                            fout.write(buffer, 0, count);
                        }
                    }

                    String absPath = targetFile.getAbsolutePath();
                    logd("ZipEntry", "was cached to", absPath);

                    return absPath;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logd("No", "was found in distro zip");

        return NO_DATA;
    }

    private Document retrieveInfo(Character kanji) throws IOException {
        String url = "http://www.edrdg.org/cgi-bin/wwwjdic/wwwjdic?1D";
        Document doc;

        synchronized (lock) {
            logd("Lookup", "on the web");
            doc = Jsoup.connect(url)
                .validateTLSCertificates(false) // For old devices with outdated trust store
                .data("kanjsel", "X")
                .data("ksrchkey", kanji.toString())
                .post();
        }

        boolean notFound = doc.toString().matches("Match\\[es]:|No kanji matched this key\\.");
        return notFound ? null : doc;
    }

    // TODO refactor
    // Better to have separate runnable for kanji downloading.
    // We could retrieve `idx` as part of `JdicRunnable` and wait until `KanjiRunnable` completes
    // without result and only then enque our `KanjiDownloadRunnable`.
    private String lookupOnline(Character kanji, File targetFile) throws InterruptedException {
        checkParentDirectory(targetFile);
        if (App.isConnected()) {
            try {
                checkIfInterrupted();
                Document doc = retrieveInfo(kanji);
                if (doc == null) {
                    logd("No", "on the web");
                } else {
                    Element el = doc.select("td:contains(Halpern NJECD Index)").next().first();
                    if (el == null) {
                        return NO_DATA;
                    } else {
                        @SuppressLint("DefaultLocale")
                        String idx = String.format("%04d", Integer.valueOf(el.text().trim()));
                        URL downloadUrl = new URL("https://www.edrdg.org/cgi-bin/wwwjdic/dispgif?" + idx);
                        logd("Will try to download", "from: " + downloadUrl);
                        long transferred = 0L;
                        checkIfInterrupted();
                        try (
                            ReadableByteChannel rbc = Channels.newChannel(downloadUrl.openStream());
                            FileChannel fc = new FileOutputStream(targetFile).getChannel()
                        ) {
                            long prev;
                            do {
                                checkIfInterrupted();
                                prev = transferred;
                                transferred += fc.transferFrom(rbc, transferred, 64 * 1024); // 64 kb
                                Log.d(TAG, (transferred - prev) + "b");
                            } while (transferred != prev); // Not sure if this is going to work in ALL cases
                        }
                        // Any GIF that is lower than 128b in size is deemed to be
                        // empty square GIF that WWWJDIC returns as 404.
                        if (transferred > 128L) {
                            String absPath = targetFile.getAbsolutePath();
                            logd("Downloaded", "was cached to", absPath);
                            return absPath;
                        } else {
                            return NO_DATA;
                        }
                    }
                }
            } catch (IOException e) {
                loge("Unable to retrieve", ":", e.getMessage());
                e.printStackTrace();
            }
        } else {
            logd("Can't retrieve", ": No Internet connection");
        }

        return NO_DATA;
    }
}
