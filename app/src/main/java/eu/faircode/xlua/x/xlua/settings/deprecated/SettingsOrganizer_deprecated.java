package eu.faircode.xlua.x.xlua.settings.deprecated;

import java.util.List;

import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

//Should make a Certain interface / thing for these types of things ? where it requires a ID and VALUE is a GHOST Data Object awaiting for Bind or Taken

@SuppressWarnings("all")
public class SettingsOrganizer_deprecated {
    private static final String TAG = "XLua.SettingsOrganizer";

    public static SettingsOrganizer_deprecated create() { return new SettingsOrganizer_deprecated(); }

    private SettingsMapHolder_deprecated map = new SettingsMapHolder_deprecated();

    public List<SettingsContainer> getSettingContainers() { return map.getContainers(); }
    public List<SettingHolder> getAllSettings() { return map.getAllSettings(); }

    public void parseList_final(List<SettingPacket> settings) {
        if(ListUtil.isValid(settings)) {
            for(SettingPacket setting : settings)
                map.pushSetting(setting);

            map.finalizeTransaction();
        }
    }

    //public static boolean isSingleOrContainer(NameInformation nameInformation) {  return nameInformation != null && (!nameInformation.endsWithNumber || nameInformation.hasChildren()); }
}
