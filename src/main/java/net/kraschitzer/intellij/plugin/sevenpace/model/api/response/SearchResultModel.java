package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultModel {

    private String query;
    private List<WorkItemSearch> workItems;
    private Integer count;

}
