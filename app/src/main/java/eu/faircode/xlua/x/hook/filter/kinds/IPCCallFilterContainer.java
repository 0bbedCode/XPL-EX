package eu.faircode.xlua.x.hook.filter.kinds;

import android.content.ContentResolver;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.IFilterContainer;
import eu.faircode.xlua.x.hook.filter.SettingPair;

public class IPCCallFilterContainer extends FilterContainerElement implements IFilterContainer {
    private static final String TAG = "XLua.IPCCallFilterContainer";

    public static IFilterContainer create() { return new IPCQueryFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.Settings.Call";
    public static final TypeMap DEFINITIONS = TypeMap.create()
            .add(ContentResolver.class, "call")
            .add("android.provider.Settings$Secure", "getString")
            .add("android.provider.Settings$Global", "getString")
            .add("android.provider.Settings$System", "getString");

    public IPCCallFilterContainer() { super(GROUP_NAME, DEFINITIONS); }

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            String authority = Str.ensureIsValidOrDefault(hook.getMethodName(), "*");
            String[] badKeys = hook.getParameterTypes();
            String[] settingsForKeys = hook.getSettings();

            if(!ArrayUtils.isValid(badKeys) || !ArrayUtils.isValid(settingsForKeys)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Call/Settings Rule is Invalid, Keys or Settings for Keys is Null, hookId=" + hook.getId() + " " + Str.ensureNoDoubleNewLines(Str.hookToJsonString(hook)));

                holder.removeRule(hook);
                return true;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Call/Settings Rule Settings: " + authority + " (we ignore authority) Parsing the Keys... Key Size=" + badKeys.length + " Settings for Keys Size=" + settingsForKeys.length + " Hook=" + Str.ensureNoDoubleNewLines(Str.hookToJsonString(hook)));

            for(int i = 0; i < badKeys.length; i++) {
                String key = Str.trim(badKeys[i], " ", true, true);
                if(Str.isValidNotWhitespaces(key)) {
                    SettingPair pair = new SettingPair(key, i, settingsForKeys);
                    String settingRemap = "call:" + pair.name;
                    settings.put(settingRemap, pair.settingName);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Pushed Call/Settings Setting=" + settingRemap + " Setting Map=" + pair.settingName);
                }
            }
        }

        return isRule;
    }
}