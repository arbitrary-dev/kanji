package com.example.gay.kanji;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_TEXT;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WebView wv = (WebView) findViewById(R.id.webView1);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl("file:///android_asset/index.html");

        wv.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                // TODO move sharedText up and iterate over it chars until we found one
                // TODO make error when none of sharedText chars exist in DB
                Character kanji = '字';

                Intent intent = getIntent();
                if (    ACTION_SEND.equals(intent.getAction())
                        && "text/plain".equals(intent.getType())) {
                    String sharedText = clean(intent.getStringExtra(EXTRA_TEXT));
                    if (sharedText != null) {
                        Log.d(TAG, "sharedText = \"" + sharedText + "\"");
                        kanji = sharedText.charAt(0);
                    }
                }

                File extStorage = getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
                File path = new File(extStorage, "Kanji"); // TODO get appName resource

                // TODO fallback to local storage
                if (!path.exists() && path.mkdirs())
                    Log.d(TAG, "External storage was created: " + path);

                Log.d(TAG, "Kanji: " + kanji);

                Boolean res = prepareKanji(path, kanji);

                if (res) wv.loadUrl("javascript:updateSrc(\"" + path + "\", '" + kanji + "')");
            }
        });
    }

    /** Cleans all non-japanese symbols from the {@code input} */
    private String clean(String input) {
        return input == null
                ? null
                : input.replaceAll("[^\u3040-\u309f\u30a0-\u30ff\u4e00-\u9faf]", "");
    }

    private Boolean prepareKanji(File path, Character kanji) {
        String filename = kanji + ".gif";
        File file = new File(path, filename);

        if (file.exists()) {
            Log.d(TAG, "Found: " + file);
        } else {
            Log.w(TAG, "Not found: " + file);
            return unzip(filename, path);
        }

        return true;
    }

    private Boolean unzip(String filename, File path) {
        try {
            InputStream zipFile = getAssets().open("kanji.zip");
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipFile));

            try {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    String name = ze.getName();

                    if (!name.equals(filename))
                        continue;

                    Log.d(TAG, "Found ZipEntry(" + name + ")");

                    File file = new File(path, name);
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        while ((count = zis.read(buffer)) != -1)
                            fout.write(buffer, 0, count);
                    } finally {
                        fout.close();
                    }

                    Log.d(TAG, "ZipEntry(" + name + ") was copied to " + file);

                    return true;
                }
            } finally {
                zis.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO fallback to downloading from WWWJDIC
        Log.w(TAG, "No ZipEntry(" + filename + ") was found.");

        return false;
    }
}
