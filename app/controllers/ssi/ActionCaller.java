package controllers.ssi;

import play.classloading.enhancers.ControllersEnhancer.ByPass;
import play.mvc.results.Result;

public class ActionCaller {

    /**
     * run another action wrapped in a runnable run() and intercept the Result
     *
     * one should wrap the call to another action like this: new Runnable () {
     * public void run() { AnotherController.action();} }
     *
     * @param runnable
     */
    protected static Result getResultFromAction(Runnable runnable) {
        try {
            runnable.run();
            System.out.println("JapidController.getResultFromAction() warning: the runnable did not generate a result.");
            return null;
        } catch (Result e) {
            return e;
        }
    }
}
