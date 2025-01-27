package eu.faircode.xlua.x.ui.models;

import android.app.Application;

import eu.faircode.xlua.x.ui.core.model.ListRepoViewModel;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;

public class SettingsExContainerViewModel  extends ListRepoViewModel<SettingsContainer> {
    public SettingsExContainerViewModel(Application application) {
        super(application, "xlua_settings_ex_hooks");   //SettingsGroupRepository.INSTANCE
    }
}
