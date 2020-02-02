package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.ErrorCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Error {

    private Integer statusCode;
    private ErrorCode errorCode;
    private String errorDescription;
}
