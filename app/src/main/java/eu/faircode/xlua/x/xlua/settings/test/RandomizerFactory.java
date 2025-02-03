package eu.faircode.xlua.x.xlua.settings.test;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.interfaces.IFragmentController;
import eu.faircode.xlua.x.ui.core.util.ListFragmentUtils;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.fragments.SettingExFragment;
import eu.faircode.xlua.x.ui.fragments.SettingFragmentUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomizerFactory {
    private static final String TAG = LibUtil.generateTag(RandomizerFactory.class);

    public int count = 0;

    public final Stack<String> stack = new Stack<>();
    private final Map<String, IRandomizer> randomizers = RandomizersCache.getCopy();
    private final Map<String, RandomSettingHolder> checked = new HashMap<>();
    private final Map<String, RandomSettingHolder> settings = new HashMap<>();

    public static List<SettingHolder> getAllSettings(IFragmentController controller) { return getAllSettings(controller.getFragment()); }
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

    public RandomizerFactory randomize(
            IFragmentController controller,
            List<SettingHolder> checked,
            Context context,
            SharedRegistry sharedRegistry) {
        if(controller == null) return this;
        return randomize(getAllSettings(controller), checked, context, sharedRegistry);
    }

    public RandomizerFactory randomize(
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
                    String name = holder.name();
                    if(!this.checked.containsKey(name)) {
                        putChecked(holder);
                    }
                }
            }
        } else {
            if(sharedRegistry == null)
                return this;

            for(SettingHolder setting : settings) {
                if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getSharedId())) {
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

        for(Map.Entry<String, RandomSettingHolder> entry : this.checked.entrySet())
            handleCheckedRequirements(entry.getValue());

        if(DebugUtil.isDebug())
            Log.d(TAG, "Stage (3) Checked Count=" + this.checked.size());

        //Start with Parents
        for(Map.Entry<String, RandomSettingHolder> entry : this.checked.entrySet()) {
            RandomSettingHolder holder = entry.getValue();
            if(holder != null && holder.isParentRandomizer() && !holder.wasRandomized())
                invokeRandomize(holder);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Stage (4) Stage (3) Finished");

        //Do the Remaining
        for(Map.Entry<String, RandomSettingHolder> entry : this.checked.entrySet()) {
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
                count++;
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Updated Setting [%s] Original(1)=[%s]  Original(2)=[%s]  New Random Value=[%s]  New Count=[%s]",
                            holder.name(),
                            holder.holder.getValue(),
                            holder.holder.getNewValue(),
                            holder.getValue(false),
                            count));

                holder.updateHolder(true, true, context);
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

    public void pushValueSpecial(String settingName, String value) { pushValue(settingName, value); }
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
                holder.randomizer.randomize(null);
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
                    holder.randomizer.randomize(null);
                }
            }
        }
    }

    private void handleCheckedRequirements(RandomSettingHolder holder) {
        if(holder != null && holder.hasRandomizer()) {
            IRandomizer randomizer = holder.randomizer;
            List<String> reqList = randomizer.getRequirements(holder.name());
            if(ListUtil.isValid(reqList)) {
                for(String rq : reqList) {
                    if(!this.checked.containsKey(rq)) {
                        //Not Checked or set to checked
                        //BUT its required...
                        RandomSettingHolder reqHolder = this.settings.get(rq);
                        if(reqHolder != null) {
                            if(reqHolder.isParentRandomizer()) {
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
                                if(originalValue != null) {
                                    //Then use THAT value, it's not checked BUT it's required, so use the original value
                                    //We do not append it to the checked list
                                    //This will finalize it's value
                                    reqHolder.setValue(reqHolder.holder.getNewValue());
                                } else {
                                    //Then we want to Randomize it
                                    //So we will leave it out of the "checked" list trusting the process
                                    //Tho we never actual update its actual value, ... so we don't append it to the "checked" list
                                    //The Checked list is for settings that need to be updated / changed after randomization
                                    putChecked(reqHolder);
                                    handleCheckedRequirements(reqHolder);
                                }
                            }
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
                        }
                    }
                }
            }
        }
    }

    private void putChecked(RandomSettingHolder randomSettingHolder) {
        if(randomSettingHolder != null) {
            String name = randomSettingHolder.name();
            if(!this.checked.containsKey(name)) {
                randomSettingHolder.ensureEmpty();
                this.checked.put(name, randomSettingHolder);
            }
        }
    }

    private IRandomizer getRandomizer(String settingName, SharedRegistry sharedRegistry) {
        if(settingName == null) return null;
        IRandomizer randomizer = sharedRegistry.getSharedObject(settingName);
        return randomizer == null ? randomizers.get(settingName) : randomizer;
    }
}
