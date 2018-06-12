package com.example.gay.kanji.pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.example.gay.kanji.App;
import com.example.gay.kanji.data.Cache;
import com.example.gay.kanji.data.Data;

public class KanjiPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "PGAD";

    public KanjiPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        String q = App.getQuery();
        int L = q.length();
        for (Character c : q.toCharArray()) {
            Data data = Cache.get(c);
            if (data != null && data.isEmpty())
                --L;
        }
        return L == 0 ? 1 : L;
    }

    @Override
    public int getItemPosition(Object object) {
        KanjiFragment frag = (KanjiFragment) object;
        Character kanji = frag.getKanji();
        int newPos = POSITION_NONE;

        Data cached = Cache.get(kanji);
        boolean isEmpty = cached != null && cached.isEmpty();
        if (isEmpty) {
            Log.d(TAG, "getItemPosition" + frag + " = " + newPos);
            return newPos;
        }

        int pos = 0;
        int oldPos = frag.getPosition();
        String q = App.getQuery();
        for (int i = 0; i < q.length(); ++i) {
            Character c = q.charAt(i);
            Data data = Cache.get(c);
            if (data == null || !data.isEmpty()) {
                if (kanji.equals(c)) newPos = pos;
                ++pos;
            }
            if (pos > oldPos) break;
        }

        Log.d(TAG, "getItemPosition" + frag + " = " + newPos);

        return newPos;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d(TAG, "destroyItem(pos = " + position + ", obj = KanjiFragment" + object + ")");
        super.destroyItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object result = super.instantiateItem(container, position);
        Log.d(TAG, "instantiateItem(pos = " + position + ") = KanjiFragment" + result);
        return result;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        KanjiFragment f = (KanjiFragment) object;
//        if (f.getFragmentManager() == null) return; // FIXME
        Log.d(TAG, "setPrimaryItem(pos = " + position + ", obj = KanjiFragment" + f + ")");
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem: " + position);
        String q = App.getQuery();
        Character kanji = q.charAt(0);
        int i = 0;
        for (Character c : q.toCharArray()) {
            Data data = Cache.get(c);
            if (data != null && data.isEmpty()) {
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
