package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.PinStatusEnum;

@Data
public class PinStatus {

    private PinStatusEnum status;

}
