package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;

@Data
public class WorkItemSearch {

    private String groupName;
    private WorkItem workItem;

}
