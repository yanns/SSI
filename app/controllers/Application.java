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

    public static void hello2() {
        renderText("hello");
    }

    public static void hello(String name) {
        renderText("hello " + name);
    }

    public static void interviews() {
        String applicationNumber1 = "HLR--1002347";
        render(applicationNumber1);
    }

}