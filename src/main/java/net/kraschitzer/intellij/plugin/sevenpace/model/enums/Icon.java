package net.kraschitzer.intellij.plugin.sevenpace.model.enums;

import javax.swing.*;
import java.util.EnumSet;

public enum Icon {
    BUG("Bug", "bug.png"),
    EPIC("Epic", "epic.png"),
    TASK("Task", "task.png"),
    USER_STORY("User Story", "user_story.png"),
    ISSUE("Issue", "issue.png"),
    FEATURE("Feature", "feature.png"),
    START(null, "start.png"),
    STOP(null, "stop.png"),
    STAR(null, "star2.png"),
    STAR_EMPTY(null, "star_empty2.png"),
    FALLBACK(null, "fallback.png"),
    ;
    private static final String PATH = "/net/kraschitzer/intellij/plugin/sevenpace/icons/";

    private String workItemType;
    private ImageIcon icon;

    Icon(String workItemType, String fileName) {
        this.workItemType = workItemType;
        this.icon = new ImageIcon(getClass().getResource(PATH + fileName));
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public static Icon getByName(String name) {
        if (name == null) {
            return FALLBACK;
        }
        for (Icon i : EnumSet.allOf(Icon.class)) {
            if (name.equals(i.workItemType)) {
                return i;
            }
        }
        return FALLBACK;
    }
}
