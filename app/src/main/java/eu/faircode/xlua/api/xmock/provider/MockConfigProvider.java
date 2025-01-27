package eu.faircode.xlua.api.xmock.provider;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.util.Map;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.xstandard.interfaces.ISettingsConfig;
import eu.faircode.xlua.utilities.StringUtil;

public class MockConfigProvider {
    private static final String TAG = "XLua.XMockPhoneProvider";

    public static boolean applySettingsConfig(Context context, ISettingsConfig config, String packageName, XDatabaseOld db) { return applySettingsConfig(context, config, packageName, XUtil.getUserId(Process.myUid()), db); }
    public static boolean applySettingsConfig(Context context, ISettingsConfig config, String packageName, int userId, XDatabaseOld db)  {
        if(config == null)
            return false;

        Map<String, String> settings = config.getSettings();
        if(settings == null || settings.size() < 1)
            return false;

        Log.i(TAG, "setting config name=" + config.getName() + " settings size=" + settings.size());
        int inserted = 0;
        int failed = 0;
        try {
            if(!db.beginTransaction(true)) {
                Log.e(TAG, "Failed to being Transaction on DB: " + db + " for applying settings config");
                return false;
            }

            for(Map.Entry<String, String> r : settings.entrySet()) {
                String n = r.getKey();
                String v = r.getValue();
                if(!StringUtil.isValidString(v))
                    continue;

                //XLuaLuaSetting setting = new XLuaSettingPacket(userId, packageName, n, v);
                //if(!db.insert(XLuaLuaSetting.Table.name, setting.createContentValues())) {
                //    Log.e(TAG, "Failed to insert setting: name=" + n + " value=" + v);
                //    failed++;
                //}else {
                //    inserted++;
                //}
            }

            db.setTransactionSuccessful();
            return true;
        }finally {
            Log.i(TAG, "Inserted settings successfully=" + inserted + " failed=" + failed + " config name=" + config.getName());
            db.endTransaction(true, false);
        }
    }
}
