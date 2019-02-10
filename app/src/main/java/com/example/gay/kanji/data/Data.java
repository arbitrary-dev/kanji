package com.example.gay.kanji.data;

import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static com.example.gay.kanji.App.JAP_CHAR_RANGE;

public class Data {

    public static final String NO_DATA = "";
    static final String LOADING = "…";

    public final Character kanji;

    // TODO Halpern NJECD Index
    private String gif;
    private String info;
    private String etymology;
    private String on, kun, meaning;

    private boolean full, empty;

    public static class Builder {

        private final Data data;

        private Builder(Character kanji) {
            data = new Data(kanji);
        }

        public Builder setGif(String gif) {
            data.gif = gif;
            return this;
        }

        public Builder setEtymology(String etymology) {
            data.etymology = etymology;
            return this;
        }

        public Builder setOn(String on) {
            data.on = on;
            return this;
        }

        public Builder setKun(String kun) {
            data.kun = kun;
            return this;
        }

        public Builder setMeaning(String meaning) {
            data.meaning = meaning;
            return this;
        }

        public Data build() {
            data.formInfo();
            data.empty = NO_DATA.equals(data.info) && NO_DATA.equals(data.gif);
            data.full = !(data.info.contains(LOADING) || data.gif == null);
            return data;
        }
    }

    public static Builder builder(Character kanji) {
        return new Builder(kanji);
    }

    private Data(Character kanji) {
        this.kanji = kanji;
    }

    public boolean isFull() {
        return full;
    }

    public boolean isEmpty() {
        return empty;
    }

    public String getGif() {
        return gif;
    }

    public String getEtymology() {
        return etymology;
    }

    public String getOn() {
        return on;
    }

    public String getKun() {
        return kun;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getInfo() {
        return info;
    }

    void formInfo() {
        if (etymology == null && on == null && kun == null && meaning == null) {
            info = LOADING;
            return;
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

        info = sb.toString();
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
        return (empty ? "Empty" : "") + getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
