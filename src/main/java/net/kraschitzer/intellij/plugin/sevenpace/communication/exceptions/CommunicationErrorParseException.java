package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

public class CommunicationErrorParseException extends CommunicatorException {


    public CommunicationErrorParseException() {
        super();
    }

    public CommunicationErrorParseException(Throwable t) {
        super(t);
    }

    public CommunicationErrorParseException(String msg, Throwable t) {
        super(msg, t);
    }

}
