package eu.faircode.xlua.x.xlua.settings.deprecated;

import android.text.TextWatcher;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.settings.UiBindingsController;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;

public class SettingExtendedOld extends UiBindingsController {
    public SettingRandomContextOld createRandomContext() { return new SettingRandomContextOld(this); }
    //Have some global key identifier warning the user the setting has a global setting
    //So setting the global value will be ignored for the context value ?
    //Also add in Bool for (is Check save able or can Randomize)
    //Add option to se to %random%
    //Add some Setting Setter or something and as well a trigger / holder for affected hooks so it can "activate" hooks if wanted!
    //
    //Hmm have base to adjust to indexes ????? default is 0 ? hmm im cooking ....
    //Index will change the invoke a change handler to check for changes if UI needs a update .. hmm ye ye
    //This is ofc just INDEX
    //
    //When we see a indexable Setting or setting with a sister we will find the brother and set it internally to him remove the sister from the list
    //
    //Fix the treat null as empty as they can have a empty value but original is NULL so you need to update the DB value to the EMPTY String
    //

    //protected IndexCache currentIndex;
    //protected HashMap<Integer, IndexCache> indexCache = new HashMap<>();

    //Make a way to save the last selected OPTION

    protected IndexCacheFactory factory = null;
    //
    //
    //First The Parent of All Parents
    //hmm so this
    //IF a Setting Group has Children then Dynamically there Insert / Create the Parent Index Controller !
    //Then Link all of the Parent Settings to the Parent Index Controller (it can implement over this class)
    //
    //
    //Now lets get to the Randomizer this, back here we are
    //So in theory we can store (indexes) in the Randomizers
    //Select Index, then when If Randomized is Selected / Clicked it will first
    //
    //Parent Parent will Have the Randomizer ? No no no yes
    //Parent Setting will Hold its own Randomizer that will take in a arg something like => updateModified(IRandomizer.randomString())
    //This one is easy (hmm we need something with parent tho)
    //Hmm we can do so the Parent Control has => IRandomizer.randomize(parentControl, childrenSettings)
    //Do not need to worry about index as that is controlled else where
    //
    //
    //Now Randomizer for ACTUAL Option Based ones (not a parent controller)
    //
    //
    //[Option Control(IOptionControl)] => IRandomizer.getOptions() as List<IRandomizer>  (invoke) =>  this.updateModified(selected.IRandomizer.randomString)
    //[Parent Indexer(IParentIndexer, IOptionControl)] => IRandomizer.getOptions() as List<IRandomizer> (invoke) this.updateFocusIndex(selected.IRandomizer.randomInt)
    //[Parent Control(IParentControl, IOptionControl)] => IRandomizer.getOptions() as List<IRandomizer> (invoke) IRandomizer.randomize(this, childSettings)
    //[Generic(IRandomizer)] =>  this.updateModified(selected.IRandomizer.randomString)
    //
    //
    //String getDisplayName()
    //List<String> getSettings();
    //String getFirstSetting();
    //boolean containsSetting(String settingName);
    //
    //
    //List<IRandomizer> getOptions();
    //boolean hasOptions();
    //IRandomizer getSelectedOption();
    //void setSelectedOption(IRandomizer option);
    //IRandomizer setSelectedOption(String optionKey);
    //
    //String generateString();
    //List<SettingExtended> randomize(SettingExtended parent, List<SettingExtended> settings)
    //
    //
    //
    //
    //

    protected String name;
    protected String originalValue;
    protected String newValue;

    protected IRandomizer randomizer;

    //protected IRandomizerOld randomizer;
    //protected List<SettingExtended> children = new ArrayList<>();
    //public boolean hasChildrenSettings() { return !children.isEmpty(); }
    //public String getNewValueNonNullable() { return this.newValue == null ? "" : this.newValue; }
    //public String getOriginalValueNonNullable() { return this.originalValue == null ? "" : this.originalValue; }

    public String getName() { return this.name; }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getNewValue() {
        return "";
    }

    public String getCategory() { return SettingsHelperOld.getIndexCacheCategory(getCurrentIndex()); }
    public String getFriendlyName() { return SettingsHelperOld.getIndexCacheFriendlyName(getCurrentIndex()); }
    public String getDescription() { return SettingsHelperOld.getIndexDescription(getCurrentIndex()); }
    public String getNameWithoutIndex() { return SettingsHelperOld.getIndexCacheNameWithoutIndex(getCurrentIndex()); }
    public IRandomizer getRandomizer() { return randomizer; }

    public IndexCacheFactory getFactory() { return this.factory; }

    //isParentControl
    //isParentIndexer
    //isOptionBased
    //isGeneric

    public boolean isValid() { return factory != null && (factory.hasParent() || factory.hasCurrent()); }
    public boolean isValid(int index) { return isValid() && index > -1 && !factory.isCurrentIndex(index) && factory.hasIndex(index); }

    public int getIndex() { return isValid() ? factory.getCurrentIndex() : -1; }
    public IndexCache getCurrentIndex() { return isValid() ? factory.getCurrent() : null; }

