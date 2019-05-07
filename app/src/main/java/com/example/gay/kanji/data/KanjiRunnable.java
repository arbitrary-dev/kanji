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
import static com.example.gay.kanji.data.Data.NO_DATA;

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

        if (!isExtStorageAvailable()) {
            Log.d(TAG, "External storage is not available");
        } else if (isExtStorageReadOnly()) {
            Log.w(TAG, "External storage is read-only");
        } else if (!checkExtStoragePermissions()) {
            Log.e(TAG, "External storage permissions are not granted");
        } else {
            checkIfInterrupted();
            gif = lookup(task.kanji);
        }

        checkIfInterrupted();

        task.gif = gif;
    }

    private static boolean isExtStorageAvailable() {
        return MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private static boolean isExtStorageReadOnly() {
        return MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    private boolean checkExtStoragePermissions() {
        Context context = App.getContext();
        return ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private String lookup(Character kanji) throws InterruptedException {
        File path = getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
        String appName = App.getName();
        path = new File(path, appName);

        checkIfInterrupted();

        // TODO fallback to local storage
        if (!path.exists() && path.mkdirs())
            Log.d(TAG, "External storage was created at " + path.getAbsolutePath());

        File targetFile = new File(path, kanji + ".gif");
        String result = lookupCached(targetFile);

        if (result.equals(NO_DATA))
            result = lookupDistroZip(targetFile);

        // TODO fallback to downloading from WWWJDIC

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
}
