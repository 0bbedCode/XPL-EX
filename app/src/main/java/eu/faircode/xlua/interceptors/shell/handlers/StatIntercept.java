package eu.faircode.xlua.interceptors.shell.handlers;

import android.util.Log;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.interceptors.shell.util.CommandOutputHelper;

public class StatIntercept extends CommandInterceptor implements ICommandIntercept {
    private static final String TAG = "XLua.StatIntercept";


    private static final String STAT_INTERCEPT_SETTING = "intercept.shell.stat.bool";

    @SuppressWarnings("unused")
    public StatIntercept() { this.command = "stat"; }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        if(result != null && result.isValid) {
            UserContextMaps maps = result.getUserMaps();
            if(maps != null) {
                if(!keepGoing(maps, STAT_INTERCEPT_SETTING)) return true;

                String newResult = CommandOutputHelper.randomizeStatOutput(result.getCommandOutput());
                if(BuildConfig.DEBUG)
                    Log.w(TAG, newResult);

                result.setNewValue(newResult);
                result.setIsMalicious(true);
                return true;
            }
        }
        return false;
    }
}
