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

public class IPCQueryFilterContainer extends FilterContainerElement implements IFilterContainer {
    private static final String TAG = "XLua.IPCQueryFilterContainer";

    public static IFilterContainer create() { return new IPCQueryFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.Intent.Query";
    public static final TypeMap DEFINITIONS = TypeMap.create().add(ContentResolver.class, "query");

    public IPCQueryFilterContainer() { super(GROUP_NAME, DEFINITIONS); }

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {

            String authority = Str.ensureIsValidOrDefault(hook.getMethodName(), "*");
            String[] badKeys = hook.getParameterTypes();
            String[] settingsForKeys = hook.getSettings();

            if(!ArrayUtils.isValid(badKeys) || !ArrayUtils.isValid(settingsForKeys)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Query Rule is Invalid, Keys or Settings for Keys is Null, hookId=" + hook.getId() + " " + Str.ensureNoDoubleNewLines(Str.hookToJsonString(hook)));

                holder.removeRule(hook);
                return true;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Query Rule Settings for Authority: " + authority + " Parsing the Keys... Key Size=" + badKeys.length + " Settings for Keys Size=" + settingsForKeys.length + " Hook=" + Str.ensureNoDoubleNewLines(Str.hookToJsonString(hook)));

            String badKeysSetting = Str.joinArray(badKeys, "|");
            String badKeysSettingName = "query:" + authority;
            settings.put(badKeysSettingName, badKeysSetting);

            for(int i = 0; i < badKeys.length; i++) {
                String key = Str.trim(badKeys[i], " ", true, true);
                if(Str.isValidNotWhitespaces(key)) {
                    SettingPair pair = new SettingPair(key, i, settingsForKeys);
                    String settingRemap = "query:[" + authority + "]:" + pair.name;
                    settings.put(settingRemap, pair.settingName);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Pushed Query Setting=" + settingRemap + " Setting Map=" + pair.settingName);
                }
            }
        }

        return isRule;
    }
}