package net.kraschitzer.intellij.plugin.sevenpace.view;

import com.intellij.openapi.ui.DialogWrapper;
import com.sun.istack.Nullable;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class StartOnBranchCheckoutDialog extends DialogWrapper {

    private final JTextField workItemId;

    public StartOnBranchCheckoutDialog(Integer workItemId) {
        super(true);
        this.workItemId = new JTextField(workItemId);
        this.workItemId.setEditable(false);
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
        final GridLayout layout = new GridLayout(3, 1);
        final JPanel dialogPanel = new JPanel(new BorderLayout());

        dialogPanel.setLayout(layout);
        dialogPanel.add(new JLabel("Start tracking of the work item linked to the checked out branch?"));
        dialogPanel.add(new JLabel("Workitem id:"));
        dialogPanel.add(workItemId);

        return dialogPanel;
    }
}
