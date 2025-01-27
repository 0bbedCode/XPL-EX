package eu.faircode.xlua.x.hook.filter;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.FilterHooksHolder;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.data.utils.ArrayUtils;

public class FilterContainerElement implements IFilterContainer {
    private static final String TAG = "XLua.FilterContainerElement";

    protected FilterHooksHolder holder = FilterHooksHolder.create();
    protected TypeMap definitions;
    protected String groupName;
    protected HashMap<String, String> settings = new HashMap<>();

    public FilterContainerElement(String groupName, TypeMap definitions) { this.groupName = groupName; this.definitions = definitions; }

    @Override
    public String getGroupName() { return groupName; }

    @Override
    public boolean hasSwallowedAsDefinition(XLuaHook hook) {
        if(hook.getGroup().equalsIgnoreCase(groupName)) {
            if(definitions.hasDefinition(hook.getClassName(), hook.getMethodName())) {
                holder.addBase(hook);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Hook [" + hook.getId() + "] Is a base Hook from Group [" + groupName + "] " + Str.ensureNoDoubleNewLines(Str.hookToJsonString(hook)));
                //Rules can be parsed in different ways, only thing reserved is class name indicating the group
                //Group is Reserved for the group as well but they are base hooks (actual hooks) when the group is the class name (below) then its a rule
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        if(hook.getClassName().equalsIgnoreCase(groupName)) {
            holder.addRule(hook);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Hook [" + hook.getId() + "] Is a Rule Hook from Group [" + groupName + "] " + Str.ensureNoDoubleNewLines(Str.hookToJsonString(hook)));

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
                            if(!TextUtils.isEmpty(settingValue)) {
                                settings.put(settingName, settingValue);
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void initializeDefinitions(List<XLuaHook> hooks, Map<String, String> settings) {
        if(hooks != null) {
            if(holder.hasRules() && holder.hasBases())
                hooks.addAll(holder.getBaseHooks());
        }

        if(settings != null) {
            if(!this.settings.isEmpty()) {
                settings.putAll(this.settings);
            }
        }
    }

    @Override
    public List<XLuaHook> getRules() { return holder.getRuleHooks();  }

    @Override
    public List<XLuaHook> getFilterBases() { return holder.getBaseHooks(); }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendLine("==============================")
                .appendFieldLine("Group Name", groupName)
                .appendFieldLine("Base Hook Count", holder.baseCount())
                .appendFieldLine("Rule Hook Count", holder.ruleCount())
                .appendFieldLine("Settings Count", settings.size())
                .appendLine("==============================")
                .toString(true);
    }
}
