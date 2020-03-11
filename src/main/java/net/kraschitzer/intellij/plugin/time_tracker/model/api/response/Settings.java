package net.kraschitzer.intellij.plugin.time_tracker.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {

    /**
     * When not null, should override existing values. When null existing values should be kept.
     */
    private String description;
    private ActivityTypeSettings activityType;
    private UserInfo userInfo;
    /**
     * If timestamp of new state is older then current, new state should be rejected.
     */
    private long timestamp;

}
