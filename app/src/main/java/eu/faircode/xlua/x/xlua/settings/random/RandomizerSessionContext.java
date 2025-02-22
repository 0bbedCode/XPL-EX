package eu.faircode.xlua.x.xlua.settings.random;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.fragments.SettingExFragment;
import eu.faircode.xlua.x.ui.fragments.SettingFragmentUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.test.RandomSettingHolder;

/*
    ToDO: Add Helper Function ? "updateSettings(settings_list_holders, stateRegistry)
                    RZA FURY IN MY EYES
 */
public class RandomizerSessionContext {
    private static final String TAG = LibUtil.generateTag(RandomizerSessionContext.class);

    public static RandomizerSessionContext create() { return  new RandomizerSessionContext(); }

    public static String sharedSettingName(String settingName) { return settingName != null && settingName.startsWith("setting:") ? settingName : Str.combine("setting:", settingName); }

    public final Stack<String> stack = new Stack<>();

    private final List<RandomSettingHolder> updated = new ArrayList<>();
    private final Map<String, IRandomizer> randomizers = RandomizersCache.getCopy();
    private final Map<String, RandomSettingHolder> checked = new HashMap<>();
    private final Map<String, RandomSettingHolder> settings = new HashMap<>();

    public int getRandomizedCount() { return updated.size(); }

