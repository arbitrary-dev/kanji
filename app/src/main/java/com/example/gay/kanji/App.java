package com.example.gay.kanji;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

public class App extends Application {

    private static final String TAG = "APP";
    public static final String JAP_CHAR_RANGE = "\\u3040-\\u309f\\u30a0-\\u30ff\\u4e00-\\u9faf";

    private static App instance;
    private static KanjiDbHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        instance = this;
        dbHelper = new KanjiDbHelper(getApplicationContext());
    }

    public static SQLiteDatabase getReadableDatabase() {
        return dbHelper.getReadableDatabase();
    }

    public static SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }

    public static void closeDatabase() {
        dbHelper.close();
    }

    public static boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)
            instance.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;

        if (ni == null || !ni.isConnected())
            return false;

        int type = ni.getType();
        return type == TYPE_WIFI || type == TYPE_MOBILE;
    }

    public static String getName() {
        ApplicationInfo appInfo = instance.getApplicationContext().getApplicationInfo();
        return instance.getPackageManager().getApplicationLabel(appInfo).toString();
    }

    public static InputStream openKanjiZip() throws IOException {
        return instance.getAssets().open("kanji.zip");
    }

    private static final String PREF_NIGHT_MODE = "nightMode";

    public static boolean isNightMode() {
        String name = App.class.getName();
        SharedPreferences settings = instance.getSharedPreferences(name, MODE_PRIVATE);
        return settings.getBoolean(PREF_NIGHT_MODE, false);
    }

    public static void toggleNightMode() {
        String name = App.class.getName();
        SharedPreferences settings = instance.getSharedPreferences(name, MODE_PRIVATE);
        boolean result = !isNightMode();
        settings.edit().putBoolean(PREF_NIGHT_MODE, result).apply();
        Log.d(TAG, "nightMode toggled to " + result);
    }

    private static final Character DEFAULT_KANJI = '字';
    private static String query;

    /**
     * @param position within {@link #query}
     * @return kanji pointed by {@code position} within {@link #query}
     *         or {@link #DEFAULT_KANJI} if {@link #query} is {@code null}
     */
    public static Character getKanjiAt(int position) {
        return query == null ? DEFAULT_KANJI : query.charAt(position);
    }

    // TODO javadoc
    /** Cleans all non-japanese symbols from the {@code input} */
    private static String clean(String input) {
        if (input == null) return null;
        String q = input.replaceAll("[^" + JAP_CHAR_RANGE + "]", "");
        return q.isEmpty() ? null : q;
    }

    /** @return {@link #query} */
    public static String getQuery() {
        return query;
    }

    /**
     * Sets app {@code query} string and resets queryPosition to 0 within this string
     * which points to a currently viewed kanji
     *
     * @param query string displayed in {@link android.support.v7.widget.SearchView SearchView}
     */
    public static void setQuery(String query) {
        App.query = clean(query);
        Log.d(TAG, "setQuery: \"" + App.query + "\"");
    }
}
