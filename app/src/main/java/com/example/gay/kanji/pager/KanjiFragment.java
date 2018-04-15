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
import com.example.gay.kanji.data.DataRetriever;
import com.example.gay.kanji.data.DataTask;

public class KanjiFragment extends Fragment {

    private static final String TAG = "FRAG";

    private Character mKanji;
    private DataTask mTask;

    private static final String KANJI_KEY = "kanji_key";

    public KanjiFragment() { } // required

    public static KanjiFragment newInstance(Character kanji) {
        Log.d(TAG, "newInstance: " + kanji);
        KanjiFragment fragment = new KanjiFragment();
        Bundle args = new Bundle();
        args.putChar(KANJI_KEY, kanji);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mKanji = getArguments().getChar(KANJI_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_kanji, container, false);
        KanjiWebView webView = (KanjiWebView) v.findViewById(R.id.webView);
        mTask = DataRetriever.retrieve(webView, mKanji);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.kanji_fragment, menu);

        // TODO text editing button
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onDestroy()");
        mTask.stop();
    }
}
