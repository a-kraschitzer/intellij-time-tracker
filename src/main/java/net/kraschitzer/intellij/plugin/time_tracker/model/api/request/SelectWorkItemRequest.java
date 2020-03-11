package net.kraschitzer.intellij.plugin.time_tracker.model.api.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class SelectWorkItemRequest {

    private Integer tfsId;
    @NotNull
    private String remark;
    @NotNull
    private String activityTypeId;
    @NotNull
    private Boolean calculateTotals;

}
