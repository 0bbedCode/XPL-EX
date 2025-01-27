package eu.faircode.xlua.x.xlua.settings.random;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

/*
    ToDO: Add Helper Function ? "updateSettings(settings_list_holders, stateRegistry)
                    RZA FURY IN MY EYES
 */
public class RandomizerSessionContext {
    private static final String TAG = "XLua.RandomizerSessionContext";

    public static RandomizerSessionContext create() { return new RandomizerSessionContext(); }

    public final String sessionId = UUID.randomUUID().toString();

    public final Map<String, String> values = new HashMap<>();
    public final Stack<String> stack = new Stack<>();

    public int count() { return values.size(); }
    public boolean isEmpty() { return values.isEmpty();  }

    /*public void randomizeBulk(List<SettingHolder> settings, SharedRegistry sharedRegistry, Context context) {
        Map<String, IRandomizer> randomizers = RandomizersCache.getCopy();
        if(DebugUtil.isDebug())
            Log.d(TAG, "Bulk Randomizing! Settings Count=" + settings.size() + " Randomizers Count=" + randomizers.size());


    }*/

    public List<String> randomized = new ArrayList<>();

    public SharedRegistry sharedRegistry = null;
    public Context context = null;

    public List<String> finishedParents = new ArrayList<>();

    public Map<String, IRandomizer> randomizers = new HashMap<>();
    public Map<String, SettingHolder> settings = new HashMap<>();

    private int savedCount = 0;
    private boolean forceUpdate = false;

    public int getSavedCount() { return savedCount; }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public void setContext(Context context) { this.context = context; }
    public void setRandomizers() {
        Map<String, IRandomizer> randomizers = RandomizersCache.getCopy();
        for(Map.Entry<String, IRandomizer> entry : randomizers.entrySet()) {
            String settingName = entry.getKey();
            String settingId = sharedSettingName(settingName);
            IRandomizer randomizer = sharedRegistry.getSharedObject(settingId);
            this.randomizers.put(settingName,randomizer == null ? entry.getValue() : randomizer);
        }
    }

    public void setSharedRegistry(SharedRegistry sharedRegistry) { this.sharedRegistry = sharedRegistry; }
    public void setSettings(List<SettingHolder> settings) {
        if(ListUtil.isValid(settings)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Settings Set Size=" + settings.size());

            for(SettingHolder setting : settings) {
                this.settings.put(setting.getName(), setting);
                if(!this.randomizers.containsKey(setting.getName())) {
                    IRandomizer randomizer = this.sharedRegistry.getSharedObject(setting.getId());
                    if(randomizer != null) {
                        this.randomizers.put(setting.getName(), randomizer);
                    }
                }
            }
        }
    }

    public List<String> resolveRequirements(List<String> requirements) {
        for(String req : requirements) {
            if(!values.containsKey(req)) {
                IRandomizer randomizer = randomizers.get(req);
                SettingHolder holder = settings.get(req);

                //Handle parents
                if(holder != null && holder.getValue() != null && !sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, sharedSettingName(req))) {
                    values.put(req, holder.getValue());
                } else {
                    if(randomizer != null) {
                        if(randomizer.hasRequirements(req))
                            resolveRequirements(randomizer.getRequirements(req));

                        stack.push(req);
                        randomizer.randomize(this);
                        //In theory we should be and start using the "pushSpecial"
                    }
                }
            }
        }

