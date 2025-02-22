package eu.faircode.xlua.x.hook.filter.kinds;

import android.util.Log;

import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.interceptors.ShellIntercept;
import eu.faircode.xlua.interceptors.shell.handlers.GetPropIntercept;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.IFilterContainer;
import eu.faircode.xlua.x.hook.filter.SettingPair;
import eu.faircode.xlua.x.xlua.LibUtil;

public class GetPropFilterContainer extends FilterContainerElement implements IFilterContainer {
    private static final String TAG = LibUtil.generateTag(GetPropFilterContainer.class);


    public static IFilterContainer create() {  return new GetPropFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.Properties";
    public static final TypeMap DEFINITIONS =
            TypeMap.create()
            .add("java.lang.System", "getProperty")
            .add("android.os.SystemProperties", "get");

    public GetPropFilterContainer() {
        super(GROUP_NAME, DEFINITIONS);
        this.dependencies.add(ShellFilterContainer.GROUP_NAME);
    }

    @Override
    public int appendSettings(Map<String, String> settings) {
        if(settings == null)
            return 0;

        int count = super.appendSettings(settings);
        if(ListUtil.isValid(getRules()) || count > 0) {
            settings.put(GetPropIntercept.GETPROP_INTERCEPT_SETTING, String.valueOf(true));
            count++;
        }

        return count;
    }

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            if(!hasSettings(hook) || !hasParams(hook)) {
                factory.removeRule(hook);
            } else {
                String[] args = hook.getParameterTypes();
                String[] settings = hook.getSettings();
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Parsing Intercept Property Rule [%s] Properties=[%s] Settings=[%s]",
                            hook.getObjectId(),
                            Str.joinArray(args),
                            Str.joinArray(settings)));

                //We just put to the settings KEY="prop:property_name", VALUE="setting.name"
                for(int i = 0; i < args.length; i++) {
                    String item = Str.trimOriginal(args[i]);
                    if(Str.isEmpty(item))
                        continue;

                    SettingPair pair = new SettingPair(item, i, settings);
                    putSettingPair(createPropertySetting(pair.name), pair.settingName);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Pushed Property, Setting Name=%s Mapped Setting Name=%s",
                                pair.name,
                                pair.settingName));
                }
            }
        }

        return isRule;
    }
}
