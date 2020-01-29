package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import net.kraschitzer.intellij.plugin.sevenpace.utils.DateDeserializer;

import java.time.LocalDateTime;

@Data
public class Token {
    private String access_token;
    private String token_type;
    private Long expires_in;
    private String refresh_token;

    @JsonProperty(".issued")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDateTime issued;

    @JsonProperty(".expires")
    @JsonDeserialize(using = DateDeserializer.class)
    private LocalDateTime expires;
    private String client_id;

}
