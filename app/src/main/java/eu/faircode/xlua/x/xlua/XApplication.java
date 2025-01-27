package eu.faircode.xlua.x.xlua;

import android.content.Context;

import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.configs.XPConfig;

/*
         ToDo: Finish this, move MOST if not ALL "app" bull shit references to here pls!
                    Assignments etc everything shall be held here
                    Create copy of Assignments etc etc
 */
public class XApplication {
    public int icon;

    public int uid;
    public String name;
    public String packageName;

    public boolean kill;

    public void updateKill(Context context) { this.kill = GetSettingExCommand.getBool(context, GetSettingExCommand.SETTING_FORCE_STOP, uid, packageName); }


    public void update(Context context) {
        //
    }

    public void applyConfig(Context context, XPConfig config) {
        //
    }
}
