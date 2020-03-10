package net.kraschitzer.intellij.plugin.sevenpace;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import net.kraschitzer.intellij.plugin.sevenpace.view.Settings;
import net.kraschitzer.intellij.plugin.sevenpace.view.TimeTrackerToolWindow;

import javax.validation.constraints.NotNull;

public class NotificationManager {

    private static final NotificationGroup DEFAULT_NOTIFICATION_GROUP = new NotificationGroup(
            "Timetracker Notifications", NotificationDisplayType.BALLOON, true);
    public static final NotificationGroup IMPORTANT_NOTIFICATION_GROUP = new NotificationGroup(
            "Timetracker Important Notifications", NotificationDisplayType.STICKY_BALLOON, true);
    public static final NotificationGroup TOOL_WINDOW_NOTIFICATION_GROUP = NotificationGroup.toolWindowGroup(
            "Timetracker Toolwindow Notifications", TimeTrackerToolWindow.TOOLWINDOW_ID);
    //public static final NotificationGroup TOOL_WINDOW2_NOTIFICATION_GROUP = new NotificationGroup(
    //        "Timetracker Toolwindow Notifications", NotificationDisplayType.TOOL_WINDOW, true, "Timetracker");

    public static void sendSettingNotification(String msg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Notification notification = DEFAULT_NOTIFICATION_GROUP
                    .createNotification(msg, "Go to Settings to generate a PIN and link with your 7Pace Server!</html>",
                            NotificationType.ERROR,
                            new NotificationListener.UrlOpeningListener(true))
                    .addAction(new NotificationAction("Settings") {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent anActionEvent,
                                                    @NotNull Notification notification) {
                            DataContext dataContext = anActionEvent.getDataContext();
                            Project project = PlatformDataKeys.PROJECT.getData(dataContext);
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, Settings.class);
                        }
                    });
            Notifications.Bus.notify(notification);
        });
    }

    public static void sendWarningNotification(String title, String msg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Notification notification = IMPORTANT_NOTIFICATION_GROUP
                    .createNotification(title, msg,
                            NotificationType.WARNING,
                            new NotificationListener.UrlOpeningListener(true));
            Notifications.Bus.notify(notification);
        });
    }

    public static void sendToolWindowNotification(String title, String msg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Notification notification = TOOL_WINDOW_NOTIFICATION_GROUP.createNotification(title, msg, NotificationType.INFORMATION, null);
            Notifications.Bus.notify(notification);
        });
    }
}
