package net.kraschitzer.intellij.plugin.sevenpace.view;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vcs.BranchChangeListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.sevenpace.NotificationManager;
import net.kraschitzer.intellij.plugin.sevenpace.communication.ICommunicator;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.ComErrorException;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.ComNotInitializedException;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.CommunicatorException;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.StartTrackingRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Settings;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.*;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.ResponseState;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.TimeTrackingState;
import net.kraschitzer.intellij.plugin.sevenpace.model.enums.Icon;
import net.kraschitzer.intellij.plugin.sevenpace.model.enums.Reason;
import net.kraschitzer.intellij.plugin.sevenpace.persistence.FavouritesState;
import net.kraschitzer.intellij.plugin.sevenpace.persistence.SettingsState;
import net.kraschitzer.intellij.plugin.sevenpace.utils.IntegerDocumentFilter;
import net.kraschitzer.intellij.plugin.sevenpace.utils.MouseClickListener;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

@Slf4j
public class TimeTrackerToolWindow implements BranchChangeListener {

    public static final String TOOLWINDOW_ID = "net.kraschitzer.nintellij.plugin.timetracker";

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

    private ICommunicator communicator;
    private Map<String, String> activityTypes;

    private TrackingStateModel currentState;

    private FavouritesState favourites = ServiceManager.getService(FavouritesState.class);
    private SettingsState settings = ServiceManager.getService(SettingsState.class);
    private String workItemUrlBase = null;

    public TimeTrackerToolWindow() {
        this(null);
    }

    public TimeTrackerToolWindow(ToolWindow toolWindow) {
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

    private void initializeComponents() {
        buttonRefresh.addActionListener(this::refresh);
        buttonStartTracking.addActionListener(this::startTrackingButtonAction);
        buttonResumeStopTracking.addActionListener(this::resumeStopTrackingCurrent);
        buttonSearchWorkItems.addActionListener(this::searchWorkItems);
        textFieldSearchWorkItems.addActionListener(this::searchWorkItems);
        labelCurrentTrackItemDescription.addMouseListener((MouseClickListener) e -> openURL(null));
        ((PlainDocument) textFieldSelectedWorkItem.getDocument()).setDocumentFilter(new IntegerDocumentFilter());

        tabbedPaneTables.addChangeListener(e -> {
            if (e.getSource() instanceof JTabbedPane) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                if (pane.getSelectedIndex() == 0) {
                    loadRecentItems();
                } else if (pane.getSelectedIndex() == 2) {
                    loadFavouriteItems();
                }
            }
        });

        attachTableSelectionListener(tableWorkItemsRecent);
        attachTableSelectionListener(tableWorkItemsFavourites);
        attachTableSelectionListener(tableWorkItemsSearch);
    }

    private void refresh(ActionEvent actionEvent) {
        try {
            currentState = communicator.getCurrentState(true);
            loadActivityTypes(currentState.getSettings());
            updateCurrentTrackedItem();
        } catch (ComNotInitializedException e) {
            NotificationManager.sendSettingNotification("Communicator not initialized.");
            log.info("Communicator hasn't been initialized.");
        } catch (ComErrorException e) {
            String logErrorMessage = "Timetracker encountered a communication error.\n";
            NotificationManager.sendWarningNotification("Communication Error",
                    "Timetracker encountered a communication error. For details please see intellij Logs");
            if (e.getError() != null) {
                log.info(logErrorMessage + e.getError());
            } else {
                log.info(logErrorMessage + e.getMessage());
            }
        } catch (CommunicatorException e) {
            e.printStackTrace();
        }
        refreshTables();
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
        // names of columns
        Vector<String> columnNames = new Vector<>();
        columnNames.add("");
        columnNames.add("id");
        columnNames.add("");
        columnNames.add("type");
        columnNames.add("name");
        columnNames.add("parent");

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        for (WorkItemSearch workItemContainer : searchResultModel.getWorkItems()) {
            Vector<Object> vector = new Vector<>();
            if (favourites != null && favourites.ids != null && favourites.ids.contains(workItemContainer.getWorkItem().getId().toString())) {
                vector.add(Icon.STAR.getIcon());
            } else {
                vector.add(Icon.STAR_EMPTY.getIcon());
            }
            vector.add(workItemContainer.getWorkItem().getId());
            vector.add(Icon.getByName(workItemContainer.getWorkItem().getType()).getIcon());
            vector.add(workItemContainer.getWorkItem().getType());
            vector.add(workItemContainer.getWorkItem().getTitle());
            vector.add(workItemContainer.getWorkItem().getParent());
            data.add(vector);
        }

        tableWorkItemsSearch.setModel(new RecentTableModel(data, columnNames));
        tableWorkItemsSearch.getColumnModel().getColumn(0).setMinWidth(20);
        tableWorkItemsSearch.getColumnModel().getColumn(0).setMaxWidth(20);

        tableWorkItemsSearch.getColumnModel().getColumn(1).setMinWidth(40);
        tableWorkItemsSearch.getColumnModel().getColumn(1).setMaxWidth(40);

        tableWorkItemsSearch.getColumnModel().getColumn(2).setMinWidth(20);
        tableWorkItemsSearch.getColumnModel().getColumn(2).setMaxWidth(20);

        tableWorkItemsSearch.getColumnModel().getColumn(3).setMinWidth(70);
        tableWorkItemsSearch.getColumnModel().getColumn(3).setMaxWidth(70);
    }

