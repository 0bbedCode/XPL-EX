package eu.faircode.xlua.x.xlua.settings.random_old;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContext;
import eu.faircode.xlua.x.xlua.settings.deprecated.SettingsHelperOld;
import eu.faircode.xlua.x.xlua.settings.random_old.extra.OptionFiller;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.ILinkParent;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;



/*public class RandomElement implements IRandomizer {
    private String displayName;
    private final List<String> settingNames = new ArrayList<>();

    private final Map<String, IRandomizer> options = new HashMap<>();
    protected IRandomizer selectedOption;
    protected boolean useFillerOption = true;
    protected boolean requiresNewInstance = false;
    protected boolean isParentControl = false;
    //protected boolean isParentControl = false;
    //We can cache in name information ?

    //setting one => if has parent => parent => if has parent => parent (keep following it til the final parent ?)
    //Then reverse the steps back ?? so we can along the way set settings or not ?
    protected String parentSettingName;

    @Override
    public String randomString() {
        return null;
    }

    //public boolean invokeParent()
    //We from the "parent" need to set / prepare settings then below we can handle rest ?
    //like



    public boolean ensureParentIsHappy(SettingsContext context) {
        SettingHolder parent = context.getParent(parentSettingName);
        if(parent == null) return true;
        if(!context.wasSettingRandomized(parent.getName())) {
            //check if checked ?
            //take priority in checked first ?
            //If ONE is checked then consider that group checked ?
            context.putRandomized(parentSettingName, "invoked", true);
            return parent.randomize(context);
        }

        return true;//this functtion is dumb
    }

    @Override
    public boolean randomize(SettingsContext context, SettingHolder callingSetting) {
        if(!ensureParentIsHappy(context)) return false;


        //No matter what we need "context" so we will have to boost this to overide function
        //Like we need to either (a) set field to Object cast to whatever type or (b) move this up as
        //Example since we are the parent we should be generating the Data but only the parent knows how to example ISP Controller
        //this his to help SYNC it
        //
        //Generate a Random Build ?
        //Then Get SoC for Build or Random SoC info
        //
        //Ensure it can go other way around. If they want to Generate a Build from SoC allow so if SoC control is the only one checked
        //
        //


        //So how about we first go through the child settings
        //then putRandomized(settingName, newValue) or something
        //THEN we

        if(isParentControl) {
            for(String child : settingNames) {
                SettingHolder holder = context.getSetting(child);
                holder.randomize(context);
            }
        } else {

        }

        if(!isParentControl) {
            context.putRandomized(settingName, randomString(), );
        }

        if(isParentControl) {
            for(String child : settingNames) {
                context.updateOrPutRandomized(null, )
            }

            //We can also
            //Just randomize all data here ?
        } else {

        }

        //lets say we are a parnent
        //
    }
}
*/

public class RandomElement implements IRandomizer {
    private String mDisplayName;
    private final List<String> mSettings = new ArrayList<>();
    private final Map<String, IRandomizer> mOptions = new HashMap<>();
    private IRandomizer mSelectedOption = null;
    private boolean mIsCheckableRandom = true;
    private boolean mRequiresNewInstance = false;
    private boolean mRequireFillerOption = true;
    private boolean mIsParentControlOverride = false;

    private boolean isParent = false;
    private String parentSettingName = null;

    public void setIsCheckableRandom(boolean isCheckableRandom) { this.mIsCheckableRandom = isCheckableRandom; }
    public void setRequiresNewInstance(boolean requiresNewInstance) { this.mRequiresNewInstance = requiresNewInstance; }
    public void setRequireFillerOption(boolean requireFillerOption) { this.mRequireFillerOption = requireFillerOption; }
    public void setIsParentControlOverride(boolean isParentControl) { this.mIsParentControlOverride = isParentControl; }

