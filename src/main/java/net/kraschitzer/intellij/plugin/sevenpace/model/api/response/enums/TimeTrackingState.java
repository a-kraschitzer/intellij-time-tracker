package net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums;

import java.util.EnumSet;

public enum TimeTrackingState {

    /**
     * currently tracking.
     */
    tracking(0),

    /**
     * idle.
     */
    idle(1),

    /**
     * checking if user is idle right now (not implemented).
     */
    idleCheck(2),

    /**
     * checking if user still tracking.
     */
    activityCheck(3),

    /**
     * track was stopped by the server and user should react to it (use with "stoppedTrackType").
     */
    clientInputRequired(4),
    ;

    private int value;

    TimeTrackingState(int value) {
        this.value = value;
    }

    public static TimeTrackingState getByValue(int value) {
        for (TimeTrackingState tts : EnumSet.allOf(TimeTrackingState.class)) {
            if (tts.value == value) {
                return tts;
            }
        }
        return null;
    }
}
