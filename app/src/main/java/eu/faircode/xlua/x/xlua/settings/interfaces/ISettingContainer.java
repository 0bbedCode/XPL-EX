package eu.faircode.xlua.x.xlua.settings.interfaces;

import java.util.List;

import eu.faircode.xlua.x.xlua.settings.SettingHolder;

public interface ISettingContainer {
    String getGroupName();
    String getContainerName();
    List<SettingHolder> getSettings();
    void ensureHasDescription(String description);
}
