package net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ComParseException extends CommunicatorException {

    public ComParseException(String msg) {
        super(msg);
    }

    public ComParseException(Throwable t) {
        super(t);
    }

    public ComParseException(String msg, Throwable t) {
        super(msg, t);
    }

}
