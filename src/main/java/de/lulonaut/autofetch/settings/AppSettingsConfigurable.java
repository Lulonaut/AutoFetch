package de.lulonaut.autofetch.settings;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import de.lulonaut.autofetch.AutoFetch;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent settingsComponent;

    public AppSettingsConfigurable() {

    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "AutoFetch";
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsComponent = new AppSettingsComponent();
        return settingsComponent.getMainPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settingsState = AppSettingsState.getInstance();
        boolean modified = settingsComponent.getEnabledStatus() != settingsState.enabledStatus;
        modified |= settingsComponent.getDelayValue() != settingsState.autoFetchDelay;
        return modified;
    }

    @Override
    public void apply() {
        AppSettingsState settingsState = AppSettingsState.getInstance();
        settingsState.enabledStatus = settingsComponent.getEnabledStatus();
        settingsState.autoFetchDelay = settingsComponent.getDelayValue();
    }

    @Override
    public void reset() {
        AppSettingsState settingsState = AppSettingsState.getInstance();
        settingsComponent.setEnabledStatus(settingsState.getEnabledStatus());
        settingsComponent.setDelayValue(settingsState.getAutoFetchDelay());
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
