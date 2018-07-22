package com.example.gay.kanji.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.example.gay.kanji.App;
import com.example.gay.kanji.data.Cache;
import com.example.gay.kanji.data.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class KanjiPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "PGAD";

    public KanjiPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        StringBuilder q = new StringBuilder(App.getQuery());
        int L = q.length();
        for (int i = 0; i < q.length(); ++i) {
            Character c = q.charAt(i);
            Data data = Cache.get(c);
            if (data.isEmpty()) {
                q.setCharAt(i, '_');
                --L;
            }
        }
        Log.d(TAG, "getCount: " + L + " " + q);
        return L == 0 ? 1 : L;
    }

    @Override
    public int getItemPosition(Object object) {
        KanjiFragment frag = (KanjiFragment) object;
        Character kanji = frag.getKanji();
        int newPos = POSITION_NONE;

        Data cached = Cache.get(kanji);
        boolean isEmpty = cached != null && cached.isEmpty();
        if (isEmpty)
            return newPos;

        int pos = 0;
        int oldPos = frag.getPosition();
        String q = App.getQuery();
        for (int i = 0; i < q.length(); ++i) {
            Character c = q.charAt(i);
            Data data = Cache.get(c);
            if (!data.isEmpty()) {
                if (kanji.equals(c)) newPos = pos;
                ++pos;
            }
            if (pos > oldPos) break;
        }

        if (newPos != oldPos && newPos != POSITION_NONE) {
            // Nasty hack to move Fragment for parent
            try {
                Field f = FragmentStatePagerAdapter.class.getDeclaredField("mFragments");
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                ArrayList<Fragment> fs = (ArrayList<Fragment>) f.get(this);
                fs.set(newPos, frag);
                fs.set(oldPos, null);
                f.setAccessible(false);

                f = FragmentStatePagerAdapter.class.getDeclaredField("mSavedState");
                f.setAccessible(true);
                @SuppressWarnings("unchecked")
                ArrayList<Fragment.SavedState> ss = (ArrayList<Fragment.SavedState>) f.get(this);
                if (ss.size() > oldPos) {
                    Fragment.SavedState state = ss.get(oldPos);
                    ss.set(newPos, state);
                    ss.set(oldPos, null);
                }
                f.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException("Something changed!");
            }
        }

        return newPos;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem: " + position);
        String q = App.getQuery();
        Character kanji = q.charAt(0);
        int i = 0;
        for (Character c : q.toCharArray()) {
            Data data = Cache.get(c);
            if (data.isEmpty()) {
                Log.d(TAG, "...skipping 「" + c + "」");
            } else {
                if (i == position) {
                    kanji = c;
                    break;
                } else {
                    ++i;
                }
            }
        }
        return KanjiFragment.newInstance(position, kanji);
    }

    // TODO getPageTitle?
}
