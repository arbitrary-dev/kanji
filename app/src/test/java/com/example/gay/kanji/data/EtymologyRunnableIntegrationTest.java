package com.example.gay.kanji.data;

import org.junit.Test;

import java.io.IOException;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.junit.Assert.assertTrue;

public class EtymologyRunnableIntegrationTest {

    @Test
    public void testRetrieveEtymology() throws IOException {
        Pattern p = Pattern.compile(
            "^[^ ].*language.+words.+mouth.+speaking.*[^ ]$", CASE_INSENSITIVE);
        String etym = EtymologyRunnable.retrieveEtymology('語');
        assertTrue(p.matcher(etym).find());
    }
}
