package eu.faircode.xlua.x.xlua.settings.deprecated;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.settings.NameInformation;
import eu.faircode.xlua.x.xlua.settings.NameInformationKind;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class SettingsMapHolder_deprecated {
    private static final String TAG = "XLua.SettingsMapHolder";

    private Map<String, SettingsContainer> containers = new HashMap<>();
    private Map<String, SettingHolder> settings = new HashMap<>();

    public List<SettingsContainer> getContainers() { return ListUtil.copyToArrayList(containers.values()); }
    public List<SettingHolder> getAllSettings() { return ListUtil.copyToArrayList(settings.values()); }

    public void pushSetting(SettingPacket setting) {
        if(setting != null) {
            NameInformation namedInformation = NameInformation.create(setting.name);

            if(namedInformation.kind == NameInformationKind.UNKNOWN) {
                Log.e(TAG, "Critical error, name information is unknown, skipping... NameInfo = " + namedInformation);
                return;
            }

            String containerName = namedInformation.getContainerName();
            SettingsContainer container = containers.get(containerName);
            if(container == null) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Container is missing from the Container list, creating a Container for: " + containerName);

                //container = containers.get(containerName);
                container = new SettingsContainer(namedInformation, containerName);
                containers.put(containerName, container);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Name information parsed for : " + setting.name + " Name Information=" + namedInformation);

            if(!namedInformation.hasChildren()) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Lua Setting is not a Container, may be Single or a Child of a Container");

                SettingHolder holder = settings.get(namedInformation.name);
                if(holder == null) {
                    holder = new SettingHolder(namedInformation, setting.value, setting.description);
                    settings.put(holder.getName(), holder);
                } else
                    holder.setValue(setting.value, true);
            } else {
                for(NameInformation c_name_info : namedInformation.getChildrenNames()) {
                    SettingHolder holder = settings.get(c_name_info.name);
                    if(holder == null) {
                        holder = new SettingHolder(namedInformation, null, setting.description);
                        settings.put(holder.getName(), holder);
                    }
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished parsing Extended lua Settings,  Global Settings Count=" + settings.size() + " and Container Count=" + containers.size());

    }

    public void pushLuaSetting(LuaSettingExtended setting) {
        if(setting != null) {
            NameInformation namedInformation = NameInformation.create(setting.getName());
            //[1] setting.value[1,2] => setting.value (value after parsed) and now 2 (child) settings
            //[2] setting.value.cool => setting.value.cool  *just ensure container exists for (it) most likely a Single Settings, its own Container is it.

            if(namedInformation.kind == NameInformationKind.UNKNOWN) {
                Log.e(TAG, "Critical error, name information is unknown, skipping... NameInfo = " + namedInformation);
                return;
            }

            String containerName = namedInformation.getContainerName();
            SettingsContainer container = containers.get(containerName);
            if(container == null) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Container is missing from the Container list, creating a Container for: " + containerName);

                //container = containers.get(containerName);
                container = new SettingsContainer(namedInformation, containerName);
                containers.put(containerName, container);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Name information parsed for : " + setting.getName() + " Name Information=" + namedInformation);

            if(!namedInformation.hasChildren()) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Lua Setting is not a Container, may be Single or a Child of a Container");

                SettingHolder holder = settings.get(namedInformation.name);
                if(holder == null) {
                    holder = new SettingHolder(namedInformation, setting.getValue(), setting.getDescription());
                    settings.put(holder.getName(), holder);
                } else {
                    holder.setValue(setting.getValue(), null, true);
                }
            } else {
                for(NameInformation c_name_info : namedInformation.getChildrenNames()) {
                    SettingHolder holder = settings.get(c_name_info.name);
                    if(holder == null) {
                        holder = new SettingHolder(namedInformation, null, setting.getDescription());
                        settings.put(holder.getName(), holder);
                    }
                }
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Finished parsing Extended lua Settings,  Global Settings Count=" + settings.size() + " and Container Count=" + containers.size());

    }

    public void finalizeTransaction() {
        for(Map.Entry<String, SettingHolder> pair : settings.entrySet()) {
            SettingHolder holder = pair.getValue();
            NameInformation nameInfo = holder.getNameInformation();
            if(nameInfo.hasChildren()) {
                Log.w(TAG, "Weird setting has Children, how is it on the global settings list: " + nameInfo.name);
                continue;
            }

            String containerName = nameInfo.getContainerName();
            SettingsContainer container = containers.get(containerName);
            if(container == null) {
                container = new SettingsContainer(containerName);
                containers.put(containerName, container);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Container was not Found for the stranded child setting, created container! Name=" + containerName);
            }

            container.ensureHasChild(holder);
        }

        for(Map.Entry<String, SettingsContainer> pair : containers.entrySet()) {
            SettingsContainer container = pair.getValue();
            if(container != null) {
                container.finalizeContainer();
            }
        }
    }

}
