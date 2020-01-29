package net.kraschitzer.intellij.plugin.sevenpace.model.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRequest {

    private String client_id;
    private String grant_type;
    private String code;

}
