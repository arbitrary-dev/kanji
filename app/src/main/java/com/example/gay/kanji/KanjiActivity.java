package com.example.gay.kanji;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.gay.kanji.data.DataRetriever;

// FIXME text selection block style should match nightmode
// and do not offset main layout.
public class KanjiActivity extends AppCompatActivity {

    private static final String TAG = "ACTV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setTheme(App.isNightMode() ? R.style.AppThemeNight : R.style.AppThemeDay);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // TODO text editing button

        MenuItem nightDayItem = menu.findItem(R.id.night_day_mode);
        if (App.isNightMode()) {
            nightDayItem.setIcon(R.drawable.ic_brightness_5_white_24dp);
            nightDayItem.setTitle(R.string.day_mode);
        } else {
            nightDayItem.setIcon(R.drawable.ic_brightness_3_white_24dp);
            nightDayItem.setTitle(R.string.night_mode);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.night_day_mode:
                // TODO preserve view state on night mode and layout switch
                // View state is info expansion and gif animation.
                App.toggleNightMode();
                recreate();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        DataRetriever.stop();
        App.closeDatabase();
        super.onDestroy();
    }
}