    public RandomElement(String displayName) { this.mDisplayName = displayName; }
    public RandomElement(RandomElement old) {
        if(old != null) {
            this.mDisplayName = old.mDisplayName;
            this.mSettings.addAll(old.mSettings);
            this.mOptions.putAll(old.mOptions);
            this.mSelectedOption = old.mSelectedOption;
            this.mIsCheckableRandom = old.mIsCheckableRandom;
            this.mRequiresNewInstance = false;
            this.mRequireFillerOption = old.mRequireFillerOption;
            this.mIsParentControlOverride = old.mIsParentControlOverride;
            //this.parentSettingName = old.mParentSettingName;
        }
    }

    public void bindSettings(String... settings) { for(String s : settings)  bindSetting(s); }
    public void bindSetting(String settingName) {
        String trimmed = Str.trimControlChars(settingName).toLowerCase();
        if(!TextUtils.isEmpty(trimmed) && !mSettings.contains(trimmed)) mSettings.add(trimmed);
    }

    public void bindOptions(IRandomizer... options) { for(IRandomizer op : options)  bindOption(op); }
    public void bindOption(IRandomizer option) {
        if(option == null || option.isOptionFiller() || mOptions.containsValue(option)) return;
        ensureOptionsReady();
        mOptions.put(option.getDisplayName(), option);
        if(mSelectedOption == null) mSelectedOption = option;
    }

    public void bindParent(String settingParent) {
        if(TextUtils.isEmpty(settingParent)) return;
        this.parentSettingName = settingParent;
    }

    public void isParent(boolean isParent) {
        this.isParent = isParent;
    }

    @Override
    public IRandomizer ensureInstance() { return requiresNewInstance() ? new RandomElement(this) : this; }

    @Override
    public boolean requiresNewInstance() { return this.mRequiresNewInstance; }

    //@Override
    public boolean isCheckableRandom() { return this.mIsCheckableRandom; }

    @Override
    public String getDisplayName() { return mDisplayName; }

    @Override
    public RandomizerKind getKind() {
        return null;
    }

    @Override
    public List<String> getSettings() { return mSettings; }

    @Override
    public boolean hasOptions() {
        return false;
    }

    //@Override
    //public String getFirstSetting() { return !mSettings.isEmpty() ? mSettings.get(0) : ""; }

    @Override
    public boolean containsSetting(String settingName) {
        if(TextUtils.isEmpty(settingName) || !ListUtil.isValid(mSettings)) return false;
        if(settingName.startsWith("|") && settingName.length() > 1) {
            String endsWith = settingName.substring(1).toLowerCase();
            for(String s : mSettings) {
                if(s.endsWith(endsWith))
                    return true;
            }

            return false;
        } else {
            return mSettings.contains(settingName.toLowerCase());
        }
    }

    @Override
    public String getSetting() {
        return "";
    }

    @Override
    public String getSetting(int index) {
        return "";
    }

    //@Override
    public boolean hasSettings() { return !mSettings.isEmpty(); }

    @Override
    public List<IRandomizer> getOptions(boolean filterOutFillers, boolean removeCurrentOptionFromList) {
        Map<String, IRandomizer> ops = filterOutFillers ? ListUtil.filterMapByValue(mOptions, OptionFiller.INSTANCE, false) : mOptions;
        List<IRandomizer> optionList = ListUtil.copyToList(ops.values());
        if(removeCurrentOptionFromList && mSelectedOption != null && optionList.contains(mSelectedOption)) optionList.remove(mSelectedOption);
        return optionList;
    }

    //@Override
    public boolean isSingleOption() { return mOptions.isEmpty() || mOptions.size() == 1; }

    //@Override
    public int optionCount() { return mOptions.size(); }

    //@Override
    public boolean hasSelectedOption() { return mSelectedOption != null; }

    @Override
    public IRandomizer getSelectedOption() { return mSelectedOption; }

    @Override
    public void setSelectedOption(IRandomizer option) { this.mSelectedOption = option; }

    @Override
    public void setSelectedOption(String selectedOptionKey) {

    }