    //public static List<SettingHolder> getAllSettings(IFragmentController controller) { return getAllSettings(controller.getFragment()); }
    public static List<SettingHolder> getAllSettings(Fragment fragment) {
        try {
            if(fragment instanceof SettingExFragment) {
                SettingExFragment frag = (SettingExFragment) fragment;
                List<SettingHolder> all = SettingFragmentUtils.getSettings(frag.getLiveData());
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Static Settings Count=" + ListUtil.size(all));

                return all;
            } else {
                return new ArrayList<>();
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed to Get all Settings, Error=" + e);
            return new ArrayList<>();
        }
    }

    public RandomizerSessionContext updateToOption(
            Fragment fragment,
            List<SettingHolder> checked,
            IRandomizer randomizer,
            Context context,
            SharedRegistry sharedRegistry) {
        if(fragment == null)
            return this;

        return updateToOption(getAllSettings(fragment), checked, randomizer, context, sharedRegistry);
    }

    public RandomizerSessionContext updateToOption(
            List<SettingHolder> settings,
            List<SettingHolder> checked,
            IRandomizer randomizer,
            Context context,
            SharedRegistry sharedRegistry) {

        for(SettingHolder setting : settings) {
            RandomSettingHolder randomHolder = new RandomSettingHolder();
            randomHolder.holder = setting;
            randomHolder.randomizer = getRandomizer(setting.getName(), sharedRegistry);
            this.settings.put(randomHolder.name(), randomHolder);
        }

        if(randomizer != null) {
            if(randomizer.isOption()) {
                for(SettingHolder holder : checked) {
                    stack.push(holder.getName());
                    randomizer.randomize(this);
                    RandomSettingHolder randomHolder = this.settings.get(holder.getName());
                    if(randomHolder != null) {
                        randomHolder.updateHolder(true, true, context, sharedRegistry);
                    }
                }
            }
        }

        return this;
    }

    public RandomizerSessionContext randomize(
            Fragment fragment,
            List<SettingHolder> checked,
            Context context,
            SharedRegistry sharedRegistry) {
        if(fragment == null) return this;
        return randomize(getAllSettings(fragment), checked, context, sharedRegistry);
    }

    public RandomizerSessionContext randomize(
            List<SettingHolder> settings,
            List<SettingHolder> checked,
            Context context,
            SharedRegistry sharedRegistry) {

        if(DebugUtil.isDebug())
            Log.d(TAG, "Stage (0) Settings Count=" + ListUtil.size(settings) + " Checked Count=" + ListUtil.size(checked));

        if(!ListUtil.isValid(settings))
            return this;

        for(SettingHolder setting : settings) {
            RandomSettingHolder randomHolder = new RandomSettingHolder();
            randomHolder.holder = setting;
            randomHolder.randomizer = getRandomizer(setting.getName(), sharedRegistry);
            this.settings.put(randomHolder.name(), randomHolder);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Stage (1) Settings Count=" + this.settings.size() + " Randomizer Count=" + this.randomizers.size());

        if(ListUtil.isValid(checked)) {
            //These are the high level checks, this is where it starts
            for(SettingHolder setting : checked) {
                RandomSettingHolder holder = this.settings.get(setting.getName());
                if(holder != null && holder.hasRandomizer()) {
                    if(!this.checked.containsKey(holder.name()))
                        putChecked(holder);
                }
            }
        } else {
            if(sharedRegistry == null)
                return this;

            for(SettingHolder setting : settings) {
                if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getObjectId())) {
                    RandomSettingHolder holder = this.settings.get(setting.getName());
                    if(holder != null && holder.hasRandomizer()) {
                        String name = holder.name();
                        if(!this.checked.containsKey(name)) {
                            putChecked(holder);
                        }
                    }
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Stage (2) Checked Count=" + this.checked.size());

        for(Map.Entry<String, RandomSettingHolder> entry : new HashMap<>(this.checked).entrySet())
            handleCheckedRequirements(entry.getValue());

        if(DebugUtil.isDebug())
            Log.d(TAG, "Stage (3) Checked Count=" + this.checked.size());

        //Start with Parents
        for(Map.Entry<String, RandomSettingHolder> entry : new HashMap<>(this.checked).entrySet()) {
            RandomSettingHolder holder = entry.getValue();
            if(holder != null && holder.isParentRandomizer() && !holder.wasRandomized())
                invokeRandomize(holder);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Stage (4) Stage (3) Finished");

        //Do the Remaining
        for(Map.Entry<String, RandomSettingHolder> entry : new HashMap<>(this.checked).entrySet()) {
            RandomSettingHolder holder = entry.getValue();
            if(holder != null && !holder.isParentRandomizer() && !holder.wasRandomized())
                invokeRandomize(holder);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Stage (5) Stage (4) Finished");

        //Update each element
        for(Map.Entry<String, RandomSettingHolder> entry : this.checked.entrySet()) {
            RandomSettingHolder holder = entry.getValue();
            if(holder != null && holder.wasRandomized()) {
                updated.add(holder);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Updated Setting [%s] Original(1)=[%s]  Original(2)=[%s]  New Random Value=[%s]  New Count=[%s]",
                            holder.name(),
                            Str.toStringOrNull(holder.holder.getValue()),
                            Str.toStringOrNull(holder.holder.getNewValue()),
                            Str.toStringOrNull(holder.getValue(false)),
                            String.valueOf(updated.size())));

                holder.updateHolder(true, true, context, sharedRegistry);
            }
        }

        return this;
    }

    public List<String> resolveRequirements(List<String> requirements) {
        /*for(String req : requirements) {
            RandomSettingHolder holder = this.settings.get(req);
            if(holder != null)
                internalRandomize(holder);
        }*/

        return requirements;
    }

    public boolean wasRandomized(String name) {
        RandomSettingHolder holder = this.settings.get(name);
        return holder != null && holder.wasRandomized();
    }

    public void pushSpecial(String settingName, String value) { pushValue(settingName, value); }
    public void pushValue(String settingName, String value) {
        RandomSettingHolder holder = this.settings.get(settingName);
        if(holder != null)
            holder.setValue(value);
    }

    public String getValue(String settingName) {
        RandomSettingHolder holder = this.settings.get(settingName);
        return holder == null ? null : holder.getValue(true);
    }

    private void invokeRandomize(RandomSettingHolder holder) {
        if(holder != null && holder.hasRandomizer()) {
            if(holder.isParentRandomizer()) {
                this.stack.push(holder.name());
                holder.randomizer.randomize(this);
                //Be aware of cases where Parent Randomizer requires
                //In that case we can seperate requirements from what it sets ....
                //We can do that later
            } else {
                if(holder.randomizer.hasRequirements(holder.name())) {
                    List<String> reqList = holder.randomizer.getRequirements(holder.name());
                    if(ListUtil.isValid(reqList)) {
                        for(String rq : reqList) {
                            RandomSettingHolder reqHolder = this.settings.get(rq);
                            if(this.checked.containsKey(rq))
                                invokeRandomize(reqHolder);
                        }
                    }
                }

                if(this.checked.containsKey(holder.name()) && !holder.wasRandomized()) {
                    stack.push(holder.name());
                    holder.randomizer.randomize(this);
                }
            }
        }
    }

    private void handleCheckedRequirements(RandomSettingHolder holder) {
        if(holder != null && holder.hasRandomizer()) {
            if(DebugUtil.isDebug())
                Log.d(TAG,  Str.fm("Handling Randomizer [%s] Requirements, Is Parent [%s] Requirement Count [%s] Is Checked=%s",
                        holder.name(),
                        holder.isParentRandomizer(),
                        ListUtil.size(holder.randomizer.getRequirements(holder.name())),
                        this.checked.containsKey(holder.name())));

            IRandomizer randomizer = holder.randomizer;
            List<String> reqList = randomizer.getRequirements(holder.name());
            if(ListUtil.isValid(reqList)) {
                for(String rq : reqList) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Resolving [%s] Randomizer Requirement [%s] Is Checked=%s",
                                holder.name(),
                                rq,
                                this.checked.containsKey(rq)));

                    if(!this.checked.containsKey(rq)) {
                        //Not Checked or set to checked
                        //BUT its required...
                        RandomSettingHolder reqHolder = this.settings.get(rq);
                        if(reqHolder != null) {
                            if(reqHolder.isParentRandomizer()) {
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, Str.fm("Holder [%s] Requirement [%s] is being pushed as a Parent",
                                            holder.name(),
                                            rq));

                                //If its a parent that one of the checked settings require
                                //Then push it to checked list, also resolve it's requirements
                                //Since its a parent all of its requirements are a force check!
                                putChecked(reqHolder);
                                handleParentChecked(reqHolder);
                            }
                            else {
                                //The randomizer is NOT a parent
                                //BUT is required by a Randomizer, so we either (A) want its Original Value OR Randomize it
                                String originalValue = Str.getNonNullOrEmptyString(reqHolder.holder.getNewValue(), reqHolder.holder.getValue());
                                //boolean force = holder.isParentRandomizer() && this.checked.containsKey(holder.name());
                                boolean force = holder.isParentRandomizer();
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, Str.fm("Holder [%s] Requirement [%s] Determining if to be pushed. Force flag=%s Holder is Parent=%s Holder Is Checked=%s Value=%s",
                                            holder.name(),
                                            rq,
                                            force,
                                            holder.isParentRandomizer(),
                                            this.checked.containsKey(holder.name()),
                                            originalValue));

                                //If the value is null
                                //Hmm... its being set
                                if(force) {
                                    //Then we want to Randomize it
                                    //So we will leave it out of the "checked" list trusting the process
                                    //Tho we never actual update its actual value, ... so we don't append it to the "checked" list
                                    //The Checked list is for settings that need to be updated / changed after randomization
                                    reqHolder.setBlockUpdate(false);
                                    putChecked(reqHolder);
                                    handleCheckedRequirements(reqHolder);
                                }
                                else {
                                    if(!Str.isEmpty(originalValue)) {
                                        //Then use THAT value, it's not checked BUT it's required, so use the original value
                                        //We do not append it to the checked list
                                        //This will finalize it's value
                                        reqHolder.setValue(originalValue);
                                    } else {
                                        //We can double check shared registry is it's checked via UI ?
                                        //For now we can assume if it was not on the list, there for invoking this
                                        //It is not checked and should not be visually updated
                                        reqHolder.setBlockUpdate(true);
                                        putChecked(reqHolder);
                                    }
                                }
                            }
                        } else {
                            Log.w(TAG, Str.fm("Requirement [%s] Does not Have a Holder! From [%s]", rq, holder.name()));
                        }
                    }
                }
            }
        }
    }

    private void handleParentChecked(RandomSettingHolder holder) {
        //Future make this so it handles requirements but not as that as Child Settings or something
        //Settings it will force set, as it can be possible it requires a setting but not to force the value
        if(holder != null && holder.hasRandomizer() && holder.isParentRandomizer()) {
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Handling Parent [%s] requirements! Count=%s", holder.name(), ListUtil.size(holder.randomizer.getRequirements(holder.name()))));

            //In no Context a Parent Randomizer should Require a Parent Randomizer
            //Parent Randomizers Control a Set of Child Settings
            //Child Settings MAY have Requirements but those Requirements should not Require a Parent Randomizer
            //Makes no sense too, just weird, these Randomizers should be (1D)
            IRandomizer randomizer = holder.randomizer;
            List<String> reqList = randomizer.getRequirements(holder.name());
            if(ListUtil.isValid(reqList)) {
                for(String rq : reqList) {
                    if(!this.checked.containsKey(rq)) {
                        RandomSettingHolder reqHolder = this.settings.get(rq);
                        if(reqHolder != null) {
                            putChecked(reqHolder);
                            if(reqHolder.isParentRandomizer())
                                handleParentChecked(reqHolder);
                            else {
                                handleCheckedRequirements(reqHolder); //do we ?
                            }
                        }else {
                            if(DebugUtil.isDebug())
                                Log.w(TAG, Str.fm("Setting [%s] requirement [%s] has a NULL holder!", holder.name(), rq));
                        }
                    } else {
                        if(DebugUtil.isDebug())
                            Log.w(TAG, Str.fm("Setting [%s] requirement [%s] is already in the Checked List!", holder.name(), rq));
                    }
                }
            }
        }
        else {
            Log.w(TAG, "Error with Parent Randomizer, Null or Lack Randomizer or Not Parent! Randomizer=" + Str.toStringOrNull(holder));
        }
    }

    private void putChecked(RandomSettingHolder randomSettingHolder) {
        if(randomSettingHolder != null) {
            String name = randomSettingHolder.name();
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Putting Checked [%s] Is Already in List=%s",
                        name,
                        this.checked.containsKey(name)));

            if(!this.checked.containsKey(name)) {
                randomSettingHolder.ensureEmpty();
                this.checked.put(name, randomSettingHolder);
            }
        }
    }

    private IRandomizer getRandomizer(String settingName, SharedRegistry sharedRegistry) {
        IRandomizer randomizer = getRandomizerInternal(settingName, sharedRegistry);
        //if(DebugUtil.isDebug())
        //    Log.d(TAG, Str.fm("Got Randomizer [%s] For [%s]", Str.toStringOrNull(randomizer), settingName));
        return randomizer;
    }


    private IRandomizer getRandomizerInternal(String settingName, SharedRegistry sharedRegistry) {
        if(settingName == null) return null;
        IRandomizer randomizer = sharedRegistry.getSharedObject(settingName);
        return randomizer == null ? randomizers.get(settingName) : randomizer;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Settings Count", this.settings.size())
                .appendFieldLine("Checked Count", this.checked.size())
                .appendFieldLine("Randomizers Count", this.randomizers.size())
                .appendFieldLine("Count", this.updated.size())
                .toString(true);
    }
}
