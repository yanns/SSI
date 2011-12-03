package controllers;

import play.*;
import play.cache.CacheFor;
import play.mvc.*;
import play.mvc.Http.Request;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }

}