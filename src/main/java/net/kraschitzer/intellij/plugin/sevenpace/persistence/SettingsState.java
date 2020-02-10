package net.kraschitzer.intellij.plugin.sevenpace.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import net.kraschitzer.intellij.plugin.sevenpace.model.enums.BranchCheckoutBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "time-tracker-settings", storages = {@Storage("timeTracker.xml")})

public class SettingsState implements PersistentStateComponent<SettingsState> {

    public String url;
    public String accessToken;
    public String refreshToken;
    public String expires;

    public BranchCheckoutBehaviour branchCheckoutBehaviour = BranchCheckoutBehaviour.DIALOG;

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
