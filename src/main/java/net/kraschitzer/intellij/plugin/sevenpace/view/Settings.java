package net.kraschitzer.intellij.plugin.sevenpace.view;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.sevenpace.communication.ICommunicator;
import net.kraschitzer.intellij.plugin.sevenpace.communication.exceptions.CommunicatorException;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.*;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.PinStatusEnum;
import net.kraschitzer.intellij.plugin.sevenpace.utils.SettingKeys;
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

    private JPanel contentPanel;
    private JPanel infoPanel;
    private JTextField textFieldName;
    private JTextField textFieldEmail;
    private JTextField textFieldProjectId;
    private JLabel labelError;


    private final ICommunicator communicator;
    private PropertiesComponent props;

    private String url;

    public Settings() {
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
        props = PropertiesComponent.getInstance();
        communicator = ICommunicator.getInstance();

        url = props.getValue(SettingKeys.URL);
        textFieldUrl.setText(url != null ? url : "");
        try {
            communicator.initialize();
            communicator.authenticate();
        } catch (Exception ex) {
        }
        populateUserInformation();
    }


    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "7PaceTimeTracker";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return contentPanel;
    }

    @Override
    public boolean isModified() {
        return !textFieldUrl.getText().equals(url);
    }

    @Override
    public void apply() throws ConfigurationException {
        saveInput();
        clearCachedInfo();
        try {
            communicator.resetInitialization();
            communicator.initialize();
        } catch (CommunicatorException e) {
            setError("Failed to initialize communicator.");
            return;
        }
    }

    private void saveInput() throws ConfigurationException {
        url = textFieldUrl.getText();
        try {
            InetAddress.getByName(url);
        } catch (UnknownHostException e) {
            throw new ConfigurationException("The given url '" + url + "' is invalid!");
        }
        props.setValue(SettingKeys.URL, url);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private void generatePin() {
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

        final PinContext pin = communicator.pinCreate();
        final SampleDialogWrapper dialog = new SampleDialogWrapper(pin.getPin());
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
                Token token = null;
                try {
                    token = communicator.token(pin.getSecret());
                } catch (CommunicatorException e) {
                    e.printStackTrace();
                }
                props.setValue(SettingKeys.ACCESS_TOKEN, token.getAccess_token());
                props.setValue(SettingKeys.REFRESH_TOKEN, token.getRefresh_token());
                props.setValue(SettingKeys.EXPIRES, String.valueOf(token.getExpires().toInstant(ZoneOffset.UTC).toEpochMilli()));
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
        props.setValue(SettingKeys.ACCESS_TOKEN, "");
        props.setValue(SettingKeys.REFRESH_TOKEN, "");
        props.setValue(SettingKeys.EXPIRES, "");
    }

    private void setError(String msg) {
        labelError.setText(msg);
    }

    private void resetError() {
        labelError.setText("");
    }
}
