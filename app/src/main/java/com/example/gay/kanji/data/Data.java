package com.example.gay.kanji.data;

import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static com.example.gay.kanji.App.JAP_CHAR_RANGE;

public class Data {

    public static final String NO_DATA = "";
    private static final String LOADING = "…";

    final Character kanji;
    private volatile String info;

    // TODO Halpern NJECD Index
    private volatile String gif;
    private volatile String etymology;
    private volatile String on, kun, meaning;

    public Data(Character kanji) {
        this.kanji = kanji;
    }

    public boolean isFull() {
        return !(getInfo().contains(LOADING) || gif == null);
    }

    public boolean isEmpty() {
        return NO_DATA.equals(getInfo()) && NO_DATA.equals(gif);
    }

    public Character getKanji() {
        return kanji;
    }

    public void setGif(String gif) {
        this.gif = gif;
    }

    public String getGif() {
        return gif;
    }

    public String getEtymology() {
        return etymology;
    }

    public synchronized void setEtymology(String etymology) {
        this.etymology = etymology;
        info = null;
    }

    public String getOn() {
        return on;
    }

    public synchronized void setOn(String on) {
        this.on = on;
        info = null;
    }

    public String getKun() {
        return kun;
    }

    public synchronized void setKun(String kun) {
        this.kun = kun;
        info = null;
    }

    public String getMeaning() {
        return meaning;
    }

    public synchronized void setMeaning(String meaning) {
        this.meaning = meaning;
        info = null;
    }


    public String getInfo() {
        String etymology;
        String on;
        String kun;
        String meaning;

        synchronized (this) {
            if (info != null)
                return info;

            etymology = this.etymology;
            on = this.on;
            kun = this.kun;
            meaning = this.meaning;
        }

        List<String> data = new LinkedList<>();
        addLine(data, glue(clean(etymology)));
        addLine(data, formJdic(on, kun, meaning));

        // Collapse adjacent nulls and replace with LOADING
        ListIterator<String> it = data.listIterator();
        boolean prevWasNull = false;
        while (it.hasNext()) {
            String curr = it.next();
            if (curr == null) {
                if (prevWasNull) it.remove();
                else it.set(LOADING);
                prevWasNull = true;
            } else {
                prevWasNull = false;
            }
        }

        StringBuilder sb = new StringBuilder();

        for (String line : data) {
            sb.append("<p>");
            sb.append(line);
            sb.append("</p>");
        }

        synchronized (this) {
            info = sb.toString();
            return info;
        }
    }

    private void addLine(List<String> data, String line) {
        if (NO_DATA.equals(line))
            return;

        if (line != null)
            line = line.replace("\"", "\\\"");

        data.add(line);
    }

    private static String formJdic(String on, String kun, String meaning) {
        if (on == null || kun == null || meaning == null)
            return null;

        StringBuilder jdic = new StringBuilder();
        appendSpan(jdic, glue(on));
        appendSpan(jdic, dimSuffixes(glue(kun)));
        appendSpan(jdic, glue(meaning));

        return jdic.toString();
    }

    private static void appendSpan(StringBuilder sb, String text) {
        if (text == null || text.isEmpty())
            return;
        sb.append("<span class='section'>");
        sb.append(text);
        sb.append("</span> "); // space at the end is necessary to have even spacing
    }
    /** Cleans some nasty shit retreived from the Internets */
    @Nullable
    private static String clean(String s) {
        return s == null ? null
            : s.replaceAll("(?<=[\\[(])\\s+|\\s+(?=[]),.])", "")
            .replaceAll("([^ ])(?=[(\\[])", "$1 ");
    }

    /** Glues together kanji's so they are not sparsely justified */
    @Nullable
    private static String glue(String s) {
        return s == null ? null : s.replaceAll(
            "([\\[(\\-" + JAP_CHAR_RANGE + "]+[" + JAP_CHAR_RANGE + "][])\\-.," + JAP_CHAR_RANGE
                + "]*|[\\[(\\-" + JAP_CHAR_RANGE + "]*[" + JAP_CHAR_RANGE + "][])\\-.,"
                + JAP_CHAR_RANGE + "]+)",
            "<span class='glue'>$1</span>"
        );
    }

    private static String dimSuffixes(String kun) {
        return kun == null ? null
            : kun.replaceAll("\\.([" + JAP_CHAR_RANGE + "]+)", "<span class='dim'>$1</span>");
    }

    @Override
    public String toString() {
        return (isEmpty() ? "Empty" : "") + getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }

    public synchronized Data copy() {
        Data copy = new Data(kanji);
        copy.gif = gif;
        copy.etymology = etymology;
        copy.on = on;
        copy.kun = kun;
        copy.meaning = meaning;
        return copy;
    }
}
