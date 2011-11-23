package controllers.ssi;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;
import play.test.FunctionalTest;
import ssi.parser.Document;
import ssi.parser.ParseState;
import ssi.parser.Section;

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
    }


    @Test
    public void testRenderPlain() {
        document.sections.add(new Section(ParseState.PLAIN_TEXT, ""));
        SsiController.renderWithSsi("text/html", document, request, response);
    }

    @Test( expected = play.mvc.results.Error.class )
    public void testRenderIfAtTheEnd() {
        document.sections.add(new Section(ParseState.IF));
        SsiController.renderWithSsi("text/html", document, request, response);
    }

    @Test( expected = play.mvc.results.Error.class )
    public void testRenderIfWithoutEndIf() {
        document.sections.add(new Section(ParseState.IF));
        document.sections.add(new Section(ParseState.INCLUDE, "url"));
        SsiController.renderWithSsi("text/html", document, request, response);
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
        document.sections.add(new Section(ParseState.IF, ifExpression ? "true" : "false"));
        document.sections.add(new Section(ParseState.PLAIN_TEXT, "then section"));
        document.sections.add(new Section(ParseState.ELSE));
        document.sections.add(new Section(ParseState.PLAIN_TEXT, "else section"));
        document.sections.add(new Section(ParseState.ENDIF));
        SsiResult ssiResult = SsiController.renderWithSsi("text/html", document, request, response);
        assertEquals(1, ssiResult.results.size());
        Result result = ssiResult.results.get(0);
        assertEquals(ifExpression ? "then section" : "else section", ((ByteArrayResult) result).toString());
    }

}
