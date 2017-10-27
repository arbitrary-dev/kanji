package com.example.gay.kanji;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.gay.kanji.KanjiContract.KanjiEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_TEXT;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.view.View.VISIBLE;

public class KanjiActivity extends AppCompatActivity {

    private static final String TAG = "KanjiActivity";
    private static final String PREF_NIGHT_MODE = "nightMode";

    private boolean mNightMode;
    private WebView mWebView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem nightDayItem = menu.findItem(R.id.night_day_mode);
        if (mNightMode) {
            nightDayItem.setIcon(R.drawable.ic_brightness_5_white_24dp);
            nightDayItem.setTitle(R.string.day_mode);
        } else {
            nightDayItem.setIcon(R.drawable.ic_brightness_3_white_24dp);
            nightDayItem.setTitle(R.string.night_mode);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getPreferences(0);
        mNightMode = settings.getBoolean(PREF_NIGHT_MODE, false);
        setTheme(mNightMode ? R.style.AppThemeNight : R.style.AppThemeDay);

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mWebView = (WebView) findViewById(R.id.webView1);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/index-" + (mNightMode ? "night" : "day") + ".html");

        // TODO move to KanjiWebView
        mWebView.setWebViewClient(new WebViewClient(){
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

                // TODO check if Storage permission was granted
                // TODO fallback to local storage
                if (!path.exists() && path.mkdirs())
                    Log.d(TAG, "External storage was created: " + path);

                Log.d(TAG, "Kanji: " + kanji);

                Boolean res = prepareKanji(path, kanji);

                if (res) {
                    mWebView.loadUrl("javascript:init(\"" + path + "\", '" + kanji + "')");
                    mWebView.setVisibility(VISIBLE);

                    etymology(kanji);
                }
            }
        });
    }

    private void etymology(Character kanji) {
        // TODO refactor into separate headless Fragment
        new GetEtymologyTask().execute(kanji);
    }

    private class GetEtymologyTask extends AsyncTask<Character, Void, String> {

        private static final String SERVICE_LINK = "http://www.chineseetymology.org/CharacterEtymology.aspx?characterInput=";

        private KanjiDbHelper dbHelper = new KanjiDbHelper(getApplicationContext());
        private SQLiteDatabase db = dbHelper.getWritableDatabase();

        private String getFromCache(Character kanji) {
            String result = null;
            Cursor cursor = null;

            try {
                cursor = db.query(
                    KanjiEntry.TABLE,
                    new String[] { KanjiEntry.COL_ETYMOLOGY },
                    KanjiEntry.COL_SYMBOL + " = ?",
                    new String[] { kanji.toString() },
                    null, null, null,
                    "1"
                );

                if (cursor.moveToNext()) {
                    int colEtymology = cursor.getColumnIndex(KanjiEntry.COL_ETYMOLOGY);
                    result = cursor.getString(colEtymology);
                    Log.d(TAG, "Etymology retrieved from cache: " + result);
                } else {
                    Log.d(TAG, "No cached etymology for " + kanji);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }

            return result;
        }

        private void putToCache(Character kanji, String etymology) {
            ContentValues values = new ContentValues();
            values.put(KanjiEntry.COL_SYMBOL, kanji.toString());
            values.put(KanjiEntry.COL_ETYMOLOGY, etymology);

            db.insert(KanjiEntry.TABLE, null, values);
            Log.d(TAG, "Cached etymology for " + kanji);
        }

        private boolean isConnected() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if (ni == null || !ni.isConnected())
                return false;

            int type = ni.getType();
            return type == TYPE_WIFI || type == TYPE_MOBILE;
        }

        @Override
        protected String doInBackground(Character... params) {
            Character kanji = params[0];
            String result = getFromCache(kanji);

            if (result == null) {
                if (isConnected()) {
                    try {
                        Document doc = Jsoup.connect(SERVICE_LINK + kanji).get();
                        // TODO integration test
                        Elements es = doc.select("#etymologyLabel p");

                        result = es.text().trim();

                        if (!result.isEmpty()) {
                            Log.d(TAG, "Etymology retrieved from Internet: " + result);
                            putToCache(kanji, result);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Can't retrieve etymology: No Internet connection");
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            dbHelper.close();
            if (result != null && !result.isEmpty())
                mWebView.loadUrl("javascript:etymology(\"" + result + "\")");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.night_day_mode:
                getPreferences(0).edit().putBoolean(PREF_NIGHT_MODE, !mNightMode).apply();
                Log.d(TAG, "onOptionsItemSelected setNightMode(" + !mNightMode + ")");
                recreate();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
            Log.d(TAG, "Not found: " + file);
            return unzip(filename, path);
        }

        return true;
    }

    private Boolean unzip(String filename, File path) {
        if (!path.exists()) {
            Log.e(TAG, "Folder doesn't exists: " + path.getAbsolutePath());
            return false;
        }

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
