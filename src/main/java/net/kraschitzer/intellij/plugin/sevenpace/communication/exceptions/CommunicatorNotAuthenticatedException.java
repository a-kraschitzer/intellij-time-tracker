package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class CommunicatorNotAuthenticatedException extends CommunicatorException {

    public CommunicatorNotAuthenticatedException() {
        super();
    }

    public CommunicatorNotAuthenticatedException(String msg) {
        super(msg);
    }

    public CommunicatorNotAuthenticatedException(Throwable t) {
        super(t);
    }

    public CommunicatorNotAuthenticatedException(String msg, Throwable t) {
        super(msg, t);
    }

}
