package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class WorkLog {

    private String id;
    private String remark;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Double periodLength;
    private String activityTypeId;
    private WorkItem workItem;

}
