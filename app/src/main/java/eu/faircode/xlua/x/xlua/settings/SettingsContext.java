package eu.faircode.xlua.x.xlua.settings;

import java.util.List;

import eu.faircode.xlua.x.xlua.settings.random_old.SettingRandomContext;

/**
 * Class to Hold the actual Context of the Randomization Request
 *
 * Hmm if its a parent...
 * I think we can follow something similar to how we do the "parent" system with Container System / init of Settings ?
 * Hmmm
 * From the "randomize" function, we just want a list of settings in OUR group ? so lets organize into Groups ?
 * Lets make a Overall Context Here ? then have built in Thread Lock for Sub Randomization Sessions or something ?
 * Then from "randomize" when we "need" something we can request a "session" or Randomize Context Wrapper from it ?
 *
 * ..
 * If we do first half then perhaps we can rid of Randomize Context Wrapper ? maybe just flag it somewhere or bin it ?
 * Perhaps in the Actual Session we can have a List of the "already" randomized and go from there if needed
 * Perhaps a "que" for those who have been not saved ? ... nah I just create session each randomize
 *
 * If none checked randomize will act like ?
 *  Randomize Blocks first Block Phone Type : SoC links etc its a Chain until its Settings that don't matter or are unique
 *
 */
public class SettingsContext {
    private final NameInformationMap<SettingHolder> settings = new NameInformationMap<>();
    private final NameInformationMap<SettingHolder> enabledSettings = new NameInformationMap<>();
    private final NameInformationMap<SettingHolder> parentsSettings = new NameInformationMap<>();

    //Need a way to tell what ones are indexed now, then simply update both

    //Should we when invoked randomize create context for all every setting or create as we randomize ?
    //RN Its create as we randomize
    private final NameInformationMap<SettingRandomContext> randomizedCopy = new NameInformationMap<>();

    private SettingHolder buildParentControlHolder;
    //private SettingHolder socParentControlHolder;

    public SettingsContext(List<SettingHolder> settings) {
        for(SettingHolder setting : settings) {
            if(setting.isValid()) {
                this.settings.put(setting);
                if(setting.isEnabled() && !setting.hasChildren()) //Do this ???
                    enabledSettings.put(setting);
                if(setting.getName().equalsIgnoreCase("android.build.parent.control"))
                    buildParentControlHolder = setting;
                else if(setting.hasChildren())
                    parentsSettings.put(setting);
            }
        }
    }

    public SettingHolder getSetting(String settingName)  { return settings.get(settingName); }

    public SettingHolder getParent(String settingName) { return parentsSettings.get(settingName); }

    public boolean hasSetting(String settingName) { return settings.hasName(settingName); }
    public boolean hasSetting(SettingHolder setting) { return settings.hasName(setting); }

    public boolean isSettingEnabled(String settingName) { return enabledSettings.hasName(settingName); }
    public boolean isSettingEnabled(SettingHolder setting) { return enabledSettings.hasName(setting); }

    public boolean wasSettingRandomized(String settingName) { return randomizedCopy.hasName(settingName); }
    public boolean wasSettingRandomized(SettingRandomContext setting) { return randomizedCopy.hasName(setting); }

    public boolean isParentSetting(String settingName) { return parentsSettings.hasName(settingName); }
    public boolean isParentSetting(SettingHolder setting) { return parentsSettings.hasName(setting); }

    public void clearRandomized(boolean updateHolders) {
        if(updateHolders) for(SettingRandomContext randomContext : randomizedCopy.getNameInformationList()) randomContext.updateHolder();
        randomizedCopy.clear();
    }

    public boolean randomizeSingle(SettingHolder setting) {
        //return setting != null && setting.randomize(this) && randomizedCopy.hasName(setting.getName());
        return false;
    }

    public int randomize() { return randomize(true); }
    public int randomize(boolean onlyChecked) {
        /*buildParentControlHolder.randomize(this);
        if(!buildParentControlHolder.randomize(this)) return 0;
        NameInformationMap<SettingHolder> settings = onlyChecked ? enabledSettings : this.settings;
        int randomized = 0;
        for(SettingHolder holder : settings.getNameInformationList()) {
            if(!holder.hasChildren()) {
                holder.randomize(this);
                randomized++;
            }
        }

        return randomized;*/
        return 0;
    }

    public String updateOrPutRandomized(String settingName, String newValue) {
        if(settingName == null) return null;
        SettingHolder holder = settings.get(settingName);
        if(holder == null) return null;
        SettingRandomContext old = randomizedCopy.get(settingName);
        if(old == null) {
            SettingRandomContext randomContext = new SettingRandomContext(holder);
            randomContext.setNewValue(newValue);
            randomizedCopy.put(randomContext);
        } else {
            return holder.getNewValue();
        }

        return null;
    }

    public void putRandomized(String settingName, String newValue, boolean replaceOld) {
        if(settingName == null) return;
        SettingHolder holder = settings.get(settingName);
        if(holder == null) return;
        SettingRandomContext old = randomizedCopy.get(settingName);
        if(old == null || replaceOld) {
            SettingRandomContext randomContext = new SettingRandomContext(holder);
            randomContext.setNewValue(newValue);
            randomizedCopy.put(randomContext);
        }
    }
}
