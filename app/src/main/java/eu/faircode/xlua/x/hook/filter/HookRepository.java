package eu.faircode.xlua.x.hook.filter;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.hook.filter.kinds.FileFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.GetPropFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCBinderFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCCallFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCQueryFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.ShellFilterContainer;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;

public class HookRepository {
    private static final String TAG = LibUtil.generateTag(HookRepository.class);

    public static HookRepository create() { return new HookRepository(); }

    public final LinkedHashMap<String, XLuaHook> hooks = new LinkedHashMap<>();
    public final LinkedHashMap<String, IFilterContainer> filters = new LinkedHashMap<>();

    public List<XLuaHook> getHooks() { return ListUtil.copyToArrayList(hooks.values()); }

    private void putHook(XLuaHook hook) { if(hook != null) hooks.put(hook.getObjectId(), hook); }
    private void putInterceptor(IFilterContainer filter) { if(filter != null) filters.put(filter.getGroupName(), filter); }

    public HookRepository() {
        putInterceptor(FileFilterContainer.create());
        putInterceptor(IPCBinderFilterContainer.create());
        putInterceptor(IPCCallFilterContainer.create());
        putInterceptor(IPCQueryFilterContainer.create());
        putInterceptor(ShellFilterContainer.create());
        putInterceptor(GetPropFilterContainer.create());
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Loaded [%s] Filters ! Groups=[%s]",
                    filters.size(),
                    Str.joinList(ListUtil.copyToList(filters.keySet()))));
    }

    public HookRepository initializeHooks(Context context, Collection<XLuaHook> assignedHooks, Map<String, String> settings) {
        try {
            //[0] Ensure valid ears https://www.youtube.com/watch?v=tlO3lZhDukU
            if(settings == null)
                throw new Exception("Settings Map is Null...");

            if(!ListUtil.isValid(assignedHooks))
                throw new Exception("No Hooks are Assigned returning...");

            //[1] Get (ALL) the Hooks (we want to find the Filter Definitions)
            Collection<XLuaHook> allHooks = GetHooksCommand.getHooks(context, true, true);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("[1] Going through (ALL) (%s) Hook Definitions to Resolve (%s) Filters Definitions. Settings Count=%s, assignment Count=%s ",
                        ListUtil.size(allHooks),
                        ListUtil.size(filters),
                        ListUtil.size(settings),
                        ListUtil.size(assignedHooks)));

            if(ListUtil.isValid(allHooks)) {
               for(XLuaHook hook : allHooks) {
                   if(hook == null)
                       continue;

                   String groupName = hook.getGroup();
                   if(Str.isEmpty(groupName) || !groupName.toLowerCase().startsWith("intercept"))   //double check
                       continue;

                   IFilterContainer filterContainer = filters.get(groupName);
                   if(filterContainer == null)
                       continue;

                   //[1.1] Means this meets this filters allowed Hook Definitions to make the Hook Possible (actual Java Hook Definition not a Rule and a Filter Intercept Definition)
                   if(filterContainer.hasSwallowedAsDefinition(hook)) {
                       if(DebugUtil.isDebug())
                           Log.d(TAG, Str.fm("[1.1] Filter Group [%s] has Swallowed Hook [%s] as a Definition! Class=%s Method=%s",
                                   groupName,
                                   hook.getObjectId(),
                                   hook.getClassName(),
                                   hook.getMethodName()));
                   }
               }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("[2] Finished initializing Filter Hook Definitions for (%s) Filters, Total of (%s) Hooks. Now cleaning (%s) Assigned Hooks ensuring split between a Rule and actual Hook Definition",
                        ListUtil.size(filters),
                        ListUtil.size(allHooks),
                        ListUtil.size(assignedHooks)));

            //[2] Go through Assigned Hooks, and Detect Rules for the Filters to bind the Rule to the Target Filter
            for(XLuaHook hook : assignedHooks) {
                if(hook == null)
                    continue;

                //[1.2] Class Name of the Rule should be the Filter Group Name
                //      -> Filter Hook Definitions should be not enabled by default, so no Filters should exist in this list
                String className = hook.getClassName();
                IFilterContainer filterContainer = filters.get(className);
                if(filterContainer != null) {
                    if(filterContainer.hasSwallowedAsRule(hook)) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("[2.1] Filter Group [%s] has Swallowed a Hook [%s] as a Rule!",
                                    className,
                                    hook.getObjectId()));
                    }
                } else {
                    //Assume it is an actual Hook, pointing to a Real Java Class / Method / Field
                    //Should never be a Hook Definition for a Filter!
                    putHook(hook);
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("[3] Finished Resolving (%s) Filters Hook Definition wants, Assignment Count [%s] Settings Count [%s]. Now Merging all the used Filters! ",
                        ListUtil.size(filters),
                        ListUtil.size(assignedHooks),
                        ListUtil.size(settings)));

            //[3] Go through Every Filter, add their Hook Definitions to Hooks if we have (Settings, or Rules)
            for(IFilterContainer filter : filters.values()) {
                //[3.1] Get the Filter Group Container -> Rules, Definitions, Dependencies
                List<XLuaHook> rules = filter.getRules();
                List<XLuaHook> definitions = filter.getFilterBases();
                List<String> dependencies = filter.getDependencies();
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("[3.1] Merging Possible Filter Container [%s] Rules Count=[%s] Definitions=[%s] Dependencies=[%s]",
                            filter.getGroupName(),
                            ListUtil.size(rules),
                            ListUtil.size(definitions),
                            ListUtil.size(definitions)));

                //[3.2] Add the Filter Group Container Target Function (filter) Hook Definitions if Has Definitions AND (Has Rules OR Settings) , this will indicate we need the Hooks
                if(ListUtil.isValid(definitions) && (ListUtil.isValid(rules) || filter.hasSettings())) {
                    for(XLuaHook definition : definitions) {
                        putHook(definition);
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("[3.2] Appended Definition Hook Hook [%s] required by [%s] Filter",
                                    definition.getObjectId(),
                                    filter.getGroupName()));
                    }
                }

                //[3.4] Append thr Settings from the Filter, these are rules mainly, as in Rules Control this list
                int count = filter.appendSettings(settings);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("[3.4] Appended (%s) Settings, with total Settings Count=(%s) Assignment Count=(%s) Filters Count=(%s). Now appending (%s) Definitions",
                            count,
                            ListUtil.size(settings),
                            ListUtil.size(assignedHooks),
                            ListUtil.size(filters),
                            ListUtil.size(definitions)));

                if(!ListUtil.isValid(definitions))
                    continue;

                //[3.5] Go through all Dependencies required by the Filter if any
                for(String dependency : dependencies) {
                    if(Str.isEmpty(dependency))
                        continue;

                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("[3.5] Resolving Dependency [%s] for [%s] Filter...",
                                filter.getGroupName(),
                                dependency));

                    //Handle it settings
                    IFilterContainer dep = filters.get(dependency);
                    if(dep == null)
                        continue;

                    List<XLuaHook> depDefinitions = dep.getFilterBases();
                    if(!ListUtil.isValid(depDefinitions))
                        continue;

                    //[3.6] Append the Hooks that are used within the Dependency Filter
                    for(XLuaHook depHook : depDefinitions) {
                        putHook(depHook);   //Some extra func invoke ?
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("[3.6] Appended Dependency Group [%] Hook [%s] required by [%s] Filter. Definition Count for Dependency [%s]",
                                    dependency,
                                    depHook.getObjectId(),
                                    filter.getGroupName(),
                                    ListUtil.size(depDefinitions)));
                    }
                }
            }

        }catch (Exception e) {
            Log.e(TAG, Str.fm("Error Initializing Hooks! Assigned Count=[%s] Settings Count=[%s] Filter Count=[%s] Internal Hooks Count=[%s] Error=%s",
                    ListUtil.size(assignedHooks),
                    ListUtil.size(settings),
                    ListUtil.size(filters),
                    ListUtil.size(hooks),
                    e));
        }

        return this;
    }
}