    //@Override
    /*public IRandomizer setSelectedOption(String optionKey) {
        if(TextUtils.isEmpty(optionKey) || !ListUtil.isValid(mOptions)) return mSelectedOption;
        if(!mOptions.containsKey(optionKey)) return mSelectedOption;
        IRandomizer randomizer = mOptions.get(optionKey);
        if(randomizer != null) mSelectedOption = randomizer;
        return mSelectedOption;
    }*/

    @Override
    public IRandomizer randomizeSelectedOption() {
        IRandomizer newRandom = RandomEnsurer.ensureRandom(new RandomEnsurer.IInvokeRandom<IRandomizer>() {
            final List<IRandomizer> options = getOptions(true, true);
            final IRandomizer original = mSelectedOption;

            @Override
            public boolean canRandomize() { return ListUtil.isValid(options); }

            @Override
            public IRandomizer randomize() {
                if(!canRandomize()) return original;
                if(options.size() == 1) return options.get(0);
                return RandomGenerator.nextElement(options);
            }

            @Override
            public boolean isSame(IRandomizer newObject) { return Objects.equals(original, newObject); }

            @Override
            public IRandomizer getOriginal() { return original; }
        });

        if(newRandom != null && !newRandom.equals(mSelectedOption)) this.mSelectedOption = newRandom;
        return newRandom;
    }

    @Override
    public boolean isOptionFiller() { return this instanceof OptionFiller || "select option".equalsIgnoreCase(mDisplayName) || "n/a".equalsIgnoreCase(mDisplayName); }

    //@Override
    public boolean isParentControl() { return (this instanceof ILinkParent) || (containsSetting("|.parent") || mIsParentControlOverride);  }

    @Override
    public String generateString() { return mSelectedOption.generateString(); }

    public boolean ensureParentIsHappy(SettingsContext context) {
        SettingHolder parent = context.getParent(parentSettingName);
        if(parent == null) return true;
        if(!context.wasSettingRandomized(parent.getName())) {
            //check if checked ?
            //take priority in checked first ?
            //If ONE is checked then consider that group checked ?

            //context.putRandomized(parentSettingName, "invoked", true);
            //return parent.randomize(context);
        }

        return true;//this functtion is dumb
    }


    @Override
    public boolean randomize(SettingsContext context, String settingName) {
        ensureParentIsHappy(context);
        return false;
        //if(isParent) return null;
        //return context.putRandomized(settingName, generateString(), true);
    }

    //@Override
    //public SettingRandomContextOld getCurrentSettingContext(SettingsContextOld context) { return context.getFirst(getFirstSetting(), true); }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) {
            String s = (String) obj;
            return SettingsHelperOld.seemsLikeSetting(s) && ListUtil.isValid(mSettings) ? mSettings.contains(s.toLowerCase())
                    :  Str.areEqual(s, this.mDisplayName, false, true);
        }

        if(obj instanceof IRandomizer) {
            IRandomizer ran = (IRandomizer) obj;
            return (ran.isOptionFiller() && this.isOptionFiller()) || (Str.areEqual(this.mDisplayName, ran.getDisplayName(), false, true));
        }

        return false;
    }

    private void ensureOptionsReady() {
        if(mRequireFillerOption && mOptions.isEmpty()) {
            IRandomizer fill = OptionFiller.INSTANCE;
            mOptions.put(fill.getDisplayName(), fill);
            mSelectedOption = fill;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Display Name", this.mDisplayName)
                .appendFieldLine("Option Count", this.mOptions.size())
                .appendFieldLine("Settings Count", this.mSettings.size())
                .appendFieldLine("Settings", "(" + Str.joinList(this.mSettings) + ")")
                .appendFieldLine("Current Option", Str.toStringOrNull(this.mSelectedOption))
                .appendFieldLine("Requires New Instance", this.mRequiresNewInstance)
                .appendFieldLine("Requires Filler Option", this.mRequireFillerOption)
                .appendFieldLine("Is Checkable Random", this.mIsCheckableRandom)
                .toString(true);
    }
}
