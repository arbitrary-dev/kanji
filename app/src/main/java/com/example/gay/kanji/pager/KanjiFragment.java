package com.example.gay.kanji.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gay.kanji.KanjiWebView;
import com.example.gay.kanji.R;
import com.example.gay.kanji.data.Cache;
import com.example.gay.kanji.data.Data;
import com.example.gay.kanji.data.DataTask;

public class KanjiFragment extends Fragment {

    private static final String TAG = "FRAG";

    private DataTask task;

    private static final String KANJI_POS = "kanji_pos";
    private static final String KANJI_KEY = "kanji_key";

    public KanjiFragment() { } // required

    public static KanjiFragment newInstance(int position, Character kanji) {
        Log.d(TAG, "newInstance(" + position + ", " + kanji + ")");
        KanjiFragment fragment = new KanjiFragment();
        Bundle args = new Bundle();
        args.putInt(KANJI_POS, position);
        args.putChar(KANJI_KEY, kanji);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate" + this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView" + this);
        View v = inflater.inflate(R.layout.fragment_kanji, container, false);
        KanjiWebView webView = (KanjiWebView) v.findViewById(R.id.webView);

        Character kanji = getKanji();
        Data data = Cache.get(kanji);
        if (!data.isFull()) {
            // TODO smart loading
            // There should be a loader icon first 1-2 seconds waiting for
            // everything to be loaded, if etymology misses the time and still
            // loading, then everything is revealed, but etymology will have a
            // "…" placeholder.
            task = new DataTask(data);
            task.start(new UiCallback(task, webView));
        } else {
            webView.update(data);
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.kanji_fragment, menu);

        // TODO text editing button
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy" + this);
        if (task != null)
            task.stop();
        super.onDestroy();
    }

    @Override
    public String toString() {
        int p = getPosition();
        Character k = getKanji();
        return "(" + (p != -1 && k != null ? p + ", " + k : "") + ") "
            + Integer.toHexString(hashCode());
    }

    int getPosition() {
        return getArguments().getInt(KANJI_POS, -1);
    }

    Character getKanji() {
        return getArguments().getChar(KANJI_KEY);
    }
}
