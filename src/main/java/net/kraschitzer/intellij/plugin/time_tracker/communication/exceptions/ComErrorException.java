package net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions;

import lombok.Getter;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.Error;

@Getter
public class ComErrorException extends CommunicatorException {

    private Error error;

    public ComErrorException(Error error) {
        super();
        this.error = error;
    }

    public ComErrorException(Error error, String msg) {
        super(msg);
        this.error = error;
    }

    public ComErrorException(Error error, Throwable t) {
        super(t);
        this.error = error;
    }

    public ComErrorException(Error error, String msg, Throwable t) {
        super(msg, t);
        this.error = error;
    }

}
