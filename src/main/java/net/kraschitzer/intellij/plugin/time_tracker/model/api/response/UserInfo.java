package net.kraschitzer.intellij.plugin.time_tracker.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {

    private String userId;
    private String userName;
    /**
     * Email for DevOps Services, Domain/login for DevOps Server.
     */
    private String userUniqueName;
    /**
     * Image only avialable for DevOps Services version. Image encoded in base64.
     */
    private String encodedImage;
    private String applicationVersion;
    private String teamServicesUrl;
    private String projectId;

}
