package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkItem {

    private Integer id;
    private String title;
    private String color;
    private String teamProject;
    private String type;
    private String workItemLink;
    private Float effort;
    private WorkItem parent;

}
