package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class CommunicatorException extends Throwable {

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
