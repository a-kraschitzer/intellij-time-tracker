package net.kraschitzer.intellij.plugin.sevenpace.view;

import com.intellij.openapi.wm.ToolWindow;
import net.kraschitzer.intellij.plugin.sevenpace.communication.Communicator;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.CommunicatorNotInitializedException;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SevenPaceToolWindow {

    private JLabel currentTrackItemId;
    private JLabel currentTrackItemDescription;
    private JLabel currentTrackTime;
    private JButton stopTracking;

    private JTextField startTrackingItemId;
    private JButton startTracking;

    private JTable workItemsRecent;
    private JTable workItemsMine;
    private JTable workItemsAll;
    private JPanel contentPanel;

    private Communicator communicator;

    public SevenPaceToolWindow(ToolWindow toolWindow) {
        stopTracking.addActionListener(this::stopTracking);
        startTracking.addActionListener(this::startTracking);

        communicator = Communicator.getInstance();
        try {
            System.out.println(communicator.getCurrentState(true));
        } catch (CommunicatorNotInitializedException e) {
            e.printStackTrace();
        }
    }

    private void startTracking(ActionEvent actionEvent) {

    }

    private void stopTracking(ActionEvent e) {

    }

    public JPanel getContent() {
        return contentPanel;
    }

}
