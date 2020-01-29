package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;

@Data
public class ActivityTypeSetting {

    private String id;
    private String color;
    private String name;
    private Boolean isDefault;
    private Boolean isNotSet;

}
