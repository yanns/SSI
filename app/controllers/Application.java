package controllers;

import play.*;
import play.cache.CacheFor;
import play.mvc.*;

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

    @CacheFor
    public static void interviews() {
        render();
    }

}