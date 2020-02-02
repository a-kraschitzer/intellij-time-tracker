package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class CommunicatorNotInitializedException extends CommunicatorException {

    public CommunicatorNotInitializedException() {
        super();
    }

    public CommunicatorNotInitializedException(String msg) {
        super(msg);
    }

    public CommunicatorNotInitializedException(Throwable t) {
        super(t);
    }

    public CommunicatorNotInitializedException(String msg, Throwable t) {
        super(msg, t);
    }

}