        return requirements;
    }

    public static String sharedSettingName(String settingName) {
        return Str.combine("setting:", settingName);
    }

    public void randomizeAll() {
        for(Map.Entry<String, IRandomizer> entry : randomizers.entrySet()) {
            String name = entry.getKey();
            IRandomizer randomizer = entry.getValue();
            if(!values.containsKey(name))
                ran(randomizer, name);
            else
                Log.w(TAG, "Contains [" + name + "] ... in Value List!");
        }

        for(Map.Entry<String, SettingHolder> entry : settings.entrySet()) {
            String name = entry.getKey();
            IRandomizer randomizer = randomizers.get(name);
            String parent = randomizer != null ? randomizer.hasParentControl() ? randomizer.getParentControlName() : null : null;
            boolean force = forceUpdate || parent != null && sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, sharedSettingName(parent));
            if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, sharedSettingName(name)) || force) {
                SettingHolder holder = entry.getValue();
                String value = values.get(name);
                if(value != null) {
                    holder.setNewValue(value);
                    holder.ensureUiUpdated(value);
                    holder.setNameLabelColor(context);
                    savedCount++;
                }
            }
        }
    }

    public void ran(IRandomizer randomizer, String settingName) {
        /*if(randomizer.hasRequirements(settingName)) {
            //Resolve any sub requirements if any
            List<String> requirements = randomizer.getRequirements(settingName);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Has Requirements, Setting Name=" + settingName + " Req=[" + Str.joinList(requirements) + "]");

            for(String rq : requirements) {
                if(!values.containsKey(rq)) {
                    IRandomizer rqRandomizer = randomizers.get(rq);
                    if(rqRandomizer != null)
                        ran(rqRandomizer, rq);
                }
            }
        }*/

        if(randomizer.hasParentControl()) {
            String parentName = randomizer.getParentControlName();
            if(!Str.isEmpty(parentName) && !finishedParents.contains(parentName)) {
                IRandomizer parent = randomizers.get(parentName);
                if(parent != null) {
                    stack.push(parentName);
                    parent.randomize(this);
                    finishedParents.add(parentName);
                }
            }
        } else {
            if(!randomizer.isParentControl()) {
                stack.push(settingName);
                randomizer.randomize(this);
            }else {
                if(!finishedParents.contains(settingName)) {
                    stack.push(settingName);
                    randomizer.randomize(this);
                    finishedParents.add(settingName);
                }
            }
        }
    }

    public void finalizeValue(String settingName, SharedRegistry sharedRegistry) {
        SettingHolder holder = settings.get(settingName);
        if(holder == null)
            return;

        //Does not matter if they are the "same" just takes more cpu cycles but who gives a shit as long as the algo works!
        boolean useOriginal = holder.getValue() != null && !sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, holder.getId());
        if(useOriginal)
            values.put(settingName, holder.getValue());
    }

    public void pushSpecial(String settingName, String newValue) {
        if(!values.containsKey(settingName)) {
            pushValue(settingName, newValue);
            finalizeValue(settingName, sharedRegistry);
        }
    }

    /*public void doTest(List<SettingHolder> settings, SharedRegistry viewStateRegistry, Context context) {
        Map<String, IRandomizer> randomizers = RandomizersCache.getCopy();

        for(Map.Entry<String, IRandomizer> entryRandom : randomizers.entrySet()) {
            String settingName = entryRandom.getKey();
            IRandomizer randomizer = entryRandom.getValue();
            boolean isChecked = viewStateRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, settingName);

            if(randomizer.hasRequirements(settingName)) {

            }
        }

        for(SettingHolder holder : settings) {
            String name = holder.getName();
            IRandomizer randomizer = randomizers.get(name);

            if(randomizer.hasRequirements()) {
                RandomElement element = (RandomElement) randomizer;
                Map<String, List<String>> requirements = element.links;

                for(Map.Entry<String, List<String>> entry : requirements.entrySet()) {
                    String orgSettingName =
                }
            }
        }
    }*/


    public void bulkRandomize(List<SettingHolder> settings, SharedRegistry viewStateRegistry, Context context) {
        Map<String, IRandomizer> randomizers = RandomizersCache.getCopy();
        if(DebugUtil.isDebug())
            Log.d(TAG, "Bulk Randomizing! Settings Count=" + settings.size() + " Randomizers Count=" + randomizers.size());

        //Get Enabled
        Map<SettingHolder, IRandomizer> pairs = new HashMap<>();
        for(SettingHolder setting : settings) {
            if(viewStateRegistry == null || viewStateRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getId())) {
                IRandomizer randomizer = randomizers.get(setting.getName());
                if(randomizer == null) {
                    Log.e(TAG, "Error No Randomizer! Setting Name=" + setting.getName());
                    continue;
                }

                pairs.put(setting, randomizer);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Created Randomizer Pair, Id=" + setting.getId() + " Name=" + setting.getName() + " Value=" + setting.getValue() + " New Value=" + setting.getNewValue());
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Settings Pairs with Randomizers and are Enabled, Size=" + pairs.size());

        //Find all parents first type things then init them ?
        for(Map.Entry<SettingHolder, IRandomizer> pair : pairs.entrySet()) {
            SettingHolder holder = pair.getKey();
            IRandomizer randomizer = pair.getValue();
            String name = holder.getName();
            if(!hasSetting(name)) {
                //append to the stack the setting ?
                //Invoke Randomizer
                randomizer.randomize(this);
                if(hasSetting(name)) {
                    String value = getValue(name);
                    holder.setNewValue(value);
                    //holder.ensureInputUpdated();

                    holder.ensureUiUpdated(value);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Randomized Setting, Setting=" + name + " Value=" + value);

                    if(context != null) {
                        holder.setNameLabelColor(context);
                    }
                } else {
                    Log.w(TAG, "Weird Did not set Value for Setting Name=" + name);
                }
            } else {
                Log.w(TAG, "Already has Setting for Set Value, Name=" + name);
            }
        }
    }

    public RandomizerSessionContext() { }

    public boolean hasSetting(String settingName) { return settingName != null && values.containsKey(settingName); }


    public boolean keepGoing(List<String> settingNames) {

        return true;//Add
    }

    public void pushValue(String settingName, String value) { pushValue(settingName, value, false); }
    public void pushValue(String settingName, String value, boolean overwriteIfExists) {
        if(!TextUtils.isEmpty(settingName)) {
            synchronized (values) {
                if(overwriteIfExists || !values.containsKey(settingName)) {
                    values.put(settingName, value);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Pushing Setting Value [%s], Name=%s  Value=%s", sessionId, settingName, value));
                }
            }
        }
    }

    public String getValue(String settingName) { return getValue(settingName, null, false); }
    public String getValue(String settingName, String defaultIfNonExistent) { return getValue(settingName, defaultIfNonExistent, true); }
    public String getValue(String settingName, String defaultIfNonExistent, boolean pushIfNonExists) {
        if(TextUtils.isEmpty(settingName))
            return defaultIfNonExistent;

        synchronized (values) {
            if(!values.containsKey(settingName)) {
                if(pushIfNonExists) {
                    values.put(settingName, defaultIfNonExistent);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Pushing Setting Value from Get [%s], Name=%s  Value=%s", sessionId, settingName, defaultIfNonExistent));
                }

                return defaultIfNonExistent;
            } else {
                return values.get(settingName);
            }
        }
    }

    public long getValueLong(String settingName) { return getValueLong(settingName, 0, false); }
    public long getValueLong(String settingName, long defaultIfNonExistent) { return getValueLong(settingName, defaultIfNonExistent, true); }
    public long getValueLong(String settingName, long defaultIfNonExistent, boolean pushIfNonExists) {
        if(TextUtils.isEmpty(settingName))
            return defaultIfNonExistent;

        synchronized (values) {
            if(!values.containsKey(settingName)) {
                if(pushIfNonExists) {
                    values.put(settingName, String.valueOf(defaultIfNonExistent));
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Pushing Setting Value from Get (long) [%s], Name=%s  Value=%s", sessionId, settingName, defaultIfNonExistent));
                }

                return defaultIfNonExistent;
            } else {
                try {
                    String v = Str.trimStart(values.get(settingName), "0", "x", "X");
                    if(TextUtils.isEmpty(v) || !Str.isNumeric(v) || v.length() > 19) {
                        if(pushIfNonExists) {
                            values.put(settingName, String.valueOf(defaultIfNonExistent));
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Pushing Setting Value from Get (long)(2) [%s], Name=%s  Value=%s", sessionId, settingName, defaultIfNonExistent));
                        }

                        return defaultIfNonExistent;
                    }

                    return Long.parseLong(v);
                }catch (Exception ignored) {
                    return defaultIfNonExistent;
                }
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("ID", this.sessionId)
                .appendFieldLine("Settings Set Count", this.values.size())
                .toString(true);
    }
}
