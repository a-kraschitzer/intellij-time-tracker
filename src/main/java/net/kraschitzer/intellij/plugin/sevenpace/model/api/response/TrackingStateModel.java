package net.kraschitzer.intellij.plugin.sevenpace.model.api.response;

import lombok.Data;

@Data
public class TrackingStateModel {

    private Track track;
    private TrackSettings trackSettings;
    private Settings settings;

}
