package net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ComErrorParseException extends CommunicatorException {

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
