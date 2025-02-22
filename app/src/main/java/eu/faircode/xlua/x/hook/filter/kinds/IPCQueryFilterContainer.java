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

public class IPCQueryFilterContainer extends FilterContainerElement implements IFilterContainer {
    private static final String TAG = LibUtil.generateTag(IPCQueryFilterContainer.class);

    public static IFilterContainer create() { return new IPCQueryFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.Intent.Query";
    public static final TypeMap DEFINITIONS =
            TypeMap.create()
                    .add(ContentResolver.class, "query");


    public IPCQueryFilterContainer() { super(GROUP_NAME, DEFINITIONS); }

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            if(!hasSettings(hook)) {
                factory.removeRule(hook);
            } else {
                List<String> filter = parseMethodAsFilter(hook, true);
                String compressedFilter = Str.joinList(filter, "|");

                List<String> authorities = parseArgsAsAuthorities(hook);
                String[] settings = hook.getSettings();
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Hook Query [%s] Rule for Group [%s] is being parsed, Filter=[%s] Settings=[%s] Authorities=[%s]", hook.getObjectId(), groupName, compressedFilter, Str.joinArray(settings), Str.joinList(authorities)));

                //First Create the target authority settings
                for(String auth : authorities) {
                    String name = createAuthoritySetting(auth);
                    putSettingPair(name, compressedFilter);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Pushing Query Authority [%s] as a Setting for Hook Id [%s] Group Name [%s] with Value=[%s]", name, hook.getObjectId(), groupName, compressedFilter));
                }

                for(int i = 0; i < filter.size(); i++) {
                    String item = filter.get(i);
                    SettingPair pair = new SettingPair(item, i, settings);
                    for(String auth : authorities) {
                        String name = createQuerySetting(auth, pair.name);
                        putSettingPair(name, pair.settingName);
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Pushing Query Setting [%s] to Map to Setting [%s] Group Name [%s]", name, pair.settingName, groupName));
                    }
                }
            }
        }

        return isRule;
    }
}