package net.kraschitzer.intellij.plugin.sevenpace.view;

import com.intellij.openapi.wm.ToolWindow;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.sevenpace.NotificationManager;
import net.kraschitzer.intellij.plugin.sevenpace.communication.ICommunicator;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.CommunicatorException;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.CommunicatorNotInitializedException;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.request.StartTrackingRequest;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Settings;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.*;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.ResponseState;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.TimeTrackingState;
import net.kraschitzer.intellij.plugin.sevenpace.model.enums.Reason;
import net.kraschitzer.intellij.plugin.sevenpace.utils.IntegerDocumentFilter;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

@Slf4j
public class SevenPaceToolWindow {

    private JLabel labelCurrentTrackItemId;
    private JLabel labelCurrentTrackItemDescription;
    private JLabel labelCurrentTrackTime;
    private JLabel labelCurrentTrackIcon;

    private JTextField textFieldSearchWorkItems;
    private JTextField textFieldSelectedWorkItem;

    private JComboBox<ActivityTypeSetting> comboBoxActivityType;

    private JButton buttonStartTracking;
    private JButton buttonStopTracking;
    private JButton buttonSearchWorkItems;

    private JTable tableWorkItemsRecent;
    private JTable tableWorkItemsMine;
    private JTable tableWorkItemsFavorites;
    private JTable tableWorkItemsSearch;

    private JPanel contentPanel;
    private JTabbedPane tabbedPaneTables;
    private JSeparator separatorVertical;

    private ICommunicator communicator;
    private NotificationManager notificationManager;
    private Map<String, ImageIcon> icons;
    private Map<String, String> activityTypes;
    private Map<String, URI> workItemRecentUris;
    private Map<String, URI> workItemSearchUris;

    private TrackingStateModel currentState;


    public SevenPaceToolWindow(ToolWindow toolWindow) {
        notificationManager = NotificationManager.getInstance();
        communicator = ICommunicator.getInstance();

        workItemRecentUris = new HashMap<>();
        workItemSearchUris = new HashMap<>();

        loadIcons();
        initializeComponents();

        try {
            currentState = communicator.getCurrentState(true);
            loadActivityTypes(currentState.getSettings());
            updateCurrentTrackedItem();
        } catch (CommunicatorNotInitializedException e) {
            notificationManager.sendSettingNotification("Communicator not initialized.");
            log.info("Communicator hasn't been initialized.");
        } catch (CommunicatorException e) {
            e.printStackTrace();
        }
        loadRecentItems();
    }

    public JPanel getContent() {
        return contentPanel;
    }

    // region INITIALIZATION
    private void createUIComponents() {
        separatorVertical = new JSeparator(1);
    }

