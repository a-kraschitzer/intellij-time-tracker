package net.kraschitzer.intellij.plugin.time_tracker.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityTypeSetting {

    private String id;
    private String color;
    private String name;
    private Boolean isDefault;
    private Boolean isNotSet;

    public String toString() {
        return name;
    }

}
