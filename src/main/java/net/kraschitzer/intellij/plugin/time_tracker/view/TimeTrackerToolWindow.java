package net.kraschitzer.intellij.plugin.time_tracker.view;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.messages.MessageBus;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.time_tracker.EventHandler;
import net.kraschitzer.intellij.plugin.time_tracker.NotificationManager;
import net.kraschitzer.intellij.plugin.time_tracker.communication.ICommunicator;
import net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions.ComErrorException;
import net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions.ComNotInitializedException;
import net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions.ComParseException;
import net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions.CommunicatorException;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.request.StartTrackingRequest;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.Settings;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.*;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.enums.ResponseState;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.enums.TimeTrackingState;
import net.kraschitzer.intellij.plugin.time_tracker.model.enums.Icon;
import net.kraschitzer.intellij.plugin.time_tracker.model.enums.Reason;
import net.kraschitzer.intellij.plugin.time_tracker.model.enums.StartTrackingBehaviour;
import net.kraschitzer.intellij.plugin.time_tracker.persistence.FavouritesState;
import net.kraschitzer.intellij.plugin.time_tracker.persistence.SettingsState;
import net.kraschitzer.intellij.plugin.time_tracker.utils.IntegerDocumentFilter;
import net.kraschitzer.intellij.plugin.time_tracker.utils.MouseClickListener;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class TimeTrackerToolWindow implements Runnable {

    public static final String TOOLWINDOW_ID = "Timetracker";
    public static final Pattern BRANCH_NAME_PATTERN = Pattern.compile("[a-zA-Z]+/(\\d+)_\\w+");

    private ToolWindow baseWindow;

    //region GUI components
    private JLabel labelCurrentTrackItemId;
    private JLabel labelCurrentTrackItemDescription;
    private JLabel labelCurrentTrackTime;
    private JLabel labelCurrentTrackIcon;

    private JTextField textFieldSearchWorkItems;
    private JTextField textFieldSelectedWorkItem;

    private JComboBox<ActivityTypeSetting> comboBoxActivityType;

    private JButton buttonStartTracking;
    private JButton buttonResumeStopTracking;
    private JButton buttonSearchWorkItems;
    private JButton buttonRefresh;

    private JTable tableWorkItemsRecent;
    private JTable tableWorkItemsMine;
    private JTable tableWorkItemsFavourites;
    private JTable tableWorkItemsSearch;

    private JPanel contentPanel;
    private JTabbedPane tabbedPaneTables;
    private JSeparator separatorVertical;
    //endregion

    private ICommunicator communicator;
    private Map<String, String> activityTypes;

    private TrackingStateModel currentState;

    private FavouritesState favourites = ServiceManager.getService(FavouritesState.class);
    private SettingsState settings = ServiceManager.getService(SettingsState.class);
    private String workItemUrlBase = null;

    private LocalDateTime latestActionTimeStamp = LocalDateTime.now();

    private LocalDateTime dialogLastClosed = LocalDateTime.MIN;
    private boolean dialogOpen = false;

    private static TimeTrackerToolWindow instance;

    public static TimeTrackerToolWindow getInstance() {
        return instance;
    }

    public TimeTrackerToolWindow(ToolWindow toolWindow) {
        if (TimeTrackerToolWindow.getInstance() != null) {
            return;
        }
        instance = this;
        baseWindow = toolWindow;
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            MessageBus messageBus = project.getMessageBus();
            messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, EventHandler.getInstance());
        }

        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(this, 20, 60, TimeUnit.SECONDS);
        communicator = ICommunicator.getInstance();
        initializeComponents();

        refresh(null);
    }

    public JPanel getContent() {
        return contentPanel;
    }

    // region INITIALIZATION
    private void createUIComponents() {
        separatorVertical = new JSeparator(1);
    }

    private void resetCurrentTrackedItem() {
        labelCurrentTrackItemId.setText("-");
        labelCurrentTrackItemDescription.setText("-");
        labelCurrentTrackIcon.setIcon(null);
        labelCurrentTrackTime.setText("--:--");
    }

    private void initializeComponents() {
        buttonRefresh.addActionListener(this::refresh);
        buttonStartTracking.addActionListener(this::startTrackingButtonAction);
        buttonResumeStopTracking.addActionListener(this::resumeStopTrackingCurrent);
        buttonSearchWorkItems.addActionListener(this::searchWorkItems);
        textFieldSearchWorkItems.addActionListener(this::searchWorkItems);
        labelCurrentTrackItemDescription.addMouseListener((MouseClickListener) e -> openURL(null));
        ((PlainDocument) textFieldSelectedWorkItem.getDocument()).setDocumentFilter(new IntegerDocumentFilter());

        tabbedPaneTables.addChangeListener(this::tabbedPaneSelectionAction);

        attachTableSelectionListener(tableWorkItemsRecent);
        attachTableSelectionListener(tableWorkItemsFavourites);
        attachTableSelectionListener(tableWorkItemsSearch);
    }

    private void refresh(ActionEvent actionEvent) {
        updateLatestActionTimeStamp();
        try {
            currentState = communicator.getCurrentState(true);
            loadActivityTypes(currentState.getSettings());
            updateCurrentTrackedItem();
        } catch (ComNotInitializedException e) {
            NotificationManager.sendSettingNotification("Communicator not initialized.");
            log.info("Communicator hasn't been initialized.");
        } catch (ComErrorException e) {
            log.debug("ComError in refresh action", e);
            String notificationMessage = "";
            String logErrorMessage = "Timetracker encountered a communication error.\n";
            if (e.getError() != null) {
                if (e.getError().getMessage() != null) {
                    notificationMessage = notificationMessage + "message: " + e.getError().getMessage() + "\n";
                    logErrorMessage += e.getError() + "\n";
                }
            } else {
                if (e.getMessage() != null) {
                    notificationMessage = notificationMessage + "message: " + e.getMessage() + "\n";
                    logErrorMessage += e.getMessage() + "\n";
                }
            }
            log.info(logErrorMessage);
            NotificationManager.sendWarningNotification("Timetracker encountered a communication error", notificationMessage);
        } catch (ComParseException e) {
            log.debug("Failed to parse response from 7pace Timetracker server", e);
            NotificationManager.sendWarningNotification("Response Parse Error",
                    "Failed to parse 7pace Timetracker response. For details please see IntelliJ Logs.");
            log.info("Failed to parse response from 7pace Timetracker server '{}'", e.getMessage());
        } catch (CommunicatorException e) {
            log.debug("Communication error in refresh action", e);
        }
    }

    private void refreshTables() {
        switch (tabbedPaneTables.getSelectedIndex()) {
            case 0:
                loadRecentItems();
                break;
            case 2:
                loadFavouriteItems();
                break;
            case 1:
            case 3:
            default:
                break;

        }
    }

    private void tabbedPaneSelectionAction(ChangeEvent e) {
        updateLatestActionTimeStamp();
        if (e.getSource() instanceof JTabbedPane) {
            JTabbedPane pane = (JTabbedPane) e.getSource();
            if (pane.getSelectedIndex() == 0) {
                loadRecentItems();
            } else if (pane.getSelectedIndex() == 2) {
                loadFavouriteItems();
            }
        }
    }

    private void attachTableSelectionListener(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 4) {
                    c.setForeground(JBColor.BLUE);
                } else {
                    c.setForeground(JBColor.BLACK);
                }
                return c;
            }
        });
        table.addMouseListener((MouseClickListener) e -> {
            String selectedWorkItemId = table.getValueAt(table.getSelectedRow(), 1).toString();
            textFieldSelectedWorkItem.setText(selectedWorkItemId);

            if (table.getSelectedColumn() == 0) {
                if (favourites.ids.contains(selectedWorkItemId)) {
                    favourites.removeFavourite(selectedWorkItemId);
                    table.setValueAt(Icon.STAR_EMPTY.getIcon(), table.getSelectedRow(), table.getSelectedColumn());
                } else {
                    favourites.addFavourite(selectedWorkItemId,
                            table.getValueAt(table.getSelectedRow(), 3).toString(),
                            table.getValueAt(table.getSelectedRow(), 4).toString());
                    table.setValueAt(Icon.STAR.getIcon(), table.getSelectedRow(), table.getSelectedColumn());
                }
            }
            if (table.getSelectedColumn() == 4) {
                openURL(table);
            }
        });
    }

    private void loadActivityTypes(Settings settings) {
        activityTypes = new HashMap<>();
        for (ActivityTypeSetting setting : settings.getActivityType().getActivityTypes()) {
            comboBoxActivityType.addItem(setting);
            if (setting.getIsDefault()) {
                comboBoxActivityType.setSelectedItem(setting);
            }
            activityTypes.put(setting.getId(), setting.getName());
        }
    }
    //endregion

    //region Data Population
    private void loadSearchResults(SearchResultModel searchResultModel) {
        Vector<Vector<Object>> data = new Vector<>();
        for (WorkItem workItem : searchResultModel.getWorkItems().stream().map(WorkItemSearch::getWorkItem).collect(Collectors
                .toList())) {
            String workItemId = workItem.getId().toString();
            data.add(initializeTableVector(workItemId,
                    favourites.containsId(workItemId),
                    workItem.getType(),
                    workItem.getTitle()));
        }

        tableWorkItemsSearch.setModel(new TimetrackerTableModel(data, TimetrackerTableModel.workItemsColumnNames));
        TimetrackerTableModel.adjustColumnSizes(tableWorkItemsSearch, TimetrackerTableModel.workItemColumnSizeAbsWidth);
    }

    private void loadFavouriteItems() {
        if (favourites == null || favourites.ids == null) {
            log.info("Favourites have not been initialized!");
            return;
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        for (String id : favourites.ids) {
            data.add(initializeTableVector(id, true, favourites.types.get(id), favourites.titles.get(id)));
        }

        tableWorkItemsFavourites.setModel(new TimetrackerTableModel(data, TimetrackerTableModel.workItemsColumnNames));
        TimetrackerTableModel.adjustColumnSizes(tableWorkItemsFavourites, TimetrackerTableModel.workItemColumnSizeAbsWidth);
    }

    private void loadRecentItems() {
        final LatestWorkLogsModel logs;
        try {
            logs = communicator.getLatestWorkLogs(15);
        } catch (CommunicatorException e) {
            log.debug("Communication Exception in load recent items", e);
            return;
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        for (WorkLog log : logs.getWorkLogs()) {
            String workItemId = log.getWorkItem().getId().toString();
            Vector<Object> vector = initializeTableVector(workItemId,
                    favourites.containsId(workItemId),
                    log.getWorkItem().getType(),
                    log.getWorkItem().getTitle());
            vector.add(activityTypes.get(log.getActivityTypeId()));
            vector.add(formatDuration(log.getPeriodLength().longValue()));
            vector.add(log.getStartTime());
            vector.add(log.getEndTime());
            data.add(vector);
        }


        tableWorkItemsRecent.setModel(new TimetrackerTableModel(data, TimetrackerTableModel.recentColumnNames));
        TimetrackerTableModel.adjustColumnSizes(tableWorkItemsRecent, TimetrackerTableModel.recentColumnSizeAbsWidth);
    }

    private Vector<Object> initializeTableVector(String id,
                                                 boolean favourite,
                                                 String workItemType,
                                                 String title) {
        Vector<Object> vector = new Vector<>();
        vector.add(favourite ? Icon.STAR.getIcon() : Icon.STAR_EMPTY.getIcon());
        vector.add(id);
        vector.add(Icon.getByName(workItemType).getIcon());
        vector.add(workItemType);
        vector.add(title);
        return vector;
    }

    private void updateCurrentTrackedItem() {
        if (currentState != null && currentState.getTrack() != null && currentState.getTrack().getWorkItem() != null) {
            String completeUrl = currentState.getTrack().getWorkItem().getWorkItemLink();
            workItemUrlBase = completeUrl.substring(0, completeUrl.lastIndexOf('/') + 1);
            labelCurrentTrackItemId.setText(String.valueOf(currentState.getTrack().getWorkItem().getId()));
            labelCurrentTrackItemDescription.setText(currentState.getTrack().getWorkItem().getType()
                    + ": " + currentState.getTrack().getWorkItem().getTitle());
            labelCurrentTrackIcon.setIcon(Icon.getByName(currentState.getTrack().getWorkItem().getType()).getIcon());
            updateTimeTrackedTime();

            if (TimeTrackingState.tracking.equals(currentState.getTrack().getTrackingState())) {
                buttonResumeStopTracking.setIcon(Icon.STOP.getIcon());
                buttonResumeStopTracking.setText("Stop Tracking");
                baseWindow.setIcon(Icon.APP_ICON_ACTIVE_DARK.getIcon());
            } else {
                buttonResumeStopTracking.setIcon(Icon.START.getIcon());
                buttonResumeStopTracking.setText("Resume Tracking");
                textFieldSelectedWorkItem.setText(currentState.getTrack().getWorkItem().getId().toString());
                baseWindow.setIcon(Icon.APP_ICON_DARK.getIcon());
            }
            refreshTables();
            contentPanel.repaint();
        } else {
            resetCurrentTrackedItem();
        }
    }

    private void scheduledUpdateTrackedItem() {
        if (currentState == null) {
            refresh(null);
        } else {
            updateTimeTrackedTime();
        }
    }

    private void updateTimeTrackedTime() {
        if (currentState != null
                && currentState.getTrack() != null
                && TimeTrackingState.tracking.equals(currentState.getTrack().getTrackingState())
                && currentState.getTrack().getCurrentTrackStartedDateTime() != null) {
            labelCurrentTrackTime.setText(formatDuration(currentState.getTrack().getCurrentTrackStartedDateTime().atOffset(ZoneOffset.UTC).until(OffsetDateTime.now(), ChronoUnit.SECONDS)));
        }
    }
    //endregion

    //region Actions
    private void startTrackingButtonAction(ActionEvent actionEvent) {
        updateLatestActionTimeStamp();
        if (currentState == null) {
            NotificationManager.sendSettingNotification("Communicator not initialized.");
            return;
        }
        String text = textFieldSelectedWorkItem.getText();
        Integer selectedWorkItemId;
        if (StringUtils.isBlank(text)) {
            selectedWorkItemId = currentState.getTrack().getWorkItem().getId();
        } else {
            selectedWorkItemId = Integer.parseInt(text);
        }
        startTracking(selectedWorkItemId);
    }

    private void resumeStopTrackingCurrent(ActionEvent e) {
        updateLatestActionTimeStamp();
        resumeStopTrackingCurrent(e, Reason.STOPPED_BY_USER);
    }

    private void resumeStopTrackingCurrent(ActionEvent e, Reason stopReason) {
        if (currentState == null) {
            NotificationManager.sendSettingNotification("Communicator not initialized.");
            return;
        }
        if (TimeTrackingState.tracking.equals(currentState.getTrack().getTrackingState())) {
            try {
                TrackingStateModel stopState = communicator.stopTracking(stopReason);
                currentState.getTrack().setTrackingState(stopState.getTrack().getTrackingState());
                NotificationManager.sendToolWindowNotification("Tracking stopped", "");
            } catch (CommunicatorException ex) {
                log.debug("Communication Exception in stop tracking", e);
                currentState = null;
                return;
            }
        } else {
            try {
                currentState = communicator.startTracking(StartTrackingRequest
                        .builder()
                        .activityTypeId(((ActivityTypeSetting) comboBoxActivityType.getSelectedItem()).getId())
                        .tfsId(currentState.getTrack().getTfsId())
                        .build());
            } catch (CommunicatorException ex) {
                log.debug("Communication Exception in resume tracking", e);
                currentState = null;
                return;
            }
        }
        updateCurrentTrackedItem();
    }

    private void searchWorkItems(ActionEvent actionEvent) {
        updateLatestActionTimeStamp();
        try {
            SearchResultModel searchResultModel = communicator.searchWorkItemByModel(textFieldSearchWorkItems.getText());
            loadSearchResults(searchResultModel);
        } catch (CommunicatorException e) {
            log.debug("Communication Exception in work item search", e);
            NotificationManager.sendToolWindowNotification("Error during search", "");
            log.info("Failed to search for work items with search term '{}', with exception '{}'", textFieldSearchWorkItems.getText(), e.getMessage());
        }
    }

    private void openURL(JTable table) {
        final String itemId;
        if (table == null && currentState != null && currentState.getTrack() != null
                && currentState.getTrack().getTfsId() != null) {
            itemId = currentState.getTrack().getTfsId().toString();
        } else if (table != null) {
            itemId = table.getValueAt(table.getSelectedRow(), 1).toString();
        } else {
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(workItemUrlBase + itemId));
            }
        } catch (Exception ex) {
            log.debug("Failed to open external work item link for id '{}' with error: {}", currentState.getTrack().getWorkItem().getId(), ex.getMessage(), ex);
        }
    }

    private void startTracking(Integer selectedWorkItemId) {
        startTracking(selectedWorkItemId, (ActivityTypeSetting) comboBoxActivityType.getSelectedItem());
    }

    private void startTracking(Integer selectedWorkItemId, ActivityTypeSetting activityType) {
        try {
            currentState = communicator.startTracking(StartTrackingRequest
                    .builder()
                    .activityTypeId(activityType.getId())
                    .tfsId(selectedWorkItemId)
                    .build());
            if (!ResponseState.OK.equals(currentState.getTrackSettings().getResponseState())) {
                switch (currentState.getTrackSettings().getResponseState()) {
                    case Warning:
                    case Error:
                        NotificationManager.sendWarningNotification("Failed to start tracking " + selectedWorkItemId + ".",
                                currentState.getTrackSettings().getResponseMessage());
                        break;
                    case AuthError:
                        NotificationManager.sendSettingNotification("Auth Error on starting a Tracking.");
                        break;
                }
                return;
            }
            updateCurrentTrackedItem();
            NotificationManager.sendToolWindowNotification("Started tracking of " + selectedWorkItemId, "");
        } catch (CommunicatorException e) {
            log.debug("Communication Exception in start tracking ow work item", e);
            log.info("Failed to start tracking of work item with id {}; error: {}", selectedWorkItemId, e.getMessage());
        }
    }
    //endregion

    //region utility
    private static String formatDuration(long secondsTotal) {
        int seconds = (int) secondsTotal % 60;
        secondsTotal /= 60;
        int minutes = (int) secondsTotal % 60;
        secondsTotal /= 60;
        int hours = (int) secondsTotal % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private void updateLatestActionTimeStamp() {
        latestActionTimeStamp = LocalDateTime.now();
    }
    //endregion

    //region action handlers
    public void startTrackOnBranchUpdate(@NotNull String branchName) {
        try {
            if (currentState != null) {
                final String workItemIdString;
                Matcher matcher = BRANCH_NAME_PATTERN.matcher(branchName);
                if (matcher.matches()) {
                    workItemIdString = matcher.group(1);
                    if (StartTrackingBehaviour.DIALOG.equals(settings.branchCheckoutBehaviour) && !dialogOpen) {
                        StartTrackingDialog dialog = new StartTrackingDialog("Start Tracking on Branch Work Item?",
                                "Start tracking of the work item linked to the checked out branch?",
                                workItemIdString, currentState.getSettings().getActivityType());
                        dialogOpen = true;
                        if (dialog.showAndGet()) {
                            SwingUtilities.invokeLater(() -> startTracking(dialog.getWorkItemId(), dialog.getSelectedActivityType()));
                        }
                        dialogOpen = false;
                        dialogLastClosed = LocalDateTime.now();
                    } else if (StartTrackingBehaviour.AUTO_TRACK.equals(settings.branchCheckoutBehaviour)) {
                        try {
                            SwingUtilities.invokeLater(() -> startTracking(Integer.parseInt(workItemIdString)));
                        } catch (Exception ex) {
                            NotificationManager.sendToolWindowNotification("Failed to auto start tracking", "invalid branch name");
                            log.info("Failed to parse branchName '" + branchName + "' with exception: " + ex.getMessage());
                        }
                    }
                } else {
                    NotificationManager.sendToolWindowNotification("Failed to automatically start tracking", "Invalid branch name '" + branchName + "'");
                    log.info("Failed to automatically start tracking. Invalid branch name '" + branchName + "'");
                }
            }
        } catch (Exception e) {
            log.debug("Exception in auto start on branch change", e);
            NotificationManager.sendToolWindowNotification("Failed to automatically start tracking", "No work item selected.");
            log.info("no work item id was selected when tracking was started automatically.");
        }
    }

    public void handleAction() {
        updateLatestActionTimeStamp();
        startTrackingOnAction();
    }

    public void startTrackingOnAction() {
        if (!StartTrackingBehaviour.OFF.equals(settings.onActivityBehaviour)
                && currentState != null
                && !TimeTrackingState.tracking.equals(currentState.getTrack().getTrackingState())
                && !dialogOpen && dialogLastClosed.plusSeconds(1).isBefore(LocalDateTime.now())) {
            if (StartTrackingBehaviour.DIALOG.equals(settings.onActivityBehaviour)) {
                StartTrackingDialog dialog = new StartTrackingDialog("Start Tracking", "",
                        textFieldSelectedWorkItem.getText(), currentState.getSettings().getActivityType());
                dialogOpen = true;
                if (dialog.showAndGet()) {
                    SwingUtilities.invokeLater(() -> startTracking(dialog.getWorkItemId(), dialog.getSelectedActivityType()));
                }
                dialogOpen = false;
                dialogLastClosed = LocalDateTime.now();
            } else if (StartTrackingBehaviour.AUTO_TRACK.equals(settings.onActivityBehaviour)) {
                try {
                    SwingUtilities.invokeLater(() -> startTracking(Integer.parseInt(textFieldSelectedWorkItem.getText())));
                } catch (NumberFormatException e) {
                    NotificationManager.sendToolWindowNotification("Failed to automatically start tracking", "No work item selected.");
                    log.info("no work item id was selected when tracking was started automatically.");
                }
            }
        }
    }

    @Override
    public void run() {
        log.info("Entered scheduled task");
        try {
            SwingUtilities.invokeLater(this::scheduledUpdateTrackedItem);
            if (settings.autoStop
                    && latestActionTimeStamp.plus(settings.autoStopActionDelay, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())
                    && TimeTrackingState.tracking.equals(currentState.getTrack().getTrackingState())) {
                log.info("Stopping currently tracked task");
                SwingUtilities.invokeLater(() -> resumeStopTrackingCurrent(null, Reason.STOPPED_BY_MACHINE));
            }
        } catch (Exception e) {
            log.debug("Exception in scheduled task", e);
        }
        log.info("Exiting scheduled task");
    }
    //endregion
}
