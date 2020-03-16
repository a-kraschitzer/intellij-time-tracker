package net.kraschitzer.intellij.plugin.time_tracker.view;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.sun.istack.Nullable;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.time_tracker.utils.MouseClickListener;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

@Slf4j
public class PINDialog extends DialogWrapper {

    private final String tenantName;
    private final JTextField pinField;

    public PINDialog(String pin, String tenantName) {
        super(true);
        this.tenantName = tenantName;
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
        pinField.setText("Successfully connected to 7pace Timetracker server!");
        setCancelButtonText("OK");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final GridLayout layout = new GridLayout(3, 1);
        final JPanel dialogPanel = new JPanel(new BorderLayout());

        dialogPanel.setLayout(layout);
        dialogPanel.add(new JLabel("Enter PIN:"));
        dialogPanel.add(pinField);
        final String pinConnectionURL = "https://dev.azure.com/" + tenantName + "/Core/_apps/hub/7pace.Timetracker.Apps";
        final JTextField tf = new JTextField(pinConnectionURL);
        tf.addMouseListener((MouseClickListener) e -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(pinConnectionURL));
                }
            } catch (Exception ex) {
                log.debug("Failed to open pin connection interface on url: '" + pinConnectionURL + "' with error: " + ex.getMessage());
            }
        });
        tf.setCaretColor(JBColor.BLUE);
        tf.setEditable(false);
        dialogPanel.add(tf);

        return dialogPanel;
    }
}