    private void loadFavouriteItems() {
        if (favourites == null || favourites.ids == null) {
            log.info("Favourites have not been initialized!");
            return;
        }

        // names of columns
        Vector<String> columnNames = new Vector<>();
        columnNames.add("");
        columnNames.add("id");
        columnNames.add("");
        columnNames.add("type");
        columnNames.add("name");

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        for (String id : favourites.ids) {
            Vector<Object> vector = new Vector<>();
            vector.add(Icon.STAR.getIcon());
            vector.add(id);
            vector.add(Icon.getByName(favourites.types.get(id)).getIcon());
            vector.add(favourites.types.get(id));
            vector.add(favourites.titles.get(id));
            data.add(vector);
        }

        tableWorkItemsFavourites.setModel(new RecentTableModel(data, columnNames));
        tableWorkItemsFavourites.getColumnModel().getColumn(0).setMinWidth(20);
        tableWorkItemsFavourites.getColumnModel().getColumn(0).setMaxWidth(20);

        tableWorkItemsFavourites.getColumnModel().getColumn(1).setMinWidth(40);
        tableWorkItemsFavourites.getColumnModel().getColumn(1).setMaxWidth(40);

        tableWorkItemsFavourites.getColumnModel().getColumn(2).setMinWidth(20);
        tableWorkItemsFavourites.getColumnModel().getColumn(2).setMaxWidth(20);

        tableWorkItemsFavourites.getColumnModel().getColumn(3).setMinWidth(70);
        tableWorkItemsFavourites.getColumnModel().getColumn(3).setMaxWidth(70);
    }

    private void loadRecentItems() {
        LatestWorkLogsModel logs = null;
        try {
            logs = communicator.getLatestWorkLogs(15);
        } catch (CommunicatorException e) {
            return;
        }

        // names of columns
        Vector<String> columnNames = new Vector<>();
        columnNames.add("");
        columnNames.add("id");
        columnNames.add("");
        columnNames.add("type");
        columnNames.add("name");
        columnNames.add("trackType");
        columnNames.add("trackedTime");
        columnNames.add("startTime");
        columnNames.add("endTime");

        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        for (WorkLog log : logs.getWorkLogs()) {
            Vector<Object> vector = new Vector<>();
            if (favourites != null && favourites.ids != null && favourites.ids.contains(log.getWorkItem().getId().toString())) {
                vector.add(Icon.STAR.getIcon());
            } else {
                vector.add(Icon.STAR_EMPTY.getIcon());
            }
            vector.add(log.getWorkItem().getId());
            vector.add(Icon.getByName(log.getWorkItem().getType()).getIcon());
            vector.add(log.getWorkItem().getType());
            vector.add(log.getWorkItem().getTitle());
            vector.add(activityTypes.get(log.getActivityTypeId()));
            vector.add(formatDuration(log.getPeriodLength().longValue()));
            vector.add(log.getStartTime());
            vector.add(log.getEndTime());
            data.add(vector);
        }


        tableWorkItemsRecent.setModel(new RecentTableModel(data, columnNames));
        tableWorkItemsRecent.getColumnModel().getColumn(0).setMinWidth(20);
        tableWorkItemsRecent.getColumnModel().getColumn(0).setMaxWidth(20);

        tableWorkItemsRecent.getColumnModel().getColumn(1).setMinWidth(40);
        tableWorkItemsRecent.getColumnModel().getColumn(1).setMaxWidth(40);

        tableWorkItemsRecent.getColumnModel().getColumn(2).setMinWidth(20);
        tableWorkItemsRecent.getColumnModel().getColumn(2).setMaxWidth(20);

        tableWorkItemsRecent.getColumnModel().getColumn(3).setMinWidth(70);
        tableWorkItemsRecent.getColumnModel().getColumn(3).setMaxWidth(70);

        tableWorkItemsRecent.getColumnModel().getColumn(5).setMinWidth(80);
        tableWorkItemsRecent.getColumnModel().getColumn(5).setMaxWidth(80);

        tableWorkItemsRecent.getColumnModel().getColumn(6).setMinWidth(70);
        tableWorkItemsRecent.getColumnModel().getColumn(6).setMaxWidth(70);

        tableWorkItemsRecent.getColumnModel().getColumn(7).setMinWidth(130);
        tableWorkItemsRecent.getColumnModel().getColumn(7).setMaxWidth(130);

        tableWorkItemsRecent.getColumnModel().getColumn(8).setMinWidth(130);
        tableWorkItemsRecent.getColumnModel().getColumn(8).setMaxWidth(130);
    }

