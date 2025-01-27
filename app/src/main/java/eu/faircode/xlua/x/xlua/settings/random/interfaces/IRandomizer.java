package eu.faircode.xlua.x.xlua.settings.random.interfaces;

import java.util.List;

import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;

public interface IRandomizer {
    boolean hasOptions();

    boolean hasParentControl();

    String getParentControlName();

    boolean isParentControl();

    List<String> getRequirements(String settingName);
    boolean hasRequirements(String settingName);


    boolean isOption();


    List<String> getSettings();

    List<IRandomizer> getOptions();

    String getRawValue();

    //boolean hasOption(String op);
    boolean hasSetting(String settingName);

    String getDisplayName();

    void randomize(RandomizerSessionContext context);
}
