package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class ComErrorParseException extends CommunicatorException {


    public ComErrorParseException() {
        super();
    }

    public ComErrorParseException(Throwable t) {
        super(t);
    }

    public ComErrorParseException(String msg, Throwable t) {
        super(msg, t);
    }

}
