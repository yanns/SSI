package controllers.ssi;

import java.util.ArrayList;
import java.util.List;

import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;

public class SsiResult extends Result {

    final List<Result> results;
    final String mimetype;

    public SsiResult(String mimetype) {
        this.results = new ArrayList<Result>();
        this.mimetype = mimetype;
    }

    @Override
    public void apply(Request request, Response response) {
        setContentTypeIfNotSet(response, mimetype);
        for (Result result : results)
            result.apply(request, response);
    }

}
