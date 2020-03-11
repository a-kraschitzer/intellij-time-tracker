package net.kraschitzer.intellij.plugin.time_tracker.model;

import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Authentication implements PersistentStateComponent {
    @Nullable
    @Override
    public Object getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Object state) {

    }

    @Override
    public void noStateLoaded() {

    }

    @Override
    public void initializeComponent() {

    }
}
