package net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions;

public class ComNotInitializedException extends CommunicatorException {

    public ComNotInitializedException() {
        super();
    }

    public ComNotInitializedException(String msg) {
        super(msg);
    }

    public ComNotInitializedException(Throwable t) {
        super(t);
    }

    public ComNotInitializedException(String msg, Throwable t) {
        super(msg, t);
    }

}
