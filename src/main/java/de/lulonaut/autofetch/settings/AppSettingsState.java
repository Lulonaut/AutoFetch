package de.lulonaut.autofetch.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "de.lulonaut.autofetch.settings.AppSettingsState", storages = @Storage("autofetchSettings.xml"))
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {
    public boolean enabledStatus = true;
    public int autoFetchDelay = 5;

    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    public int getAutoFetchDelay() {
        return autoFetchDelay;
    }

    public void setAutoFetchDelay(int autoFetchDelay) {
        this.autoFetchDelay = autoFetchDelay;
    }

    public boolean getEnabledStatus() {
        return enabledStatus;
    }

    public void setEnabledStatus(boolean enabledStatus) {
        this.enabledStatus = enabledStatus;
    }

    @Override
    public @Nullable AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
