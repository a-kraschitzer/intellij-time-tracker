package net.kraschitzer.intellij.plugin.sevenpace.model.enums;

public enum NextIdleCheck {

    ONE_HOUR(0),

    TOMORROW(1),

    NEXT_MONDAY(2),

    ;

    private int value;

    NextIdleCheck(int value) {
        this.value = value;
    }

}