    public SettingExtendedOld(List<SettingExtendedOld> controls, boolean parseAllControls) {
        //This is to Init a Setting THAT ONLY is for Controlling Index THATS ALL, so take the first Control Setting from the Category
        //Then Get all of its Index Caches to Parse for Index Controller
        this(controls.get(0).getCurrentIndex());
        if(parseAllControls) {
            //We just need to Parse the elements after the first cached and from the first setting
            List<IndexCache> caches = new ArrayList<>(controls.get(0).getFactory().getIndexCaches().values());
            if(caches.size() > 1) {
                for(int i = 1; i < caches.size(); i++)
                    factory.ensureIsCached(caches.get(i), true);
            }
        }
    }

    public SettingExtendedOld(LuaSettingExtended settingExtended) {
        if(settingExtended != null) {
            IndexCache cache = IndexCache.create(settingExtended.getName(), settingExtended.getValue(), settingExtended.getDescription(), this);
            this.factory = new IndexCacheFactory(cache, null);
        }
    }

    public SettingExtendedOld(List<IndexCache> indexCaches) {
        if(indexCaches != null) {
            this.factory = new IndexCacheFactory(indexCaches.get(0), this);
            if(indexCaches.size() > 1) {
                for(int i = 1; i < indexCaches.size(); i++)
                    this.factory.ensureIsCached(indexCaches.get(i), true);
            }
        }
    }

    public SettingExtendedOld(IndexCache indexCache) {
        if(indexCache != null) {
            this.factory = new IndexCacheFactory(indexCache, this); //Get randomizers ???
        }
    }

    public void ensureIsUpdated() {
        //super.ensureIsUpdated(this.newValue, this.name);
    }
    public boolean isModifiedNotSaved() { return !Str.areEqual(this.originalValue, this.newValue, true, true); }

    public boolean wasConsumedAsChildIndexed(IndexCache indexCache) { return isValid() && this.factory.ensureIsCached(indexCache, true); }

    public boolean updateFocusIndex(int index) {
        if(!isValid(index)) return false;
        factory.performIndexUpdate(index, this);
        ensureIsUpdated();
        return isModifiedNotSaved();
    }

    public boolean updateModified(String newModifiedText) {
        newValue = newModifiedText;
        ensureIsUpdated();
        return isModifiedNotSaved();
    }

    public void bindRandomizer(IRandomizer randomizer) {
        this.randomizer = randomizer;
    }

    public boolean bindAndUpdateElements(
            TextInputEditText inputText,
            TextWatcher watcher,
            TextView nameLabel,
            TextView friendlyNameLabel,
            TextView descriptionLabel) {
        SettingsHelperOld.setLabelText(nameLabel, name);
        SettingsHelperOld.setLabelText(friendlyNameLabel, getFriendlyName());
        SettingsHelperOld.setLabelText(descriptionLabel, getDescription());
        SettingsHelperOld.setInputTextText(inputText, watcher, newValue);
        //bindElements(inputText, watcher, nameLabel);
        return isModifiedNotSaved();
    }


    /*public void setName(String name) {

    }
    public boolean isSubIndexedSetting(SettingExtended setting) {
        if(getIndex() != 0 || setting == null) return false;
        int otherIndex = setting.getIndex();
        if(otherIndex > 0 && this.friendlyName.equalsIgnoreCase(setting.friendlyName)) {
            this.indexCache.put(otherIndex, setting.currentIndex);
            return true;
        }

        return false;
    }

    public boolean isDatabaseAndCurrentValueSame() {
        if(newValue == null) return true;
        return newValue.equalsIgnoreCase(originalValue);
    }

    public boolean updateIndex(int index) {
        currentIndex.swapData(this, index);
        return isDatabaseAndCurrentValueSame();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
        return isDatabaseAndCurrentValueSame();
    }

    public boolean setNewValue(String newValue, boolean setInputTextIfAvailable) {
        this.newValue = newValue;
        if(setInputTextIfAvailable && this.inputText != null && this.isDataInLabel(name)) super.setInputText(newValue);
        return isDatabaseAndCurrentValueSame();
    }


    public void syncOriginalWithNewValue() {
        this.originalValue = this.newValue;
        this.newValue = null;
    }

    public void bindRandomizer(IRandomizerOld randomizer) {
        //We can have a 2D randomizer where it handles it single self
        //Nothing Complex
        //
        //Then we have the Parent Randomizer, where the Randomizer Controls its children settings
    }*/

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) {
            String s = (String)obj;
            return s.equalsIgnoreCase(this.getName()) || s.equalsIgnoreCase(this.getFriendlyName()) || s.equalsIgnoreCase(this.getNameWithoutIndex());
        }

        if(obj instanceof SettingExtendedOld) {
            SettingExtendedOld s = (SettingExtendedOld)obj;
            return Str.areEqual(s.getName(), this.getName(), false, true) ||
                    Str.areEqual(s.getFriendlyName(), this.getFriendlyName(), false, true) ||
                    Str.areEqual(s.getNameWithoutIndex(), this.getNameWithoutIndex(), false, true);
        }

        return false;
    }
}
