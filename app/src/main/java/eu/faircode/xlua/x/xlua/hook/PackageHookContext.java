package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.properties.MockPropConversions;
import eu.faircode.xlua.api.xlua.XLuaQuery;
import eu.faircode.xlua.api.xmock.XMockQuery;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetSettingsExCommand;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.NameInformation;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class PackageHookContext {
    private static final String TAG = LibUtil.generateTag(PackageHookContext.class);

    public static PackageHookContext create(final XC_LoadPackage.LoadPackageParam lpparam, int uid, final Context context) { return new PackageHookContext(lpparam.packageName, uid, context); }

    public static final String RANDOM_VALUE = "%random%";
    public static final String RANDOMIZE_VALUE = "%randomize%";

    public static final List<String> RANDOM_INDICATORS = Arrays.asList(RANDOM_VALUE, RANDOMIZE_VALUE);

    public final String packageName;
    public final int uid;
    public final String temporaryKey;
    public final boolean useDefault;

    public final HashMap<String, String> settings = new HashMap<>();
    public final HashMap<String, Integer> buildPropSettings = new HashMap<>();
    public final HashMap<String, String> buildPropMaps = new HashMap<>();

    public PackageHookContext(String packageName, int uid, final Context context) {
        this.packageName = packageName;
        this.uid = uid;
        this.temporaryKey = UUID.randomUUID().toString();
        this.useDefault = GetSettingExCommand.getBool(context, GetSettingExCommand.SETTING_USE_DEFAULT, uid, packageName);

        settings.putAll(GetSettingsExCommand.getAsMap(
                context,
                true,
                UserIdentity.DEFAULT_USER,
                UserIdentity.GLOBAL_NAMESPACE,
                GetSettingsExCommand.FLAG_ONE));

        settings.putAll(GetSettingsExCommand.getAsMap(
                context,
                true,
                uid,
                packageName,
                GetSettingsExCommand.FLAG_ONE));

        initRandomizers(context);
        //final Map<String, Integer> propSettings = MockPropConversions.toMap(XMockQuery.getModifiedProperties(context, uid, pName));
        //final Map<String, String> propMaps = XMockQuery.getMockPropMapsMap(context, true, settings, false);
    }

    private void initRandomizers(Context context) {
        if(ListUtil.isValid(settings)) {
            try {
                List<SettingHolder> holders = new ArrayList<>(settings.size());
                List<SettingHolder> needToRandomize = new ArrayList<>();
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Starting Pre Randomization on [%s] Settings, Package Name [%s] UID [%s] Use Default [%s]", this.settings.size(), packageName, uid, useDefault));

                Map<String, IRandomizer> randomizers = RandomizersCache.getCopy();
                SharedRegistry sharedRegistry = new SharedRegistry();
                for(Map.Entry<String, String> entry : settings.entrySet()) {
                    String name = entry.getKey();
                    String value = entry.getValue();

                    SettingHolder holder = new SettingHolder(NameInformation.createRaw(name), value, null);
                    holders.add(holder);

                    if(!Str.isEmpty(name) && !Str.isEmpty(value)) {
                        if(!randomizers.containsKey(name)) {
                            Log.w(TAG, Str.fm("Setting [%s] Does not have a Randomizer! Failed to Pre Randomize... ", name));
                            continue;
                        }

                        if(value.contains("%") && value.length() < 15) {
                            String low = value.toLowerCase();
                            if(RANDOM_INDICATORS.contains(low)) {
                                sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, RandomizerSessionContext.sharedSettingName(name), true);
                                needToRandomize.add(holder);
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, Str.fm("Pushed Setting [%s] with Value [%s] to the list of to Randomize! To Randomize Count=[%s] Holder=[%s]", name, value, needToRandomize.size(), Str.toStringOrNull(Str.noNL(holder))));
                            }
                        }
                    } else {
                        if(DebugUtil.isDebug())
                            Log.w(TAG, Str.fm("Error Null or Empty ? Skipping Setting:" + name + " Value=" + value));
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Finished Parsing Settings for Pre Randomization, Settings Count=[%s] To Randomize Count=[%s] Now Randomizing the need!", holders.size(), needToRandomize.size()));

                //RandomizerSessionContext ctx = RandomizerSessionContext.create();
                //ctx.setContext(context);
                //ctx.setSharedRegistry(sharedRegistry);
                //ctx.setSettings(holders);
                //ctx.setRandomizers();
                //ctx.randomizeAll();

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Finished Pre Randomizing [%s] Settings, Total Settings [%s] Now pushing!", needToRandomize.size(), holders.size()));

                for(SettingHolder holder : needToRandomize) {
                    String name = holder.getName();
                    boolean hasSetting = this.settings.containsKey(name);
                    String oldVal = this.settings.get(name);
                    String newVal = holder.getNewValue();
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Updating Setting [%s] with new Value [%s] from old Value [%s], exists=[%s]", name, newVal, oldVal, hasSetting));

                    this.settings.put(name, newVal);
                }
            }catch (Exception e) {
                Log.e(TAG, Str.fm("Failed to Pre Randomize Settings! Settings Count [%s] Package Name [%s] UID [%s] Use Default [%s] Error=", this.settings.size(), packageName, uid, useDefault, e));
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("PackageName", this.packageName)
                .appendFieldLine("UID", this.uid)
                .appendFieldLine("Temp Key", this.temporaryKey)
                .appendFieldLine("Use Default", this.useDefault)
                .appendFieldLine("Settings Count", this.settings.size())
                .appendFieldLine("Build Prop Settings Count", this.buildPropSettings.size())
                .appendFieldLine("Build Prop Settings Map Count", this.buildPropMaps.size())
                .toString(true);
    }
}
