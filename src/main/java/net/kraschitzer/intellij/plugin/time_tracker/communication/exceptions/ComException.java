package net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions;

public class ComException extends CommunicatorException {

    public ComException(String msg) {
        super(msg);
    }

    public ComException(Throwable t) {
        super(t);
    }

    public ComException(String msg, Throwable t) {
        super(msg, t);
    }

}
