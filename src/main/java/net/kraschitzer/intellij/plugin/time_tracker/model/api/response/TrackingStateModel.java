package net.kraschitzer.intellij.plugin.time_tracker.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingStateModel {

    private Track track;
    private TrackSettings trackSettings;
    private Settings settings;

}
