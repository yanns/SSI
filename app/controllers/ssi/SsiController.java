package controllers.ssi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.mvel2.MVEL;

import play.Play;
import play.classloading.enhancers.ControllersEnhancer.ByPass;
import play.data.validation.Validation;
import play.exceptions.UnexpectedException;
import play.i18n.Lang;
import play.libs.MimeTypes;
import play.mvc.ActionInvoker;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;
import play.utils.HTML;
import play.vfs.VirtualFile;
import ssi.parser.Document;
import ssi.parser.FileDocumentParser;
import ssi.parser.ParseState;
import ssi.parser.Section;

public class SsiController extends Controller {

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
            try {
                document = FileDocumentParser.parseFile(localFile);
            } catch (IOException e) {
                throw new UnexpectedException(e);
            };
            if (useCache)
                documentCache.put(templateName, document);
        }
        renderWithSsi(mimetype, document);
    }

    protected static void renderWithSsi(String mimetype, Document document) {
        Request innerRequest = newRequest(request);
        Response innerResponse = newResponse(response);
        try {
            renderWithSsi(mimetype, document, response, innerRequest, innerResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void renderWithSsi(String mimetype, Document document, Response currentResponse,
            Request innerRequest, Response innerResponse) throws IOException {

        currentResponse.setContentTypeIfNotSet(mimetype);
        Boolean currentCondition = null;
        Deque<Boolean> ifStack = new ArrayDeque<Boolean>();

        for (int sectionIt = 0 ; sectionIt < document.sections.size() ; sectionIt++) {
            Section section = document.sections.get(sectionIt);
            ParseState parseState = section.parseState;

            if (currentCondition == null || currentCondition.booleanValue()) {

                // sections that are not parsed depending on the current condition

                if (parseState == ParseState.PLAIN_TEXT)
                    currentResponse.out.write(section.content);

                else if (parseState == ParseState.INCLUDE) {
                    // the charset should not be relevant for URL
                    innerRequest.path = new String(section.content);
                    innerRequest.args.clear();
                    innerRequest.args.put("innerRequest", Boolean.TRUE);
                    innerResponse.out.reset();

                    // check if there is parameter
                    while (sectionIt != document.sections.size() - 1
                            && document.sections.get(sectionIt + 1).parseState == ParseState.PARAM ) {
                        section = document.sections.get(++sectionIt);
                        if (section.content == null)
                            error("expecting parameter name");

                        String paramName = new String(section.content);
                        if (sectionIt == document.sections.size() - 1)
                            error("expecting value or end delimiter for parameter '" + paramName + "'");

                        final byte[] paramValue;
                        Section followingSection = document.sections.get(++sectionIt);
                        if (followingSection.parseState == ParseState.PLAIN_TEXT) {
                            if (sectionIt == document.sections.size() - 1)
                                error("expecting end delimiter for parameter '" + paramName + "'");
                            paramValue = followingSection.content;
                            followingSection = document.sections.get(++sectionIt);
                        } else {
                            paramValue = null;
                        }
                        if (followingSection.parseState != ParseState.END_PARAM && followingSection.parseState != ParseState.PARAM) {
                            error("expecting end delimiter for parameter '" + paramName + "'");
                        } else {
                            innerRequest.args.put(paramName, paramValue);
                            if (followingSection.parseState == ParseState.PARAM) sectionIt--;
                        }
                    } // end of include parameters

                    // TODO flush content and call include asynchronous?
                    ActionInvoker.invoke(innerRequest, innerResponse);
                    currentResponse.out.write(innerResponse.out.toByteArray());

                } else if (parseState == ParseState.IF) {
                    if (sectionIt == document.sections.size() - 1)
                        error("expected else or endif expression");
                    if (section.content == null || section.content.length == 0)
                        error("empty if expression");

                    // TODO is the charset relevant? (URL in UTF8?)
                    String ifExpression = new String(section.content);
                    final Object result = MVEL.eval(ifExpression, getELVariables(currentResponse));
                    if (!(result instanceof Boolean))
                        error("following if expression must evaluate to boolean: " + ifExpression);

                    if (currentCondition != null) ifStack.push(currentCondition);
                    currentCondition = (Boolean)result;

                } else if (parseState == ParseState.ECHO && section.content != null) {
                    String echoExpression = new String(section.content);
                    final Object result = MVEL.eval(echoExpression, getELVariables(currentResponse));
                    if (result != null) {
                        // escape HTML to avoid HTML/JS injection
                        String resultAsString = new String(HTML.htmlEscape(result.toString()));
                        currentResponse.out.write(resultAsString.getBytes(currentResponse.encoding));
                    }
                }

            }

            // sections parsed every time (do not depend on condition)

            if (parseState == ParseState.ELSE) {
                if (currentCondition == null)
                    error("else without if");
                currentCondition = Boolean.valueOf(!currentCondition.booleanValue());

            } else if (parseState == ParseState.ENDIF) {
                currentCondition = ifStack.size() != 0 ? ifStack.pop() : null;
            }
        }

        if (currentCondition != null)
            error("missing endif expression");
    }

    protected static Map getELVariables(Response currentResponse) {
        Map vars = new HashMap();
        // be consistent with http://www.playframework.org/documentation/1.2.3/templates#implicits
        vars.put("errors", Validation.current().errors());
        vars.put("flash", flash);
        vars.put("lang", Lang.get());
        vars.put("messages", MessagesWrapper.class);
        vars.put("out", currentResponse.out);
        vars.put("params", params);
        vars.put("play", Play.class);
        vars.put("request", request);
        vars.put("session", session);

        // additional mappings
        vars.put("renderArgs", renderArgs);
        return vars;
    }

    protected static Request newRequest(Request originalRequest) {
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

    protected static Response newResponse(Response originalReponse) {
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
