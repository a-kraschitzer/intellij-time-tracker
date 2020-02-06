package net.kraschitzer.intellij.plugin.sevenpace.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@State(name = "7PaceTimetracker")
public class TimetrackerState implements PersistentStateComponent<TimetrackerState> {

    public String url;
    public String accessToken;
    public String refreshToken;
    public String expires;

    @Nullable
    @Override
    public TimetrackerState getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull TimetrackerState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
