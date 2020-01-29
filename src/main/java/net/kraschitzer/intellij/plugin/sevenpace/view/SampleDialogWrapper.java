package net.kraschitzer.intellij.plugin.sevenpace.view;

import com.intellij.openapi.ui.DialogWrapper;
import com.sun.istack.Nullable;

import javax.swing.*;
import java.awt.*;

public class SampleDialogWrapper extends DialogWrapper {

    private String pin;
    private JTextField pinField;

    public SampleDialogWrapper(String pin) {
        super(true); // use current window as parent
        pinField = new JTextField(pin);
        pinField.setEditable(false);
        init();
        setTitle("Enter PIN");
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{getCancelAction()};
    }

    public void setValidated() {
        pinField.setText("Sucessfully connected to 7Pace server!");
        setCancelButtonText("OK");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        GridLayout layout = new GridLayout(3, 1);
        JPanel dialogPanel = new JPanel(new BorderLayout());

        dialogPanel.setLayout(layout);
        dialogPanel.add(new JLabel("Enter PIN:"));
        dialogPanel.add(pinField);
        JTextField tf = new JTextField("https://dev.azure.com/gourban/Core/_apps/hub/7pace.Timetracker.Apps");
        tf.setEditable(false);
        dialogPanel.add(tf);

        return dialogPanel;
    }
}
