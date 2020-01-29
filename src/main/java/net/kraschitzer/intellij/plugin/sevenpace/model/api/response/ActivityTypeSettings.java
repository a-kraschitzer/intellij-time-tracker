package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;

import java.util.List;

@Data
public class ActivityTypeSettings {

    private Boolean isEnabled;
    private String defaultActivity;
    private List<ActivityTypeSetting> activityTypes;

}
