package net.kraschitzer.intellij.plugin.time_tracker.view;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.ActivityTypeSetting;
import net.kraschitzer.intellij.plugin.time_tracker.model.api.response.ActivityTypeSettings;
import net.kraschitzer.intellij.plugin.time_tracker.utils.IntegerDocumentFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;

@Slf4j
public class StartTrackingDialog extends DialogWrapper {

    private final JLabel labelHeader;
    private final JTextField textFieldWorkItemId;
    private final JComboBox<ActivityTypeSetting> comboBoxActivityType;

    public StartTrackingDialog(String title, String header, String workItemId, ActivityTypeSettings settings) {
        super(true);
        labelHeader = new JLabel();
        textFieldWorkItemId = new JTextField();
        comboBoxActivityType = new ComboBox<>();

        init();

        setTitle(title);
        labelHeader.setText(header);
        textFieldWorkItemId.setText(workItemId);
        if (textFieldWorkItemId.getText().isEmpty()) {
            textFieldWorkItemId.setText("0");
        }
        for (ActivityTypeSetting setting : settings.getActivityTypes()) {
            comboBoxActivityType.addItem(setting);
            if (setting.getIsDefault()) {
                comboBoxActivityType.setSelectedItem(setting);
            }
        }
    }

    @Override
    @NotNull
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction()};
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        ((PlainDocument) textFieldWorkItemId.getDocument()).setDocumentFilter(new IntegerDocumentFilter());

        final GridLayout layout = new GridLayout(4, 1);
        final JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setLayout(layout);
        dialogPanel.add(labelHeader);
        dialogPanel.add(new JLabel("Workitem id:"));
        dialogPanel.add(textFieldWorkItemId);
        dialogPanel.add(comboBoxActivityType);

        return dialogPanel;
    }

    public ActivityTypeSetting getSelectedActivityType() {
        return (ActivityTypeSetting) comboBoxActivityType.getSelectedItem();
    }

    public Integer getWorkItemId() {
        return Integer.parseInt(textFieldWorkItemId.getText());
    }
}
