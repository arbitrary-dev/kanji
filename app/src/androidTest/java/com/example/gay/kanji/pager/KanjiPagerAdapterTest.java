package com.example.gay.kanji.pager;

import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LruCache;
import android.view.ViewGroup;

import com.example.gay.kanji.KanjiActivity;
import com.example.gay.kanji.data.Cache;
import com.example.gay.kanji.data.Data;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Field;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static com.example.gay.kanji.data.Data.NO_DATA;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class KanjiPagerAdapterTest {

    @Rule
    public final ActivityTestRule<KanjiActivity> mActivityRule =
        new ActivityTestRule<>(KanjiActivity.class);

    @Rule
    public final MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private ViewGroup viewGroup;

    @Test
    public void getItemPosition_hack() {
        FragmentActivity activity = mActivityRule.getActivity();
        KanjiPagerAdapter p = new KanjiPagerAdapter(activity.getSupportFragmentManager(), "日本語");

        p.instantiateItem(viewGroup, 0);
        p.instantiateItem(viewGroup, 1);
        Object item = p.instantiateItem(viewGroup, 2);

        cacheEmptyDataFor(p.getQuery().charAt(1));

        assertEquals("Item should move left one position", 1, p.getItemPosition(item));
        assertEquals("Instantiated item should be the same", item, p.instantiateItem(viewGroup, 1));
        assertNotEquals("Should instantiate new item", item, p.instantiateItem(viewGroup, 2));
    }

    private void cacheEmptyDataFor(Character ch) {
        try {
            Field f = Cache.class.getDeclaredField("instance");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            LruCache<Character, Data> c = (LruCache<Character, Data>) f.get(null);
            Data data = Data.builder(ch)
                .setGif(NO_DATA)
                .setEtymology(NO_DATA)
                .setOn(NO_DATA)
                .setKun(NO_DATA)
                .setMeaning(NO_DATA)
                .build();
            assertTrue(data.isEmpty());
            c.put(ch, data);
            f.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
