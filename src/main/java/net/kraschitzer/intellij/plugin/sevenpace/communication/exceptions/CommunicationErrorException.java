package net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions;

import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Error;

public class CommunicationErrorException extends CommunicatorException {

    private Error error;

    public CommunicationErrorException(Error error) {
        super();
        this.error = error;
    }

    public CommunicationErrorException(Error error, String msg) {
        super(msg);
        this.error = error;
    }

    public CommunicationErrorException(Error error, Throwable t) {
        super(t);
        this.error = error;
    }

    public CommunicationErrorException(Error error, String msg, Throwable t) {
        super(msg, t);
        this.error = error;
    }

}
