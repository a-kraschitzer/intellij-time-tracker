package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class ComHostUnknownException extends CommunicatorException {


    public ComHostUnknownException() {
        super();
    }

    public ComHostUnknownException(Throwable t) {
        super(t);
    }

    public ComHostUnknownException(String msg, Throwable t) {
        super(msg, t);
    }

}
