package controllers.ssi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mvel2.MVEL;

import play.Play;
import play.classloading.enhancers.ControllersEnhancer.ByPass;
import play.exceptions.UnexpectedException;
import play.jobs.Job;
import play.libs.F.Promise;
import play.libs.MimeTypes;
import play.mvc.ActionInvoker;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;
import play.vfs.VirtualFile;
import ssi.parser.Document;
import ssi.parser.FileDocumentParser;
import ssi.parser.ParseState;
import ssi.parser.Section;

public class SsiController extends Controller {

    // test with async = true shows worse performance
    // maybe we should activate it for large file
    public static final boolean async = false;

    public static final Map<String, Document> documentCache = new HashMap<String, Document>();

    public static void render() {
        renderWithSsi(false);
    }

    public static void renderWithCache() {
        renderWithSsi(true);
    }

    protected static void renderWithSsi(boolean useCache) {
        String templateName = request.path;
        VirtualFile file = Play.getVirtualFile(templateName);
        if (file == null || !file.exists())
            notFound();
        final File localFile = file.getRealFile();
        final String mimetype = MimeTypes.getContentType(localFile.getName(), "text/plain");


        Document document = null;
        if (useCache && documentCache.containsKey(templateName))
            document = documentCache.get(templateName);

        if (document == null) {
            if (async) {
                Promise<Document> docPromise = new Job<Document>() {
                    @Override
                    public Document doJobWithResult() throws Exception {
                        return FileDocumentParser.parseFile(localFile);
                    }
                }.now();
                document = await(docPromise);
            } else {
                try {
                    document = FileDocumentParser.parseFile(localFile);
                } catch (IOException e) {
                    throw new UnexpectedException(e);
                };
            }
            if (useCache)
                documentCache.put(templateName, document);
        }
        throw renderWithSsi(mimetype, document);
    }

    protected static SsiResult renderWithSsi(String mimetype, Document document) {
        Request innerRequest = newRequest(request);
        Response innerResponse = newResponse(response);
        return renderWithSsi(mimetype, document, innerRequest, innerResponse);
    }

    protected static SsiResult renderWithSsi(String mimetype, Document document,
            Request innerRequest, Response innerResponse) {

        final SsiResult ssiResult = new SsiResult(mimetype);
        Boolean currentCondition = null;

        for (int sectionIt = 0 ; sectionIt < document.sections.size() ; sectionIt++) {
            Section section = document.sections.get(sectionIt);
            if (currentCondition == null || currentCondition.booleanValue()) {
                if (section.parseState == ParseState.PLAIN_TEXT)
                    ssiResult.results.add(new ByteArrayResult(section.content));

                else if (section.parseState == ParseState.INCLUDE) {
                    // TODO flush content and call include asynchronous?
                    // TODO is the charset relevant? (URL in UTF8?)
                    innerRequest.path = new String(section.content);
                    innerResponse.out.reset();
                    ActionInvoker.invoke(innerRequest, innerResponse);
                    ssiResult.results.add(new ByteArrayResult(innerResponse.out.toByteArray()));

                } else if (section.parseState == ParseState.IF) {
                    if (sectionIt == document.sections.size() - 1)
                        error("expected else or endif expression");
                    if (section.content == null || section.content.length == 0)
                        error("empty if expression");

                    // TODO is the charset relevant? (URL in UTF8?)
                    String ifExpression = new String(section.content);
                    final Object result = MVEL.eval(ifExpression, getELVariables());
                    if (!(result instanceof Boolean))
                        error("following if expression must evaluate to boolean: " + ifExpression);

                    currentCondition = (Boolean)result;

                } else if (section.parseState == ParseState.ECHO && section.content != null) {
                    String echoExpression = new String(section.content);
                    final Object result = MVEL.eval(echoExpression, getELVariables());
                    if (result != null)
                        ssiResult.results.add(new StringResult(result.toString()));
                }
            }
            if (section.parseState == ParseState.ELSE) {
                if (currentCondition == null)
                    error("else without if");
                currentCondition = Boolean.valueOf(!currentCondition.booleanValue());
            } else if (section.parseState == ParseState.ENDIF) {
                currentCondition = null;
            }
        }

        if (currentCondition != null)
            error("missing endif expression");
        return ssiResult;
    }

    protected static Map getELVariables() {
        Map vars = new HashMap();
        vars.put("request", request);
        vars.put("params", params);
        return vars;
    }

    private static Request newRequest(Request originalRequest) {
        Request request = Request.createRequest(
                originalRequest.remoteAddress,
                originalRequest.method,
                "",
                originalRequest.querystring,
                originalRequest.contentType,
                originalRequest.body,
                originalRequest.url,
                originalRequest.host,
                true,
                originalRequest.port,
                originalRequest.domain,
                originalRequest.secure,
                originalRequest.headers,
                originalRequest.cookies
        );
        return request;
    }

    private static Response newResponse(Response originalReponse) {
        Response response = new Response();
        response.out = new ByteArrayOutputStream();
        response.headers = originalReponse.headers;
        response.cookies = originalReponse.cookies;
        return response;
    }

    /**
     * this will set a flag so calling another action won't trigger a redirect
     */
    private static void dontRedirect() {
        play.classloading.enhancers.ControllersEnhancer.ControllerInstrumentation.initActionCall();
    }

    /**
     * run another action wrapped in a runnable run() and intercept the Result
     *
     * one should wrap the call to another action like this: new Runnable () {
     * public void run() { AnotherController.action();} }
     *
     * @param runnable
     */
    @ByPass
    protected static Result getResultFromAction(Runnable runnable) {
        dontRedirect();
        return ActionCaller.getResultFromAction(runnable);
    }

}
