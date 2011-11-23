package controllers.ssi;

import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;

public class StringResult extends Result {

    final String content;

    public StringResult(String content) {
        this.content = content;
    }

    @Override
    public void apply(Request request, Response response) {
        try {
            response.out.write(content.getBytes(getEncoding()));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public String toString() {
        return new String(content);
    }

}
