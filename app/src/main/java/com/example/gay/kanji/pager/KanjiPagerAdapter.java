package com.example.gay.kanji.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.example.gay.kanji.App;

public class KanjiPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "PGAD";

    public KanjiPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        String q = App.getQuery();
        return q == null ? 1 : q.length();
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem: " + position);
        Character kanji = App.getKanjiAt(position);
        return KanjiFragment.newInstance(kanji);
    }
}
