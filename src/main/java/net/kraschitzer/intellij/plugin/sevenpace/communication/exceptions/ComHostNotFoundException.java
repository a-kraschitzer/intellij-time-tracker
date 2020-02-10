package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class ComHostNotFoundException extends CommunicatorException {


    public ComHostNotFoundException() {
        super();
    }

    public ComHostNotFoundException(Throwable t) {
        super(t);
    }

    public ComHostNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

}
