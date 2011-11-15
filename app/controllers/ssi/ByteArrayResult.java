package controllers.ssi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;
import ssi.parser.Section;

public class ByteArrayResult extends Result {

    final byte[] content;

    public ByteArrayResult(byte[] content) {
        this.content = content;
    }

    @Override
    public void apply(Request request, Response response) {
        try {
            response.out.write(content);
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

}
