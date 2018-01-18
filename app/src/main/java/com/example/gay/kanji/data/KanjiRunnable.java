package com.example.gay.kanji.data;

import com.example.gay.kanji.App;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.example.gay.kanji.data.DataRetriever.NO_DATA;

public class KanjiRunnable extends TaskRunnable {

    String getLoggingTag() { return "KNJ"; }
    String getLoggingData() { return "gif"; }

    KanjiRunnable(DataTask task) {
        super(task);
    }

    @Override
    protected void runInner() throws InterruptedException {
        File extStorage = getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
        String appName = App.getName();
        extStorage = new File(extStorage, appName);

        checkIfInterrupted();

        // TODO check if Storage permission was granted
        // TODO fallback to local storage
        if (!extStorage.exists() && extStorage.mkdirs())
            logd("External storage was created to store", ":", extStorage.getAbsolutePath());

        Character kanji = task.getKanji();
        String gif = prepareKanji(extStorage, kanji);

        checkIfInterrupted();

        task.setGif(gif);
        DataRetriever.update(task);
    }

    private String prepareKanji(File path, Character kanji) throws InterruptedException {
        String filename = kanji + ".gif";
        File file = new File(path, filename);

        if (file.exists()) {
            // TODO refactor cache quering to a separate TaskRunnable
            String absPath = file.getAbsolutePath();
            logd("Found cached");
            return absPath;
        } else {
            return unzip(filename, path);
        }
    }

    private String unzip(String filename, File path) throws InterruptedException {
        if (!path.exists()) {
            loge("No folder to store", "at", path.getAbsolutePath());
            return NO_DATA;
        }

        try {
            checkIfInterrupted();

            InputStream zipFile = App.openKanjiZip();

            try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipFile))) {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    checkIfInterrupted();

                    if (!ze.getName().equals(filename))
                        continue;

                    logd("Found", "in distro zip");

                    File file = new File(path, filename);
                    try (FileOutputStream fout = new FileOutputStream(file)) {
                        while ((count = zis.read(buffer)) != -1) {
                            checkIfInterrupted();
                            fout.write(buffer, 0, count);
                        }
                    }

                    String absPath = file.getAbsolutePath();
                    logd("ZipEntry", "was cached to", absPath);

                    return absPath;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO fallback to downloading from WWWJDIC
        logd("No", "was found in distro zip");

        return NO_DATA;
    }
}
