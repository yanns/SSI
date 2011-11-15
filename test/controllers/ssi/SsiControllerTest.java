package controllers.ssi;

import static org.junit.Assert.*;

import org.junit.Test;

import controllers.ssi.SsiController;
import controllers.ssi.SsiResult;

import ssi.parser.Document;
import ssi.parser.ParseState;
import ssi.parser.Section;

public class SsiControllerTest {

    @Test( expected = SsiResult.class )
    public void testRenderPlain() {
        Document document = new Document();
        document.sections.add(new Section(ParseState.PLAIN_TEXT, "".getBytes()));
        SsiController.renderWithSsi("text/html", document);
    }

    @Test( expected = play.mvc.results.Error.class )
    public void testRenderIfAtTheEnd() {
        Document document = new Document();
        document.sections.add(new Section(ParseState.IF));
        SsiController.renderWithSsi("text/html", document);
    }

    @Test( expected = play.mvc.results.Error.class )
    public void testRenderIfWithoutThen() {
        Document document = new Document();
        document.sections.add(new Section(ParseState.IF));
        document.sections.add(new Section(ParseState.INCLUDE));
        SsiController.renderWithSsi("text/html", document);
    }

}
