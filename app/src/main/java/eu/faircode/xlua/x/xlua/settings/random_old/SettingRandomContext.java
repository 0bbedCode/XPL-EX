package eu.faircode.xlua.x.xlua.settings.random_old;

import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.interfaces.IValueDescriptor;
import eu.faircode.xlua.x.xlua.settings.interfaces.NameInformationTypeBase;

/**
 * For one lets try to keep this Class as Simple as Possible
 * All this class does is provide context to the Target Setting when it comes to Randomization
 * The Holder should never be taken out (so actions upon it should be handled here) and helps in the Context of Multiple Settings / Controllers Trying to Randomize a already Randomized Setting
 * This only is suppose to be used during the Stage of doing the Randomization disposed of once Randomization is Done
 * This only is suppose to be used in Connection with the {.SettingsContext} Class as that is more the Overall Context of everything all settings while this class is Context for a setting within
 */

public class SettingRandomContext extends NameInformationTypeBase implements IValueDescriptor {
    public static SettingRandomContext create(SettingHolder holder) { return new SettingRandomContext(holder); }

    private SettingHolder holder;
    //Maybe the Instance of Random ?
    private String randomValue;
    private boolean wasRandomized = false;
    @Override
    public boolean isValid() { return holder != null && holder.hasNameInformation(); }
    public boolean wasRandomized() { return wasRandomized; }


    @Override
    public String getValue() { return holder.getValue(); }

    @Override
    public String getNewValue() { return randomValue; }

    @Override
    public String getDescription() { return holder.getDescription(); }

    public void setNewValue(String newValue) { randomValue = newValue; }

    public void updateHolder() {
        //Determine if the Holder should be updated ?
        holder.setNewValue(randomValue);
        //holder.ensureInputUpdated();
    }


    public SettingRandomContext(SettingHolder holder) {
        if(holder != null && holder.hasNameInformation()) {
            super.bindNameInformation(holder.getNameInformation());
            this.holder = holder;
        }
    }
}
