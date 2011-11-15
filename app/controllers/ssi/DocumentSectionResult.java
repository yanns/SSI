package controllers.ssi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;
import ssi.parser.Section;

public class DocumentSectionResult extends Result {

    final Section section;

    public DocumentSectionResult(Section section) {
        this.section = section;
    }

    @Override
    public void apply(Request request, Response response) {
        try {
            response.out.write(section.content.getBytes(getEncoding()));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

}
