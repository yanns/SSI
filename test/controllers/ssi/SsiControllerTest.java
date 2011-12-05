package controllers.ssi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    public void testRenderPlain() throws IOException {
        document.add( PLAIN_TEXT, "" );
        SsiController.renderWithSsi("text/html", document, response, request, null);
    }

    @Test( expected = play.mvc.results.Error.class )
    public void testRenderIfAtTheEnd() throws IOException {
        document.add( IF );
        SsiController.renderWithSsi("text/html", document, response, request, null);
    }

    @Test( expected = play.mvc.results.Error.class )
    public void testRenderIfWithoutEndIf() throws IOException {
        document
            .add( IF )
            .add( INCLUDE, "url" );
        SsiController.renderWithSsi("text/html", document, response, request, null);
    }

    @Test
    public void testSimpleIfTrue() throws IOException {
        testSimpleIfBooleanInternal(false);
    }

    @Test
    public void testSimpleIfFalse() throws IOException {
        testSimpleIfBooleanInternal(true);
    }

    private void testSimpleIfBooleanInternal(boolean ifExpression) throws IOException {
        document
            .add( IF, ifExpression ? "true" : "false" )
            .add(   PLAIN_TEXT, "then section" )
            .add( ELSE )
            .add(   PLAIN_TEXT, "else section" )
            .add( ENDIF );

        SsiController.renderWithSsi("text/html", document, response, request, null);
        assertEquals(ifExpression ? "then section" : "else section", response.out.toString() );
    }


    @Test
    public void testNestedIf() throws IOException {
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

        SsiController.renderWithSsi("text/html", document, response, request, null);
        assertEquals("then section level 1else section level 2", response.out.toString() );
    }
}
