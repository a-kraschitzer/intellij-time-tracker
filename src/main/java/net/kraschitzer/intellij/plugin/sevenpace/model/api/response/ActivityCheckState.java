package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;

@Data
public class ActivityCheckState {

    /**
     * Represents if activity check is initiated by the server and amount of seconds left. Null should overwrite existing value.
     */
    private String description;
    private Boolean isRunning;
    private Integer secondsLeft;

}
