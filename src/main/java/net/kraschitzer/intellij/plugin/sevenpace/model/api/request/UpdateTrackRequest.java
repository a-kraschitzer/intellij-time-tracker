package net.kraschitzer.intellij.plugin.sevenpace.model.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateTrackRequest {

    private String remark;
    private String activityTypeId;

}
