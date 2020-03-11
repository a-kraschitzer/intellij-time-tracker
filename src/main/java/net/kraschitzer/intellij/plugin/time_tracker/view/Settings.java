package net.kraschitzer.intellij.plugin.time_tracker.view;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.time_tracker.communication.ICommunicator;
import net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions.ComHostNotFoundException;
import net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions.ComHostUnknownException;
import net.kraschitzer.intellij.plugin.time_tracker.communication.exceptions.CommunicatorException;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.*;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.enums.PinStatusEnum;
import net.kraschitzer.intellij.plugin.time_tracker.model.enums.StartTrackingBehaviour;
import net.kraschitzer.intellij.plugin.time_tracker.persistence.SettingsState;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneOffset;

@Slf4j
public class Settings implements Configurable {

    private JTextField textFieldUrl;
    private JButton buttonGeneratePin;

    private JComboBox<StartTrackingBehaviour> comboBoxBranchCheckoutBehaviour;
    private JComboBox<StartTrackingBehaviour> comboBoxOnActivityBehaviour;

    private JPanel contentPanel;
    private JPanel infoPanel;
    private JTextField textFieldName;
    private JTextField textFieldEmail;
    private JTextField textFieldProjectId;
    private JLabel labelError;
    private JSpinner spinnerAutoActionDelay;
    private JCheckBox checkBoxEnableAutoStop;

    private final ICommunicator communicator;
    private SettingsState settings = ServiceManager.getService(SettingsState.class);

    private String url;

    public Settings() {
        initializeComponents();
        communicator = ICommunicator.getInstance();

        try {
            communicator.initialize();
            communicator.authenticate();
            populateUserInformation();
        } catch (Exception ignored) {
            //ignored
        }
    }

    public void initializeComponents() {
        for (StartTrackingBehaviour beh : StartTrackingBehaviour.values()) {
            comboBoxBranchCheckoutBehaviour.addItem(beh);
            comboBoxOnActivityBehaviour.addItem(beh);
        }
        comboBoxBranchCheckoutBehaviour.setSelectedItem(settings.branchCheckoutBehaviour);
        comboBoxOnActivityBehaviour.setSelectedItem(settings.onActivityBehaviour);
        buttonGeneratePin.addActionListener(e -> generatePin());
        textFieldUrl.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                resetError();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resetError();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                resetError();
            }
        });
        url = settings.url;
        textFieldUrl.setText(url != null ? url : "");
        spinnerAutoActionDelay.setValue(settings.autoStopActionDelay);
        checkBoxEnableAutoStop.setSelected(settings.autoStop);
    }


    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "TimeTracker (7Pace)";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return contentPanel;
    }

    @Override
    public boolean isModified() {
        return (!textFieldUrl.getText().equals(url)
                || !settings.branchCheckoutBehaviour.equals(comboBoxBranchCheckoutBehaviour.getSelectedItem())
                || !settings.onActivityBehaviour.equals(comboBoxOnActivityBehaviour.getSelectedItem())
                || settings.autoStopActionDelay != (int) spinnerAutoActionDelay.getValue()
                || settings.autoStop != checkBoxEnableAutoStop.isSelected()
        );
    }

    @Override
    public void apply() throws ConfigurationException {
        saveInput();
        communicator.resetInitialization();
        populateUserInformation();
        try {
            communicator.initialize();
        } catch (CommunicatorException e) {
            setError("Failed to initialize communicator.");
        }
    }

    private void saveInput() throws ConfigurationException {
        url = textFieldUrl.getText();
        try {
            InetAddress.getByName(url);
        } catch (UnknownHostException e) {
            throw new ConfigurationException("The given url '" + url + "' is invalid!");
        }
        settings.url = url;
        settings.branchCheckoutBehaviour = (StartTrackingBehaviour) comboBoxBranchCheckoutBehaviour.getSelectedItem();
        settings.onActivityBehaviour = (StartTrackingBehaviour) comboBoxOnActivityBehaviour.getSelectedItem();
        settings.autoStopActionDelay = (Integer) spinnerAutoActionDelay.getValue();
        settings.autoStop = checkBoxEnableAutoStop.isSelected();
    }

    private void createUIComponents() {
    }

    private void generatePin() {
        resetError();
        try {
            saveInput();
            clearCachedInfo();
            communicator.resetInitialization();
            communicator.initialize();
        } catch (ConfigurationException e) {
            setError("The given url '" + url + "' is invalid!");
            return;
        } catch (CommunicatorException e) {
            setError("Failed to initialize communicator.");
            return;
        }

        final PinContext pin;
        try {
            pin = communicator.pinCreate();
        } catch (ComHostNotFoundException e) {
            setError("The given host does not support 7pace PIN creation!");
            return;
        } catch (ComHostUnknownException e) {
            setError("The given url '" + url + "' is invalid!");
            return;
        } catch (CommunicatorException e) {
            setError("Failed to create PIN.");
            return;
        }
        final PINDialog dialog = new PINDialog(pin.getPin(), url.split(".timehub.7pace.com")[0]);
        Thread t = new Thread(() -> {
            PinStatus status = communicator.pinStatus(pin.getSecret());
            while (PinStatusEnum.Validating.equals(status.getStatus()) && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(500);
                    status = communicator.pinStatus(pin.getSecret());
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            if (PinStatusEnum.Validated.equals(status.getStatus())) {
                final Token token;
                try {
                    token = communicator.token(pin.getSecret());
                } catch (CommunicatorException e) {
                    setError("Failed to generate token, please try again.");
                    return;
                }
                settings.accessToken = token.getAccess_token();
                settings.refreshToken = token.getRefresh_token();
                settings.expires = String.valueOf(token.getExpires().toInstant(ZoneOffset.UTC).toEpochMilli());
            }
            dialog.setValidated();
        });
        t.start();
        dialog.show();
        t.interrupt();
        populateUserInformation();
    }

    private void populateUserInformation() {
        try {
            TrackingStateModel tsm = communicator.getCurrentState(true);
            if (tsm != null && tsm.getSettings() != null && tsm.getSettings().getUserInfo() != null) {
                UserInfo userInfo = tsm.getSettings().getUserInfo();
                textFieldName.setText(userInfo.getUserName());
                textFieldEmail.setText(userInfo.getUserUniqueName());
                textFieldProjectId.setText(userInfo.getProjectId());
                infoPanel.setVisible(true);
            } else {
                infoPanel.setVisible(false);
            }
        } catch (CommunicatorException e) {
            log.debug("Failed to populate user information, {}", e.getMessage());
            infoPanel.setVisible(false);
        }
    }

    private void clearCachedInfo() {
        settings.clear();
    }

    private void setError(String msg) {
        labelError.setText(msg);
    }

    private void resetError() {
        labelError.setText("");
    }
}
