package net.kraschitzer.intellij.plugin.time_tracker.model.api.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class StartTrackingRequest {

    @NotNull
    @Builder.Default
    private Integer timeZone = 0;
    private Integer tfsId;

    @NotNull
    @Builder.Default
    private String remark = "";

    @NotNull
    @Builder.Default
    private String activityTypeId = "";

}
