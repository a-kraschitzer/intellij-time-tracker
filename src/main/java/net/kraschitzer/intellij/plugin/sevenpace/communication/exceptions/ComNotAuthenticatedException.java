package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class ComNotAuthenticatedException extends CommunicatorException {

    public ComNotAuthenticatedException() {
        super();
    }

    public ComNotAuthenticatedException(String msg) {
        super(msg);
    }

    public ComNotAuthenticatedException(Throwable t) {
        super(t);
    }

    public ComNotAuthenticatedException(String msg, Throwable t) {
        super(msg, t);
    }

}
