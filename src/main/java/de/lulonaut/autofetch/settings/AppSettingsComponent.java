package de.lulonaut.autofetch.settings;

import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

public class AppSettingsComponent {
    private JPanel mainPanel;
    private JBCheckBox enabledStatus;
    private JBIntSpinner jbIntSpinner;

    public AppSettingsComponent() {
        enabledStatus = new JBCheckBox("Enable periodic fetching of all git remotes", AppSettingsState.getInstance().getEnabledStatus());
        jbIntSpinner = new JBIntSpinner(AppSettingsState.getInstance().getAutoFetchDelay(), 1, 60);
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(enabledStatus, 1)
                .addLabeledComponent(new JLabel("Refresh delay in minutes (Takes effect after IDE restart)"), jbIntSpinner, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public boolean getEnabledStatus() {
        return enabledStatus.isSelected();
    }

    public void setEnabledStatus(boolean newStatus) {
        enabledStatus.setSelected(newStatus);
    }

    public int getDelayValue() {
        return jbIntSpinner.getNumber();
    }

    public void setDelayValue(int value) {
        jbIntSpinner.setValue(value);
    }
}
