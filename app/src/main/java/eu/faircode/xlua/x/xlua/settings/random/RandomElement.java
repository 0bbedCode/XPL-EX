package eu.faircode.xlua.x.xlua.settings.random;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;

/*
    ToDO: SF SONG METRO BOOM, Add Helper populate function perhaps ? "populateSpinner(spinner)"
            Hmm maybe we do want to request Setting Values ? it will help us with setting value EXAMPLE
                If they SET the MANUFACTURER for Device Already, they just want to Randomize model, it will Grab MANUFACTURER
 */
//Hmmmmm we can make some static link list shit ???
//Iterate it to init the Context
public abstract class RandomElement implements IRandomizer {
    private static final String TAG = LibUtil.generateTag(RandomElement.class);

    private final List<IRandomizer> options = new ArrayList<>();

    private final List<String> settings = new ArrayList<>();
    private final List<String> parents = new ArrayList<>();

    public final Map<String, List<String>> links = new HashMap<>();

    public void putRequirement(String requirement) {
        if(!Str.isEmpty(requirement)) {
            String first = settings.get(0);
            List<String> req = new ArrayList<>();
            req.add(requirement);
            links.put(first, req);
        }
    }

    public void putRequirementsAsIndex(String... requirements) {
        if(ArrayUtils.isValid(requirements)) {
            int ix = 1;
            for(String setting : settings) {
                String[] split = setting.split("\\.");
                int val = Str.tryParseInt(split[split.length - 1], ix);

                List<String> req = new ArrayList<>();
                for(String rq : requirements) {
                    String rName = Str.combineEx(rq, Str.PERIOD, String.valueOf(val));
                    req.add(rName);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Pushing Req: " + rName);
                }

                links.put(setting, req);
                ix++;
            }
        }
    }

    @Override
    public boolean isOption() {
        return this.getClass().getSimpleName().toLowerCase().contains("option");
    }

    @Override
    public boolean isParentControl() {
        return false;
    }

    @Override
    public String getParentControlName() {
        return parents.get(0);
    }

    @Override
    public boolean hasParentControl() {
        return !parents.isEmpty();
    }

    @Override
    public List<String> getRequirements(String settingName) { return links.get(settingName); }

    @Override
    public boolean hasRequirements(String settingName) { return links.containsKey(settingName); }

    protected String displayName;

    public List<String> getSettings() { return settings; }

    @Override
    public List<IRandomizer> getOptions() { return options; }


    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof RandomElement))
            return false;

        RandomElement other = (RandomElement) obj;
        return other.displayName.equalsIgnoreCase(this.displayName) && settings.size() == other.settings.size() && parents.size() == other.parents.size();
    }

    @Override
    public String getRawValue() { return null; }

    public RandomElement() { }
    public RandomElement(String displayName) { this.displayName = displayName; }

    public String getFirstSettingName() { return settings.isEmpty() ? null : settings.get(0); }

    /*
        ToDO: Check this why is it default false ?
     */
    public IRandomizer randomOption() { return randomOption(false); }
    public IRandomizer randomOption(boolean skipFirst) {
        if(options.isEmpty())
            return null;

        if(options.size() == 1)
            return options.get(0);

        int itemIndex = RandomGenerator.nextInt(skipFirst ? 1 : 0, options.size());
        return options.get(itemIndex);
    }

    public void putParents(String... parents) {
        if(ArrayUtils.isValid(parents)) {
            for(String parent : parents) {
                String trimmed = Str.trimEx(parent, " ", ".");
                if(TextUtils.isEmpty(trimmed) || this.parents.contains(trimmed))
                    continue;

                this.parents.add(trimmed);
            }
        }
    }

    public void putOption(IRandomizer option) {
        if(!this.options.contains(option))
            this.options.add(option);
    }

    public void putOptions(IRandomizer... options) {
        if(ArrayUtils.isValid(options)) {
            for(IRandomizer option : options) {
                if(!this.options.contains(option))
                    this.options.add(option);
            }
        }
    }

    public void putIndexSettings(String settingName, int... indexes) {
        if(!Str.isEmpty(settingName)) {
            String trimmed = Str.trimEx(settingName, " ", ".");
            for(int index : indexes) {
                String indexName = Str.combineEx(trimmed, Str.PERIOD, index);
                if(this.settings.contains(indexName))
                   continue;

                this.settings.add(indexName);
            }
        }
    }

    public void putSettings(String... settings) {
        if(ArrayUtils.isValid(settings)) {
            for(String setting : settings) {
                String trimmed = Str.trimEx(setting, " ", ".");
                if(TextUtils.isEmpty(trimmed) || this.settings.contains(trimmed))
                    continue;

                this.settings.add(trimmed);
            }
        }
    }

    @Override
    public boolean hasOptions() { return !options.isEmpty(); }

    @Override
    public boolean hasSetting(String settingName) {
        for(String setting : settings)
            if(setting.equalsIgnoreCase(settingName))
                return true;

        return false;
    }

    @Override
    public String getDisplayName() { return displayName; }

    /*@NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Display Name", displayName)
                .appendFieldLine("Options Size", options.size())
                .appendFieldLine("Settings Size", settings.size())
                .appendFieldLine("Settings", Str.joinList(settings))
                .toString();
    }*/

    @NonNull
    @Override
    public String toString() { return displayName; }
}
