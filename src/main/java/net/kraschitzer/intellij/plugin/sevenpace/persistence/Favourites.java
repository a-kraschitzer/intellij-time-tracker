package net.kraschitzer.intellij.plugin.sevenpace.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.WorkItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@State(name = "time-tracker-favourites", storages = {@Storage("timeTracker.xml")})

public class Favourites implements PersistentStateComponent<Favourites> {

    public List<String> ids;
    public Map<String, String> titles;
    public Map<String, String> types;
    public Map<String, String> links;

    public Favourites() {
        ids = new ArrayList<>();
        titles = new HashMap<>();
        types = new HashMap<>();
        links = new HashMap<>();
    }

    public void addFavourite(WorkItem item) {
        addFavourite(item.getId().toString(),
                item.getTitle(),
                item.getType(),
                item.getWorkItemLink());
    }

    public void addFavourite(String selectedWorkItemId, String type, String title, String toString) {
        if (!ids.contains(selectedWorkItemId)) {
            ids.add(selectedWorkItemId);
            types.put(selectedWorkItemId, type);
            titles.put(selectedWorkItemId, title);
            links.put(selectedWorkItemId, toString);
        }
    }

    public void removeFavourite(String id) {
        if (ids.contains(id)) {
            titles.remove(id);
            types.remove(id);
            links.remove(id);
            ids.remove(id);
        }
    }

    @Nullable
    @Override
    public Favourites getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull Favourites fav) {
        XmlSerializerUtil.copyBean(fav, this);
    }
}
