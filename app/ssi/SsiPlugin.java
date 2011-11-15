package ssi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import play.Logger;
import play.PlayPlugin;
import play.mvc.ActionInvoker;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.RenderHtml;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

public class SsiPlugin extends PlayPlugin {


    @Override
    public boolean serveStatic(VirtualFile file, Request request, Response response) {
        if (file.getName().endsWith(".htm")) {
            try {
                String output = new Scanner(file.getRealFile()).useDelimiter("\\Z").next();

                Request innerRequest = FunctionalTest.newRequest();
                innerRequest.path = "/hello/toi";
                innerRequest.querystring = request.querystring;
                Response innerResponse = FunctionalTest.newResponse();
                ActionInvoker.invoke(innerRequest, innerResponse);
                String actionOutput = innerResponse.out.toString();

                output = output.replaceAll("yann", actionOutput);
                response.setContentTypeIfNotSet("text/html");
                response.out.write(output.getBytes());
                return true;
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
        return super.serveStatic(file, request, response);
    }

}
