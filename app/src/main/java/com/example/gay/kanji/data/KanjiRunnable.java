package com.example.gay.kanji.data;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.gay.kanji.App;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.MEDIA_MOUNTED_READ_ONLY;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.example.gay.kanji.data.DataRetriever.NO_DATA;

public class KanjiRunnable extends TaskRunnable {

    private static final String TAG = "KNJ";
    String getLoggingTag() { return TAG; }
    String getLoggingData() { return "gif"; }

    KanjiRunnable(DataTask task) {
        super(task);
    }

    @Override
    protected void runInner() throws InterruptedException {
        String gif = NO_DATA;

        if (!isExtStorageAvailable())
            Log.d(TAG, "External storage is not available");
        else if (isExtStorageReadOnly())
            Log.w(TAG, "External storage is read-only");
        else if (!checkExtStoragePermissions())
            Log.e(TAG, "External storage permissions are not granted");
        else {
            checkIfInterrupted();

            File path = getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
            String appName = App.getName();
            path = new File(path, appName);

            checkIfInterrupted();

            // TODO fallback to local storage
            if (!path.exists() && path.mkdirs())
                Log.d(TAG, "External storage was created at " + path.getAbsolutePath());

            Character kanji = task.getKanji();
            gif = prepareKanji(path, kanji);
        }

        checkIfInterrupted();

        task.setGif(gif);
        DataRetriever.update(task);
    }

    private static boolean isExtStorageAvailable() {
        return MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private static boolean isExtStorageReadOnly() {
        return MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    private boolean checkExtStoragePermissions() {
        Context ctx = task.getWebView().getContext();
        return ContextCompat.checkSelfPermission(ctx, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(ctx, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
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
