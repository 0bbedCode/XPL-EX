package eu.faircode.xlua.x.xlua.settings.random_old.interfaces;

import java.util.List;

import eu.faircode.xlua.x.xlua.settings.deprecated.SettingExtendedOld;

public interface IParentIndexerOld {
    boolean hasChildSettings();
    String getCategory();
    List<SettingExtendedOld> getChildSettings();
}
