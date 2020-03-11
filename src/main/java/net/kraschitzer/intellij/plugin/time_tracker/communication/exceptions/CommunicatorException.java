package net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions;

public class CommunicatorException extends Exception {

    public CommunicatorException() {
        super();
    }

    public CommunicatorException(String msg) {
        super(msg);
    }

    public CommunicatorException(Throwable t) {
        super(t);
    }

    public CommunicatorException(String msg, Throwable t) {
        super(msg, t);
    }
}