    private void initializeComponents() {
        buttonStartTracking.addActionListener(this::startTracking);
        buttonStopTracking.addActionListener(this::stopTrackingCurrent);
        buttonSearchWorkItems.addActionListener(this::searchWorkItems);
        labelCurrentTrackItemDescription.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentState != null && currentState.getTrack() != null
                        && currentState.getTrack().getWorkItem() != null
                        && currentState.getTrack().getWorkItem().getWorkItemLink() != null) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(new URI(currentState.getTrack().getWorkItem().getWorkItemLink()));
                        }
                    } catch (Exception ex) {
                        log.debug("Failed to open external work item link for id '" + currentState.getTrack().getWorkItem().getId() + "' with error: " + ex.getMessage());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        ((PlainDocument) textFieldSelectedWorkItem.getDocument()).setDocumentFilter(new IntegerDocumentFilter());

        tabbedPaneTables.addChangeListener(e -> {
            if (e.getSource() instanceof JTabbedPane) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                if (pane.getSelectedIndex() == 0) {
                    loadRecentItems();
                }
            }
        });

        attachTableSelectionListener(tableWorkItemsRecent);
        attachTableSelectionListener(tableWorkItemsSearch);
    }

    private void attachTableSelectionListener(JTable table) {
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedWorkItemId = table.getValueAt(table.getSelectedRow(), 0).toString();
                textFieldSelectedWorkItem.setText(selectedWorkItemId);

                URI uri = null;
                if (table.getSelectedColumn() == 3) {
                    if (table == tableWorkItemsRecent) {
                        uri = workItemRecentUris.get(selectedWorkItemId);
                    } else if (table == tableWorkItemsSearch) {
                        uri = workItemSearchUris.get(selectedWorkItemId);
                    }
                    if (uri == null) {
                        notificationManager.sendWarningNotification("Failed to open URL.", "No URL was found for the selected Work Item.");
                    }
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(uri);
                        }
                    } catch (Exception ex) {
                        log.debug("Failed to open external work item link for id '" + selectedWorkItemId + "' with error: " + ex.getMessage());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
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

    private void loadIcons() {
        String path = "/net/kraschitzer/intellij/plugin/sevenpace/workItems/";
        icons = new HashMap<>();
        icons.put("Bug", new ImageIcon(getClass().getResource(path + "bug.png")));
        icons.put("Epic", new ImageIcon(getClass().getResource(path + "epic.png")));
        icons.put("Task", new ImageIcon(getClass().getResource(path + "task.png")));
        icons.put("User Story", new ImageIcon(getClass().getResource(path + "user_story.png")));
        icons.put("Issue", new ImageIcon(getClass().getResource(path + "issue.png")));
        icons.put("Feature", new ImageIcon(getClass().getResource(path + "feature.png")));
        icons.put("Task_Start", new ImageIcon(getClass().getResource(path + "start.png")));
        icons.put("Task_Stop", new ImageIcon(getClass().getResource(path + "stop.png")));
    }
    //endregion

    //region Data Population
    private void loadSearchResults(SearchResultModel searchResultModel) {
        // names of columns
        Vector<String> columnNames = new Vector<>();
        columnNames.add("id");
        columnNames.add("");
        columnNames.add("type");
        columnNames.add("name");
        columnNames.add("parent");

        // data of the table
        workItemSearchUris.clear();
        Vector<Vector<Object>> data = new Vector<>();
        for (WorkItemSearch workItemContainer : searchResultModel.getWorkItems()) {
            addUrlToMap(workItemSearchUris, workItemContainer.getWorkItem());
            Vector<Object> vector = new Vector<>();
            vector.add(workItemContainer.getWorkItem().getId());
            vector.add(icons.get(workItemContainer.getWorkItem().getType()));
            vector.add(workItemContainer.getWorkItem().getType());
            vector.add(workItemContainer.getWorkItem().getTitle());
            vector.add(workItemContainer.getWorkItem().getParent());
            data.add(vector);
        }

        tableWorkItemsSearch.setModel(new RecentTableModel(data, columnNames));
        tableWorkItemsSearch.getColumnModel().getColumn(0).setMinWidth(40);
        tableWorkItemsSearch.getColumnModel().getColumn(0).setMaxWidth(40);

        tableWorkItemsSearch.getColumnModel().getColumn(1).setMinWidth(20);
        tableWorkItemsSearch.getColumnModel().getColumn(1).setMaxWidth(20);

        tableWorkItemsSearch.getColumnModel().getColumn(2).setMinWidth(70);
        tableWorkItemsSearch.getColumnModel().getColumn(2).setMaxWidth(70);
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
        columnNames.add("id");
        columnNames.add("");
        columnNames.add("type");
        columnNames.add("name");
        columnNames.add("trackType");
        columnNames.add("trackedTime");
        columnNames.add("startTime");
        columnNames.add("endTime");

        workItemRecentUris.clear();
        // data of the table
        Vector<Vector<Object>> data = new Vector<>();
        for (WorkLog log : logs.getWorkLogs()) {
            addUrlToMap(workItemRecentUris, log.getWorkItem());
            Vector<Object> vector = new Vector<>();
            vector.add(log.getWorkItem().getId());
            vector.add(icons.get(log.getWorkItem().getType()));
            vector.add(log.getWorkItem().getType());
            vector.add(log.getWorkItem().getTitle());
            vector.add(activityTypes.get(log.getActivityTypeId()));
            vector.add(formatDuration(log.getPeriodLength().longValue()));
            vector.add(log.getStartTime());
            vector.add(log.getEndTime());
            data.add(vector);
        }


        tableWorkItemsRecent.setModel(new RecentTableModel(data, columnNames));
        tableWorkItemsRecent.getColumnModel().getColumn(0).setMinWidth(40);
        tableWorkItemsRecent.getColumnModel().getColumn(0).setMaxWidth(40);

        tableWorkItemsRecent.getColumnModel().getColumn(1).setMinWidth(20);
        tableWorkItemsRecent.getColumnModel().getColumn(1).setMaxWidth(20);

        tableWorkItemsRecent.getColumnModel().getColumn(2).setMinWidth(70);
        tableWorkItemsRecent.getColumnModel().getColumn(2).setMaxWidth(70);

        tableWorkItemsRecent.getColumnModel().getColumn(4).setMinWidth(80);
        tableWorkItemsRecent.getColumnModel().getColumn(4).setMaxWidth(80);

        tableWorkItemsRecent.getColumnModel().getColumn(5).setMinWidth(70);
        tableWorkItemsRecent.getColumnModel().getColumn(5).setMaxWidth(70);

        tableWorkItemsRecent.getColumnModel().getColumn(6).setMinWidth(130);
        tableWorkItemsRecent.getColumnModel().getColumn(6).setMaxWidth(130);

        tableWorkItemsRecent.getColumnModel().getColumn(7).setMinWidth(130);
        tableWorkItemsRecent.getColumnModel().getColumn(7).setMaxWidth(130);
    }

    private void addUrlToMap(Map<String, URI> map, WorkItem workItem) {
        try {
            map.put(workItem.getId().toString(), new URI(workItem.getWorkItemLink()));
        } catch (Exception ignored) {
        }
    }

    private void updateCurrentTrackedItem() {
        labelCurrentTrackItemId.setText(String.valueOf(currentState.getTrack().getWorkItem().getId()));
        labelCurrentTrackItemDescription.setText(currentState.getTrack().getWorkItem().getType()
                + ": " + currentState.getTrack().getWorkItem().getTitle());
        labelCurrentTrackTime.setText(formatDuration(currentState.getTrack().getTotalMeLength()));
        labelCurrentTrackIcon.setIcon(icons.get(currentState.getTrack().getWorkItem().getType()));

        buttonStopTracking.setEnabled(TimeTrackingState.tracking.equals(currentState.getTrack().getTrackingState()));
    }
    //endregion

    //region Actions
    private void startTracking(ActionEvent actionEvent) {
        String text = textFieldSelectedWorkItem.getText();
        Integer selectedWorkItemId;
        if (StringUtils.isBlank(text)) {
            selectedWorkItemId = currentState.getTrack().getWorkItem().getId();
        } else {
            selectedWorkItemId = Integer.parseInt(text);
        }
        try {
            currentState = communicator.startTracking(StartTrackingRequest
                    .builder()
                    .activityTypeId(((ActivityTypeSetting) comboBoxActivityType.getSelectedItem()).getId())
                    .tfsId(selectedWorkItemId)
                    .build());
            if (!ResponseState.OK.equals(currentState.getTrackSettings().getResponseState())) {
                switch (currentState.getTrackSettings().getResponseState()) {
                    case Warning:
                    case Error:
                        notificationManager.sendWarningNotification("Failed to start tracking " + selectedWorkItemId + ".",
                                currentState.getTrackSettings().getResponseMessage());
                        break;
                    case AuthError:
                        notificationManager.sendSettingNotification("Auth Error on starting a Tracking.");
                        break;
                }
                return;
            }
            updateCurrentTrackedItem();
        } catch (CommunicatorException e) {
            log.info("Failed to start tracking of work item with id {}; error: {}", selectedWorkItemId, e.getMessage());
        }
    }

    private void stopTrackingCurrent(ActionEvent e) {
        if (TimeTrackingState.tracking.equals(currentState.getTrack().getTrackingState())) {
            try {
                TrackingStateModel stopState = communicator.stopTracking(Reason.STOPPED_BY_USER);
                currentState.getTrack().setTrackingState(stopState.getTrack().getTrackingState());
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
    //endregion
}
