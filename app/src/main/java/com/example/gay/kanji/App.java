package com.example.gay.kanji;

import android.app.Application;
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
}
