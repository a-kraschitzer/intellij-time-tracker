package net.kraschitzer.intellij.plugin.time_tracker.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@State(name = "time_tracker-favourites", storages = {@Storage("timeTracker.xml")})

public class FavouritesState implements PersistentStateComponent<FavouritesState> {

    public List<String> ids = new ArrayList<>();
    public Map<String, String> titles = new HashMap<>();
    public Map<String, String> types = new HashMap<>();

    public FavouritesState() {
    }

    public void addFavourite(String selectedWorkItemId, String type, String title) {
        if (!ids.contains(selectedWorkItemId)) {
            ids.add(selectedWorkItemId);
            types.put(selectedWorkItemId, type);
            titles.put(selectedWorkItemId, title);
        }
    }

    public void removeFavourite(String id) {
        if (ids.contains(id)) {
            titles.remove(id);
            types.remove(id);
            ids.remove(id);
        }
    }

    public boolean containsId(String id) {
        return ids != null && ids.contains(id);
    }

    @Nullable
    @Override
    public FavouritesState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull FavouritesState fav) {
        XmlSerializerUtil.copyBean(fav, this);
    }
}
