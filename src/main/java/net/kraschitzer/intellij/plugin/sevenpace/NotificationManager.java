package net.kraschitzer.intellij.plugin.sevenpace;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import net.kraschitzer.intellij.plugin.sevenpace.view.Settings;

import javax.validation.constraints.NotNull;

public class NotificationManager {

    public static NotificationManager getInstance() {
        return ServiceManager.getService(NotificationManager.class);
    }

    private final NotificationGroup localNotificationGroup = new NotificationGroup(
            "7Pace Timetracker Notifications", NotificationDisplayType.TOOL_WINDOW, true);

    public void sendSettingNotification(String msg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Notification notification = localNotificationGroup
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

    public void sendWarningNotification(String title, String msg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            Notification notification = localNotificationGroup
                    .createNotification(title, msg,
                            NotificationType.WARNING,
                            new NotificationListener.UrlOpeningListener(true));
            Notifications.Bus.notify(notification);
        });
    }
}
