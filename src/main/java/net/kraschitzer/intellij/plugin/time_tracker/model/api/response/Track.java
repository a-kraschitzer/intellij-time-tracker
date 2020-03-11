package net.kraschitzer.intellij.plugin.time_tracker.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.enums.StoppedTrackType;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.enums.TimeTrackingState;
import net.kraschitzer.intellij.plugin.time_tracker.utils.DateDeserializer;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDateTime currentTrackStartedDateTime;
    @NotNull
    private String trackStatusChangeDate;
    private Integer trackTimeZone;


}
