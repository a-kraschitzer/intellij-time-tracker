package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class ComErrorParseException extends ComErrorException {


    public ComErrorParseException() {
        super();
    }

    public ComErrorParseException(String msg) {
        super(msg);
    }

    public ComErrorParseException(Throwable t) {
        super(t);
    }

    public ComErrorParseException(String msg, Throwable t) {
        super(msg, t);
    }

}
