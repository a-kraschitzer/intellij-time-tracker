package net.kraschitzer.intellij.plugin.time_tracker.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityCheckState {

    /**
     * Represents if activity check is initiated by the server and amount of seconds left. Null should overwrite existing value.
     */
    private String description;
    private Boolean isRunning;
    private Integer secondsLeft;

}
