package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.ErrorCode;

@Data
public class Error {

    private Integer statusCode;
    private ErrorCode errorCode;
    private String errorDescription;
    private String message;

}
