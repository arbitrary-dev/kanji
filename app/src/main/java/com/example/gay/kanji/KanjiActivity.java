package com.example.gay.kanji;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.gay.kanji.pager.KanjiPagerAdapter;

import java.lang.reflect.Field;

import static android.content.Intent.EXTRA_TEXT;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

// FIXME text selection block style should match nightmode
// and do not offset main layout.
public class KanjiActivity extends AppCompatActivity {

    private static final String TAG = "ACTV";

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        // Theme should be set before calling super.onCreate()
        // Otherwise, you'll get:
        // java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity.
        // It happens when Fragments got recreated.
        // TODO android.support.v7.app.AppCompatDelegate.setLocalNightMode()
        setTheme(App.isNightMode() ? R.style.AppThemeNight : R.style.AppThemeDay);

        super.onCreate(savedInstanceState);

        Log.d(TAG, "setContentView() start");
        setContentView(R.layout.activity_main);
        Log.d(TAG, "setContentView() end");

        App.setQuery(getIntent().getStringExtra(EXTRA_TEXT));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateToolbarTitle();

        // FIXME pager is black on switching nightMode off
        //       But when just opening app it behaves nice.
        //       Maybe this is the culprit?
        //       E/ViewRootImpl: sendUserActionEvent() mView == null
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                updateToolbarTitle();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
        resetPager();

        // TODO request ext storage permissions
    }

    private void resetPager() {
        KanjiPagerAdapter pagerAdapter = new KanjiPagerAdapter(getSupportFragmentManager());
        if (mViewPager != null)
            mViewPager.setAdapter(pagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Search

        final MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                App.setQuery(query);
                resetPager();
                updateToolbarTitle();
                searchItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        try {
            // Nasty shit to style hint, but it seems to be no other way around
            Field f = SearchView.class.getDeclaredField("mSearchHintIcon");
            f.setAccessible(true);
            Resources r = getResources();
            // Mutation prevents d.setAlpha() from being applied to menu search icon also (Issue #6)
            Drawable d = r.getDrawable(R.drawable.ic_search_white_24dp, null).mutate();
            d.setAlpha(153);
            f.set(searchView, d);
            searchView.setQueryHint(r.getString(R.string.app_name));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageView mCloseButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        mCloseButton.getDrawable().setAlpha(153);

        // Night mode

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

    private void updateToolbarTitle() {
        String q = App.getQuery();

        if (q == null)
            return;

        TypedValue typedValue = new TypedValue();
        TypedArray a = obtainStyledAttributes(
            typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();

        SpannableStringBuilder title = new SpannableStringBuilder(q);
        int p = mViewPager.getCurrentItem();
        title.setSpan(new ForegroundColorSpan(color), p, p + 1, SPAN_EXCLUSIVE_EXCLUSIVE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(title);
    }

    private static final String STATE_QUERY = "state_query";
    private static final String STATE_QUERY_POSITION = "state_query_position";

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.d(TAG, "onSaveInstanceState()");
        String q = App.getQuery();
        if (q != null) {
            state.putString(STATE_QUERY, q);
            state.putInt(STATE_QUERY_POSITION, mViewPager.getCurrentItem());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        Log.d(TAG, "onRestoreInstanceState()");
        String q = state.getString(STATE_QUERY);
        if (q != null) {
            App.setQuery(q);
            resetPager();
            mViewPager.setCurrentItem(state.getInt(STATE_QUERY_POSITION), false);
            updateToolbarTitle();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        App.closeDatabase();
        super.onDestroy();
    }
}
