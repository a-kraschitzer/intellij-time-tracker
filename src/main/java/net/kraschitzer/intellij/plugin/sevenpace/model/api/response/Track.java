package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.StoppedTrackType;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.TimeTrackingState;

import javax.validation.constraints.NotNull;

@Data
public class Track {

    private Integer tfsId;
    private String remark;
    private String activityTypeId;
    private WorkItem workItem;
    private TimeTrackingState trackingState;
    private StoppedTrackType stoppedTrackType;
    private ActivityCheckState activityCheck;
    private Integer currentTrackLength;
    private Integer totalMeTodayLength;
    private Integer totalMeLength;
    @NotNull
    private Integer totalTeamLength;
    private Integer trackAdjustmentLength;
    private String currentTrackStartedDateTime;
    @NotNull
    private String trackStatusChangeDate;
    private Integer trackTimeZone;

}
