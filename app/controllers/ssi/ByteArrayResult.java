package controllers.ssi;

import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;

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

    @Override
    public String toString() {
        return new String(content);
    }

}
