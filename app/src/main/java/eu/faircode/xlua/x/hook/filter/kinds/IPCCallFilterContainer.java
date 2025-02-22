package eu.faircode.xlua.x.hook.filter.kinds;

import android.content.ContentResolver;
import android.util.Log;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.IFilterContainer;
import eu.faircode.xlua.x.hook.filter.SettingPair;
import eu.faircode.xlua.x.xlua.LibUtil;

public class IPCCallFilterContainer extends FilterContainerElement implements IFilterContainer {
    private static final String TAG = LibUtil.generateTag(IPCCallFilterContainer.class);

    public static IFilterContainer create() { return new IPCCallFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.Settings.Call";
    public static final TypeMap DEFINITIONS =
            TypeMap.create()
            .add(ContentResolver.class, "call")
            .add("android.provider.Settings$Secure", "getString")
            .add("android.provider.Settings$Global", "getString")
            .add("android.provider.Settings$System", "getString");

    public IPCCallFilterContainer() { super(GROUP_NAME, DEFINITIONS); }

    /*
        ToDO: Add Support for Authority
     */

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            if(!hasSettings(hook)) {
                factory.removeRule(hook);
            } else {
                //We need to eventually support authority
                List<String> filter = parseMethodAsFilter(hook, true);
                String[] settings = hook.getSettings();
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Hook Call [%s] Rule for Group [%s] was parsed, Filter=[%s] Settings=[%s]", hook.getObjectId(), groupName, Str.joinList(filter), Str.joinArray(settings)));

                for(int i = 0; i < filter.size(); i++) {
                    String item = filter.get(i);
                    SettingPair pair = new SettingPair(item, i, settings);
                    putSettingPair(createCallSetting(pair.name), pair.settingName);
                }
            }
        }

        return isRule;
    }
}