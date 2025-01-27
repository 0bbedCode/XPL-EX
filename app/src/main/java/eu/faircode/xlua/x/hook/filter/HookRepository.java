package eu.faircode.xlua.x.hook.filter;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xlua.XLuaQuery;
import eu.faircode.xlua.x.hook.filter.kinds.FileFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCBinderFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCCallFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCQueryFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.ShellFilterContainer;

public class HookRepository {
    private static final String TAG = "XLua.HookRepository";

    public static HookRepository create() { return new HookRepository(); }

    private final List<XLuaHook> mHooks = new ArrayList<>();
    private final List<IFilterContainer> mFilters = Arrays.asList(
            FileFilterContainer.create(),
            IPCBinderFilterContainer.create(),
            IPCCallFilterContainer.create(),
            IPCQueryFilterContainer.create(),
            ShellFilterContainer.create());

    public List<XLuaHook> getHooks() {
        return mHooks;
    }

    public HookRepository doFiltering(
            Context context,
            Collection<XLuaHook> assignedHooks,
            Map<String, String> settings) {

        //Init Hook Definition Bases, so there are special Hooks under category of Filter or Interceptors
        //We will first Init them to their groups, they are disabled so they can not be assigned via app
        Collection<XLuaHook> allHooks = XLuaQuery.getAllHooks(context, true);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Creating the Filter/Interceptor Groups and their Disabled Hooks... Total Hook Size=" + allHooks.size() + " Settings Size=" + settings.size());

        for(XLuaHook hook : allHooks) {
            for(IFilterContainer container : mFilters) {
                if(container.hasSwallowedAsDefinition(hook)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Hook was Swallowed into a Container as a Definition, GroupName=" + container.getGroupName() + " HookId=" + hook.getId());
                    break;
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Created the Filter/Interceptor Groups, now combing through assigned hooks for Filter Rules and Non Rules... Assigned Hooks Size=" + assignedHooks.size());

        for(XLuaHook assignedHook : assignedHooks) {
            boolean wasSwallowed = false;
            for(IFilterContainer container : mFilters) {
                if(container.hasSwallowedAsRule(assignedHook)) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Hook was Swallowed into a Container as a Rule, GroupName=" + container.getGroupName() + " HookId=" + assignedHook.getId());
                    wasSwallowed = true;
                    break;
                }
            }

            if(!wasSwallowed) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Adding Hook to Assigned List of Hooks (not a rule), HookId=" + assignedHook.getId());
                mHooks.add(assignedHook);
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished Combing through Filter Rules and non Rules, now Initializing each Filter for its Settings and Base Definitions if filter used. New Assigned Hooks Size=" + mHooks.size() + " Old Assigned Hooks Size=" + assignedHooks.size() + " Settings Size=" + settings.size());

        for(IFilterContainer container : mFilters) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Initializing Filter/Interceptor Group=" + container.getGroupName() + " Settings and Base Definitions if needed, toString=" + container.toString());

            container.initializeDefinitions(mHooks, settings);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished Initializing all Assigned Hooks, Rules, Filters, Original Assigned Hooks Size=" + assignedHooks.size() + " New Assigned Hooks Size=" + mHooks.size() + " Settings Size=" + settings.size());

        return this;
    }
}
