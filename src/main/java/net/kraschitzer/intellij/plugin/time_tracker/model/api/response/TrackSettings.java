package net.kraschitzer.intellij.plugin.time_tracker.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.enums.ResponseState;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackSettings {

    /**
     * Each response with "trackingStateModel" contains this object where status is kept. In case of "Warning" or "Error" message is set to "responseMessage".
     * This message should be displayed to the user.
     */
    private String description;
    private Boolean isTrackingStartAllowed;
    private ResponseState responseState;
    private String responseMessage;

}
