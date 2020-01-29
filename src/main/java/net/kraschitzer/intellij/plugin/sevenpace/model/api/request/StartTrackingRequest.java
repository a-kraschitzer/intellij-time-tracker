package net.kraschitzer.intellij.plugin.sevenpace.model.api.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class StartTrackingRequest {

    @NotNull
    private Integer timeZone;
    private Integer tfsId;
    @NotNull
    private String remark;
    @NotNull
    private String activityTypeId;

}
