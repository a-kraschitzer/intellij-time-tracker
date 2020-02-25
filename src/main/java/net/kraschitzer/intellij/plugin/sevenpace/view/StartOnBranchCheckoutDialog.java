package net.kraschitzer.intellij.plugin.sevenpace.view;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.sun.istack.Nullable;
import lombok.extern.slf4j.Slf4j;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.ActivityTypeSetting;
import net.kraschitzer.intellij.plugin.sevenpace.model.api.response.ActivityTypeSettings;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class StartOnBranchCheckoutDialog extends DialogWrapper {

    private final JTextField textFieldWorkItemId;
    private final JComboBox<ActivityTypeSetting> comboBoxActivityType;
    private final Integer workItemId;
    private final ActivityTypeSettings settings;

    public StartOnBranchCheckoutDialog(Integer workItemId, ActivityTypeSettings settings) {
        super(true);
        this.workItemId = workItemId;
        this.settings = settings;
        textFieldWorkItemId = new JTextField();
        comboBoxActivityType = new ComboBox<>();
        init();
        setTitle("Start Tracking on Branch Work Item?");
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction()};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        textFieldWorkItemId.setText(workItemId.toString());
        textFieldWorkItemId.setEditable(false);
        for (ActivityTypeSetting setting : settings.getActivityTypes()) {
            comboBoxActivityType.addItem(setting);
            if (setting.getIsDefault()) {
                comboBoxActivityType.setSelectedItem(setting);
            }
        }

        final GridLayout layout = new GridLayout(4, 1);
        final JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.setLayout(layout);
        dialogPanel.add(new JLabel("Start tracking of the work item linked to the checked out branch?"));
        dialogPanel.add(new JLabel("Workitem id:"));
        dialogPanel.add(textFieldWorkItemId);
        dialogPanel.add(comboBoxActivityType);

        return dialogPanel;
    }

    public ActivityTypeSetting getSelectedActivityType() {
        return (ActivityTypeSetting) comboBoxActivityType.getSelectedItem();
    }
}