    private void updateCurrentTrackedItem() {
        String completeUrl = currentState.getTrack().getWorkItem().getWorkItemLink();
        workItemUrlBase = completeUrl.substring(0, completeUrl.lastIndexOf('/') + 1);
        labelCurrentTrackItemId.setText(String.valueOf(currentState.getTrack().getWorkItem().getId()));
        labelCurrentTrackItemDescription.setText(currentState.getTrack().getWorkItem().getType()
                + ": " + currentState.getTrack().getWorkItem().getTitle());
        labelCurrentTrackTime.setText(formatDuration(currentState.getTrack().getTotalMeLength()));
        labelCurrentTrackIcon.setIcon(Icon.getByName(currentState.getTrack().getWorkItem().getType()).getIcon());

        if (TimeTrackingState.tracking.equals(currentState.getTrack().getTrackingState())) {
            buttonResumeStopTracking.setIcon(Icon.STOP.getIcon());
            buttonResumeStopTracking.setText("Stop Tracking");
        } else {
            buttonResumeStopTracking.setIcon(Icon.START.getIcon());
            buttonResumeStopTracking.setText("Resume Tracking");
        }
        contentPanel.repaint();
    }
    //endregion

    //region Actions
    private void startTrackingButtonAction(ActionEvent actionEvent) {
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
        if (currentState == null) {
            NotificationManager.sendSettingNotification("Communicator not initialized.");
            return;
        }
        if (TimeTrackingState.tracking.equals(currentState.getTrack().getTrackingState())) {
            try {
                TrackingStateModel stopState = communicator.stopTracking(Reason.STOPPED_BY_USER);
                currentState.getTrack().setTrackingState(stopState.getTrack().getTrackingState());
            } catch (CommunicatorException ex) {
                ex.printStackTrace();
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
                ex.printStackTrace();
                return;
            }
        }
        updateCurrentTrackedItem();
        if (tabbedPaneTables.getSelectedIndex() == 0) {
            loadRecentItems();
        }
    }

    private void searchWorkItems(ActionEvent actionEvent) {
        try {
            SearchResultModel searchResultModel = communicator.searchWorkItemByModel(textFieldSearchWorkItems.getText());
            loadSearchResults(searchResultModel);
        } catch (CommunicatorException e) {
            return;
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
            log.debug("Failed to open external work item link for id '" + currentState.getTrack().getWorkItem().getId() + "' with error: " + ex.getMessage());
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
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void branchWillChange(@NotNull String branchName) {
    }

    @Override
    public void branchHasChanged(@NotNull String branchName) {
        final Integer workItemId;
        try {
            String workItemIdString = branchName.split("/")[1].split("_")[0];
            workItemId = Integer.parseInt(workItemIdString);
        } catch (Exception ex) {
            log.debug("Failed to parse branchName '" + branchName + "' with exception: " + ex.getMessage());
            return;
        }
        switch (settings.branchCheckoutBehaviour) {
            case DIALOG:
                StartOnBranchCheckoutDialog dialog = new StartOnBranchCheckoutDialog(workItemId, currentState.getSettings().getActivityType());
                if (dialog.showAndGet()) {
                    SwingUtilities.invokeLater(() -> startTracking(workItemId, dialog.getSelectedActivityType()));
                }
                return;
            case AUTO_TRACK:
                SwingUtilities.invokeLater(() -> startTracking(workItemId));
                return;
            case OFF:
            default:
                break;
        }
    }
    //endregion
}
