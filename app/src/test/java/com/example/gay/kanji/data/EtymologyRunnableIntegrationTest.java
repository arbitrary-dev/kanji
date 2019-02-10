package com.example.gay.kanji.data;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class EtymologyRunnableIntegrationTest {

    @Test
    public void testRetrieveEtymology() throws IOException {
        String etym = EtymologyRunnable.retrieveEtymology('語');

        assertThat(etym, containsString("language"));
        assertThat(etym, containsString("words"));
        assertThat(etym, containsString("saying"));
        assertThat(etym, containsString("to-speak"));
    }
}
