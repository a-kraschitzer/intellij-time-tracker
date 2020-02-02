package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.PinStatusEnum;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PinStatus {

    private PinStatusEnum status;

}
