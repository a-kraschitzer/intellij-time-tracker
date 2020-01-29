package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;

import java.util.List;

@Data
public class LatestWorkLogsModel {

    private Integer count;
    private List<WorkLog> workLogs;

}
