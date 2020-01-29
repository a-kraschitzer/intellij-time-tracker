package net.kraschitzer.intellij.plugin.sevenpace.view;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import net.kraschitzer.intellij.plugin.sevenpace.communication.Communicator;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.PinContext;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.PinStatus;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.Token;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.enums.PinStatusEnum;
import net.kraschitzer.intellij.plugin.sevenpace.utils.SettingKeys;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.time.ZoneOffset;

public class Settings implements Configurable {

    private JTextField urlField;
    private JButton generatePin;

    private JPanel contentPanel;

    private PropertiesComponent props;

    private String url;

    public Settings() {
        props = PropertiesComponent.getInstance();
        url = props.getValue(SettingKeys.URL);
        urlField.setText(url != null ? url : "");

        generatePin.addActionListener(e -> generatePin());
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
        return !urlField.getText().equals(url);
    }

    @Override
    public void apply() throws ConfigurationException {
        url = urlField.getText();
        props.setValue(SettingKeys.URL, url);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private void generatePin() {
        try {
            apply();
        } catch (ConfigurationException ex) {
            ex.printStackTrace();
        }
        final Communicator communicator = Communicator.getInstance();
        final PinContext pin = communicator.pinCreate();
        //log.debug("Retrieved new PIN: {} and secret: {}...", pin.getPin(), pin.getSecret().substring(0, 20));
        final SampleDialogWrapper dialog = new SampleDialogWrapper(pin.getPin());
        Thread t = new Thread(() -> {
            PinStatus status = communicator.pinStatus(pin.getSecret());
            while (PinStatusEnum.Validating.equals(status.getStatus()) && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(500);
                    status = communicator.pinStatus(pin.getSecret());
                    System.out.println("checked for status, status = " + status.getStatus());
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            if (PinStatusEnum.Validated.equals(status.getStatus())) {
                Token token = communicator.token(pin.getSecret());
                props.setValue(SettingKeys.ACCESS_TOKEN, token.getAccess_token());
                props.setValue(SettingKeys.REFRESH_TOKEN, token.getRefresh_token());
                props.setValue(SettingKeys.EXPIRES, String.valueOf(token.getExpires().toInstant(ZoneOffset.UTC).toEpochMilli()));
            }
            dialog.setValidated();
        });
        dialog.show();
        t.interrupt();
    }
}
