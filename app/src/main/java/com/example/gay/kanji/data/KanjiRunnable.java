package com.example.gay.kanji.data;

import android.util.Log;

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

    private static final String TAG = "KNJ";

    KanjiRunnable(DataTask task) {
        super(task);
    }

    @Override
    protected void runInner() throws InterruptedException {
        File extStorage = getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
        String appName = App.getName();
        Log.d(TAG, "Application name: " + appName);
        extStorage = new File(extStorage, appName);

        checkIfInterrupted();

        // TODO check if Storage permission was granted
        // TODO fallback to local storage
        if (!extStorage.exists() && extStorage.mkdirs())
            Log.d(TAG, "External storage was created: " + extStorage);

        Character kanji = task.getKanji();
        Log.d(TAG, "Kanji: " + kanji);

        String gif = prepareKanji(extStorage, kanji);

        checkIfInterrupted();

        task.setGif(gif);
    }

    private String prepareKanji(File path, Character kanji) throws InterruptedException {
        String filename = kanji + ".gif";
        File file = new File(path, filename);

        if (file.exists()) {
            String absPath = file.getAbsolutePath();
            Log.d(TAG, "Found: " + absPath);
            return absPath;
        } else {
            Log.d(TAG, "Not found: " + file);
            return unzip(filename, path);
        }
    }

    private String unzip(String filename, File path) throws InterruptedException {
        if (!path.exists()) {
            Log.e(TAG, "Folder doesn't exists: " + path.getAbsolutePath());
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

                    Log.d(TAG, "Found ZipEntry(" + filename + ")");

                    File file = new File(path, filename);
                    try (FileOutputStream fout = new FileOutputStream(file)) {
                        while ((count = zis.read(buffer)) != -1) {
                            checkIfInterrupted();
                            fout.write(buffer, 0, count);
                        }
                    }

                    String absPath = file.getAbsolutePath();
                    Log.d(TAG, "ZipEntry(" + filename + ") was copied to " + absPath);

                    return absPath;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO fallback to downloading from WWWJDIC
        Log.w(TAG, "No ZipEntry(" + filename + ") was found.");

        return NO_DATA;
    }
}
