package controllers.ssi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;
import play.test.FunctionalTest;
import ssi.parser.Document;
import ssi.parser.ParseState;
import ssi.parser.Section;

import static ssi.parser.ParseState.*;

public class SsiControllerTest extends FunctionalTest {

    private Document document;
    private Request request;
    private Response response;

    @Before
    public void setUp() {
        document = new Document();
        request = newRequest();
        request.contentType = "text/html";
        request.current.set(request);
        request.body = new ByteArrayInputStream(new byte[0]);
        response = newResponse();
        response.out = new ByteArrayOutputStream();
    }


    @Test
    public void testRenderPlain() {
        document.add( PLAIN_TEXT, "" );
        SsiController.renderWithSsi("text/html", document, response, request, response);
    }

    @Test( expected = play.mvc.results.Error.class )
    public void testRenderIfAtTheEnd() {
        document.add( IF );
        SsiController.renderWithSsi("text/html", document, response, request, response);
    }

    @Test( expected = play.mvc.results.Error.class )
    public void testRenderIfWithoutEndIf() {
        document
            .add( IF )
            .add( INCLUDE, "url" );
        SsiController.renderWithSsi("text/html", document, response, request, response);
    }

    @Test
    public void testSimpleIfTrue() {
        testSimpleIfBooleanInternal(false);
    }

    @Test
    public void testSimpleIfFalse() {
        testSimpleIfBooleanInternal(true);
    }

    private void testSimpleIfBooleanInternal(boolean ifExpression) {
        document
            .add( IF, ifExpression ? "true" : "false" )
            .add(   PLAIN_TEXT, "then section" )
            .add( ELSE )
            .add(   PLAIN_TEXT, "else section" )
            .add( ENDIF );

        SsiResult ssiResult = SsiController.renderWithSsi("text/html", document, response, request, response);
        assertEquals(1, ssiResult.results.size());
        Result result = ssiResult.results.get(0);
        assertEquals(ifExpression ? "then section" : "else section", ((ByteArrayResult) result).toString());
    }


    @Test
    public void testNestedIf() {
        document
            .add( IF,  "true" )
            .add(   PLAIN_TEXT, "then section level 1" )
            .add(   IF,  "false" )
            .add(       PLAIN_TEXT, "then section level 2" )
            .add(   ELSE )
            .add(       PLAIN_TEXT, "else section level 2" )
            .add(   ENDIF )
            .add( ELSE )
            .add(   PLAIN_TEXT, "else section level 1" )
            .add( ENDIF );

        SsiResult ssiResult = SsiController.renderWithSsi("text/html", document, response, request, response);
        assertEquals(2, ssiResult.results.size());
        Result if1 = ssiResult.results.get(0);
        assertEquals("then section level 1", ((ByteArrayResult) if1).toString());
        Result if2 = ssiResult.results.get(1);
        assertEquals("else section level 2", ((ByteArrayResult) if2).toString());
    }
}
