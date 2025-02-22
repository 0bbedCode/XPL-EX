package eu.faircode.xlua.x.hook.filter;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.FilterHooksHolder;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class FilterContainerElement implements IFilterContainer {
    private static final String TAG = LibUtil.generateTag(FilterContainerElement.class);

    protected TypeMap definitions;
    protected String groupName;
    protected FilterHooksHolder factory = FilterHooksHolder.create();
    protected HashMap<String, String> createdSettings = new HashMap<>();
    protected List<String> dependencies = new ArrayList<>();

    public FilterContainerElement() { }
    public FilterContainerElement(String groupName, TypeMap definitions) { this.groupName = groupName; this.definitions = definitions; }

    @Override
    public String getGroupName() { return groupName; }

    public void putSettingPair(String name, String value) {
        if(Str.isEmpty(name)) {
            Log.e(TAG, Str.fm("Setting for Group [%s] Passed was NULL or Empty! Stack=[%s]", groupName, RuntimeUtils.getStackTraceSafeString(new Throwable())));
            return;
        }

        if(value == null) {
            Log.w(TAG, Str.fm("Setting [%s] has a NULL Value! Replacing with a Empty String! Group [%s]", name, groupName));
            value = Str.EMPTY;
        }

        if(createdSettings.containsKey(name)) {
            String old = createdSettings.get(name);
            Log.e(TAG, Str.fm("Setting [%s] is already in the List of Created Settings! Replacing old! Group=[%s] New Value=[%s] Old Value=[%s]", name, groupName, value, old));
        }

        createdSettings.put(name, value);
    }

    @Override
    public boolean hasSwallowedAsDefinition(XLuaHook hook) {
        if(isGroup(hook)) {
            if(definitions.hasDefinition(hook.getClassName(), hook.getMethodName())) {
                factory.addBase(hook);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Found a Hook Definition [%s] For this Group [%s] added to List of Definitions, Count=[%s]", hook.getObjectId(), groupName, factory.baseCount()));

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        if(isClassGroup(hook)) {
            factory.addRule(hook);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Found a Rule [%s] for this Hook Group [%s], added Rules Count=[%s]", hook.getObjectId(), groupName, factory.ruleCount()));

            String[] params = hook.getParameterTypes();
            if(ArrayUtils.isValid(params)) {
                //We reserve the settings[] field so the settings UI is not confused
                //if the param has a '=' then we can assume they are trying to set a setting to a value
                //Instead of Invoking a Script or something and waiting we can pre init settings via Param Types of a Hook Definition
                for(String p : params) {
                    if(p.contains("=")) {
                        String[] parts = p.split("=");
                        if(p.length() == 2) {
                            String settingName = parts[0].trim();
                            String settingValue = parts[1];
                            if(!Str.isEmpty(settingValue))
                                createdSettings.put(settingName, settingValue);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }


    @Override
    public List<String> getDependencies() { return ListUtil.copyToArrayList(dependencies); }

    @Override
    public boolean hasSettings() { return !createdSettings.isEmpty(); }

    @Override
    public int appendSettings(Map<String, String> settings) {
        int count = 0;
        if(settings != null) {
            if(ListUtil.isValid(this.createdSettings)) {
                for(Map.Entry<String, String> entry : new HashMap<>(this.createdSettings).entrySet()) {
                    String name = entry.getKey();
                    String value = entry.getValue();
                    if(!Str.isEmpty(name) && !Str.isEmpty(value)) {
                        settings.put(name, value);
                        count++;
                    }
                }
            }
        }

        return count;
    }

    @Override
    public void initializeDefinitions(List<XLuaHook> hooks, Map<String, String> settings) {
        if(hooks != null)
            if((factory.hasRules() && factory.hasBases()))
                hooks.addAll(factory.getBaseHooks());

        if(settings != null)
            if(!this.createdSettings.isEmpty())
                settings.putAll(this.createdSettings);
    }

    @Override
    public List<XLuaHook> getRules() { return factory.getRuleHooks();  }

    @Override
    public List<XLuaHook> getFilterBases() { return factory.getBaseHooks(); }

    public boolean isClassGroup(XLuaHook hook) { return isClassGroup(hook, false, false); }
    public boolean isClassGroup(XLuaHook hook, boolean checkHasParams) { return isClassGroup(hook, checkHasParams, false); }
    public boolean isClassGroup(XLuaHook hook, boolean checkHasParams, boolean checkHasSettings) {
        if(!isDefinitionValid(hook, checkHasParams, checkHasSettings))
            return false;

        return hook.getClassName().equalsIgnoreCase(groupName);
    }

    public boolean isGroup(XLuaHook hook) { return isGroup(hook, false, false); }
    public boolean isGroup(XLuaHook hook, boolean checkHasParams) { return isGroup(hook, checkHasParams, false); }
    public boolean isGroup(XLuaHook hook, boolean checkHasParams, boolean checkHasSettings) {
        if(!isDefinitionValid(hook, checkHasParams, checkHasSettings))
            return false;

        return hook.getGroup().equalsIgnoreCase(groupName);
    }

    public boolean isDefinitionValid(XLuaHook hook) { return isDefinitionValid(hook, false, false); }
    public boolean isDefinitionValid(XLuaHook hook, boolean checkHasParams) { return isDefinitionValid(hook, checkHasParams, false); }
    public boolean isDefinitionValid(XLuaHook hook, boolean checkHasParams, boolean checkHasSettings) {
        if(hook == null) {
            Log.e(TAG, "Critical Error, some how the Hook Definition passed is Null ? Check your fucking Code! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
            return false;
        }

        if(Str.isEmpty(hook.getClassName()) || Str.isEmpty(hook.getMethodName()) || Str.isEmpty(hook.getGroup())) {
            Log.e(TAG, "Error, Hook Definition is Invalid, is Missing Class Name, Method Name or Group Name! Hook=" + JSONUtil.toJsonString(hook));
            return false;
        }

        return !(checkHasParams && !hasParams(hook) || checkHasSettings && !hasSettings(hook));
    }

    public boolean hasParams(XLuaHook hook) {
        if(hook == null) {
            Log.e(TAG, "(hasParams) Error Hook Input is Null! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
            return false;
        }

        if(!ArrayUtils.isValid(hook.getParameterTypes())) {
            Log.e(TAG, "Error, Hook Definition is Invalid, is Missing Parameter Types! Hook=" + JSONUtil.toJsonString(hook));
            return false;
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasSettings(XLuaHook hook) {
        if(hook == null) {
            Log.e(TAG, "(hasSettings) Error Hook Input is Null! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
            return false;
        }

        if(!ArrayUtils.isValid(hook.getSettings())) {
            Log.e(TAG, "Error, Hook Definition is Invalid, is Missing Settings! Hook=" + JSONUtil.toJsonString(hook));
            return false;
        }

        return true;
    }

    public List<String> parseArgsAsAuthorities(XLuaHook hook) {
        List<String> auths = new ArrayList<>();
        if(!hasParams(hook)) {
            Log.w(TAG, "Hook is Some how missing param types or is Null, using [*] (wild card) for Authorities! Hook=" + JSONUtil.toJsonString(hook));
            auths.add("*");
            return auths;
        }

        String[] params = hook.getParameterTypes();
        for(String auth : params) {
            if(!Str.isEmpty(auth)) {
                String trimmed = Str.trimEx(auth, true, true);
                if(!Str.isEmpty(trimmed))
                    auths.add(trimmed);
            }
        }

        if(auths.isEmpty()) {
            Log.w(TAG, Str.fm("No Valid Param Types for Hook [%s] under Group [%s], using [*] (wild card) for Authorities!", hook.getObjectId(), groupName));
            auths.add("*");
        }

        return auths;
    }

    @SuppressWarnings("IndexOfReplaceableByContains")
    public List<String> parseMethodAsFilter(XLuaHook hook, boolean trimWhiteSpace) {
        //Also Support parse by new line!
        List<String> filter = new ArrayList<>();
        if(hook == null || Str.isEmpty(hook.getMethodName())) {
            Log.w(TAG, "Hook is Some how missing method name or is Null, using [*] (wild card), Hook=" + JSONUtil.toJsonString(hook));
            filter.add("*");
            return filter;
        }

        String method = hook.getMethodName();
        if(method.indexOf("|") > -1) {
            String[] parts = hook.getMethodName().split("\\|");
            for(String p : parts) {
                if(!Str.isEmpty(p)) {
                    if(trimWhiteSpace) {
                        String trimmed = Str.trimEx(p, true, true);
                        if(!Str.isEmpty(trimmed))
                            filter.add(trimmed);
                    } else {
                        filter.add(p);
                    }
                }
            }
        } else {
            filter.add(method);
        }

        if(filter.isEmpty()) {
            Log.w(TAG, Str.fm("No Valid Filters for Hook [%s] under Group [%s], using [*] (wild card) for Filters!", hook.getObjectId(), groupName));
            filter.add("*");
        }

        return filter;
    }

    // "query:[" + auth + "]:" + pair.name;

    public static String createQuerySetting(String auth, String name) { return Str.isEmpty(auth) || Str.isEmpty(name) ? null : Str.combineEx("query:[", auth, "]:", name);  }
    public static String createAuthoritySetting(String rule) { return !Str.isEmpty(rule) ? Str.combine("query:", rule) : rule; }
    public static String createCallSetting(String rule) { return !Str.isEmpty(rule) ? Str.combine("call:", rule) : rule; }
    public static String createPropertySetting(String property) { return !Str.isEmpty(property) ? Str.combine("prop:", property) : property; }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendLine("==============================")
                .appendFieldLine("Group Name", groupName)
                .appendFieldLine("Base Hook Count", factory.baseCount())
                .appendFieldLine("Rule Hook Count", factory.ruleCount())
                .appendFieldLine("Settings Count", createdSettings.size())
                .appendLine("==============================")
                .toString(true);
    }
}
