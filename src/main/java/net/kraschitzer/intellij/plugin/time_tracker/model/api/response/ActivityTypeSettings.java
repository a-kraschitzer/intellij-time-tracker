package net.kraschitzer.intellij.plugin.time_tracker.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityTypeSettings {

    private Boolean isEnabled;
    private String defaultActivity;
    private List<ActivityTypeSetting> activityTypes;

}
