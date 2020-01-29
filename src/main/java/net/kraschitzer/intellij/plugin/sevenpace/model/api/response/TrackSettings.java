package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.ResponseState;

@Data
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
