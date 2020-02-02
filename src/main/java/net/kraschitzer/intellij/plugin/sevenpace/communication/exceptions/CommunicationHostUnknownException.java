package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class CommunicationHostUnknownException extends CommunicatorException {


    public CommunicationHostUnknownException() {
        super();
    }

    public CommunicationHostUnknownException(Throwable t) {
        super(t);
    }

    public CommunicationHostUnknownException(String msg, Throwable t) {
        super(msg, t);
    }

}
