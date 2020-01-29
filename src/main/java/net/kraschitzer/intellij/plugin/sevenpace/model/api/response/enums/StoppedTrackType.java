package net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums;

public enum StoppedTrackType {

    Tracking(0),
    StoppedByActivityCheck(1),
    StoppedByPing(2),
    StoppedByClientCondition(3),
    StoppedByMaxSingleTrackLengthExceeded(4),
    Idle(5),
    StoppedByConcurency(6),
    ;

    private int value;

    StoppedTrackType(int value) {
        this.value = value;
    }
}
