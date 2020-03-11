package net.kraschitzer.intellij.plugin.time_tracker.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import net.kraschitzer.intellij.plugin.time_tracker.model.enums.StartTrackingBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "time_tracker-settings", storages = {@Storage("timeTracker.xml")})

public class SettingsState implements PersistentStateComponent<SettingsState> {

    public String url;
    public String accessToken;
    public String refreshToken;
    public String expires;

    public StartTrackingBehaviour branchCheckoutBehaviour = StartTrackingBehaviour.DIALOG;
    public StartTrackingBehaviour onActivityBehaviour = StartTrackingBehaviour.OFF;
    public int autoStopActionDelay = 300;
    public boolean autoStop = true;

    public SettingsState() {
    }

    @Nullable
    @Override
    public SettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SettingsState set) {
        XmlSerializerUtil.copyBean(set, this);
    }

    public void clear() {
        accessToken = null;
        refreshToken = null;
        expires = null;
    }
}
