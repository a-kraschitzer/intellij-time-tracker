package net.kraschitzer.intellij.plugin.time_tracker.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import net.kraschitzer.intellij.plugin.time_tracker.utils.DateDeserializer;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkLog {

    private String id;
    private String remark;
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDateTime startTime;
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDateTime endTime;
    private Double periodLength;
    private String activityTypeId;
    private WorkItem workItem;

}
