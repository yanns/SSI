package ssi.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

public class FileDocumentParserTest {

    private static final int NUMBER_OF_THREAD = 100;
    private static final int NUMBER_OF_LOOP = 100;

    @Test
    public void testParseFile() throws IOException, URISyntaxException {
        URL homepage = this.getClass().getResource("homepage.html");
        Document result = FileDocumentParser.parseFile(homepage.toURI());
        assertNotNull(result.sections);
        assertEquals(3, result.sections.size());

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(0).parseState);

        assertEquals(ParseState.INCLUDE, result.sections.get(1).parseState);
        assertEquals("/hello/toi", result.sections.get(1).getContentAsString());

        assertEquals(ParseState.PLAIN_TEXT, result.sections.get(2).parseState);
    }

    @Test
    public void performanceTest() throws IOException, InterruptedException {
        Runnable test = new Runnable() {
            @Override
            public void run() {
                for (int i = 0 ; i < NUMBER_OF_LOOP ; i++ ) {
                    try {
                        testParseFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread[] threads = new Thread[NUMBER_OF_THREAD];
        for (int i = 0 ; i < NUMBER_OF_THREAD ; i++ )
            threads[i] = new Thread(test);
        for (int i = 0 ; i < NUMBER_OF_THREAD ; i++ )
            threads[i].start();
        for (int i = 0 ; i < NUMBER_OF_THREAD ; i++ )
            threads[i].join();
    }

}
