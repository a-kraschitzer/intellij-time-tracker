package net.kraschitzer.intellij.plugin.time_tracker;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vcs.BranchChangeListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.time_tracker.view.TimeTrackerToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
public class EventHandler implements BranchChangeListener, BulkFileListener, FileEditorManagerListener {

    private static final long ACTION_DELAY = 1;
    private static EventHandler instance;

    public static EventHandler getInstance() {
        if (instance == null) {
            instance = new EventHandler();
        }
        return instance;
    }

    private LocalDateTime latestActionTimeStamp = LocalDateTime.now();

    public EventHandler() {
        log.info("Created instance of EventHandler");
        if (instance == null) {
            instance = this;
        }
    }

    @Override
    public void branchWillChange(@NotNull String branchName) {
    }

    @Override
    public void branchHasChanged(@NotNull String branchName) {
        TimeTrackerToolWindow toolWindow = TimeTrackerToolWindow.getInstance();
        if (toolWindow != null) {
            SwingUtilities.invokeLater(() -> toolWindow.startTrackOnBranchUpdate(branchName));
        }
    }

    @Override
    public void after(@NotNull java.util.List<? extends VFileEvent> events) {
        distillAction();
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        distillAction();
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        distillAction();
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        distillAction();
    }

    private void distillAction() {
        if (latestActionTimeStamp.plus(ACTION_DELAY, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
            TimeTrackerToolWindow tracker = TimeTrackerToolWindow.getInstance();
            if (tracker != null) {
                SwingUtilities.invokeLater(tracker::handleAction);
            }
        }
        latestActionTimeStamp = LocalDateTime.now();
    }

}
