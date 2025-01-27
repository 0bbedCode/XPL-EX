package eu.faircode.xlua.x.ui.fragments;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.hook.data.AssignmentData;
import eu.faircode.xlua.x.xlua.hook.data.AssignmentState;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.SettingsGroup;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class SettingFragmentUtils {
    private static final String TAG = "XLua.SettingFragmentUtils";


    public static List<String> getHookIds(
            Context context,
            int uid,
            String packageName,
            LiveData<List<SettingsGroup>> liveData,
            SharedRegistry sharedRegistry) {
        List<String> hookIds = new ArrayList<>();
        try {
            ///            SharedRegistry.ItemState state = sharedRegistry.getItemState(SharedRegistry.STATE_TAG_CONTAINERS, item.getContainerName());
            List<SettingsGroup> data = liveData.getValue();
            if(ListUtil.isValid(data)) {
                for(SettingsGroup g : data) {
                    for(SettingsContainer c : g.getContainers()) {
                        if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_CONTAINERS, c.getContainerName())) {
                            AssignmentData assignmentData = CoreUiUtils.ensureAssignmentDataInit(
                                    context,
                                    uid,
                                    packageName,
                                    sharedRegistry,
                                    c,
                                    false);

                            for(AssignmentState state : assignmentData.states) {
                                String id = state.hook.getId();
                                if(!hookIds.contains(id)) {
                                    hookIds.add(id);
                                }
                            }
                        }
                    }
                }
            }

            return hookIds;
        }catch (Exception e) {
            Log.e(TAG, "Error with Hook Ids, Error=" + e);
            return hookIds;
        }
    }

    public static List<String> settingsToNameList(List<SettingHolder> holders) {
        List<String> names = new ArrayList<>();
        if(!ListUtil.isValid(holders))
            return names;

        for(SettingHolder holder : holders) {
            String name = holder.getName();
            if(!Str.isEmpty(name) && !names.contains(name) && !names.contains(","))
                names.add(name);
        }


        return names;
    }

    public static List<SettingPacket> filterCheckedAsPackets(List<SettingHolder> holders, SharedRegistry sharedRegistry) {
        List<SettingPacket> list = new ArrayList<>();
        for(SettingHolder holder : holders) {
            if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, holder.getId())) {
                SettingPacket packet = new SettingPacket(holder.getName(), holder.getValue());
                list.add(packet);
            }
        }

        return list;
    }

    public static List<SettingHolder> filterChecked(List<SettingHolder> holders, SharedRegistry sharedRegistry) {
        List<SettingHolder> list = new ArrayList<>();
        for(SettingHolder holder : holders) {
            if(sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, holder.getId()))
                list.add(holder);
        }

        return list;
    }

    public static List<SettingHolder> getSettings(LiveData<List<SettingsGroup>> liveData) {
        List<SettingHolder> settings = new ArrayList<>();
        try {
            List<SettingsGroup> data = liveData.getValue();
            List<String> added = new ArrayList<>();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Live Data (2) Size=" + ListUtil.size(data));

            if(ListUtil.isValid(data)) {
                for(SettingsGroup g : data) {
                    if(!ListUtil.isValid(g.getContainers()))
                        Log.w(TAG, "Invalid amount of Containers, (2) WTF! Group Name=" + g.getGroupName());

                    for(SettingsContainer c : g.getContainers()) {
                        if(!ListUtil.isValid(c.getSettings()))
                            Log.w(TAG, "Invalid amount of Containers, (2) WTF! Group Name=" + g.getGroupName() + " Container = " + c.getSettings());

                        for(SettingHolder h : c.getSettings()) {
                            if(!added.contains(h.getName())) {
                                added.add(h.getName());
                                settings.add(h);
                            }
                        }
                    }
                }
            }

            return settings;
        }catch (Exception e) {
            Log.e(TAG, "Error Getting Settings! Error=" + e);
            return settings;
        }
    }

    public static Map<SettingHolder, SettingPacket> getSettingPackets(LiveData<List<SettingsGroup>> liveData, SharedRegistry registry, UserClientAppContext context, ActionFlag flag) {
        Map<SettingHolder, SettingPacket> packets = new HashMap<>();
        try {
            List<SettingsGroup> data = liveData.getValue();
            List<String> added = new ArrayList<>();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Live Data Size=" + ListUtil.size(data));

            if(ListUtil.isValid(data)) {
                for(SettingsGroup g : data) {
                    if(!ListUtil.isValid(g.getContainers()))
                        Log.w(TAG, "Invalid amount of Containers, WTF! Group Name=" + g.getGroupName());

                    for(SettingsContainer c : g.getContainers()) {
                        if(!ListUtil.isValid(c.getSettings()))
                            Log.w(TAG, "Invalid amount of Containers, WTF! Group Name=" + g.getGroupName() + " Container = " + c.getSettings());

                        for(SettingHolder h : c.getSettings()) {
                            if(registry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, h.getId())) {
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, "Setting Is Checked: " + h.getName() + " Value=" + h.getValue() + " New Value=" + h.getNewValue() + " Is Not Saved=" + h.isNotSaved() + " Id=" + h.getId());

                                if(h.isNotSaved() && !added.contains(h.getName())) {

                                    added.add(h.getName());

                                    SettingPacket packet = new SettingPacket();
                                    packet.setUserIdentity(UserIdentity.fromUid(context.appUid, context.appPackageName));
                                    packet.setActionPacket(ActionPacket.create(flag, context.kill));
                                    packet.name = h.getName();
                                    packet.value = h.getNewValue();

                                    packets.put(h, packet);
                                }
                            }
                        }
                    }
                }
            }

            return packets;
        }catch (Exception e) {
            Log.e(TAG, "Error Getting Enabled Settings! Error=" + e);
            return packets;
        }
    }
}
