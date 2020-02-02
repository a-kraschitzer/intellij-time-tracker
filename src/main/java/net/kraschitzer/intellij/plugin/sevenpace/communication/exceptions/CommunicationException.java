package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class CommunicationException extends CommunicatorException {

    public CommunicationException(String msg) {
        super(msg);
    }

    public CommunicationException(Throwable t) {
        super(t);
    }

    public CommunicationException(String msg, Throwable t) {
        super(msg, t);
    }

}
