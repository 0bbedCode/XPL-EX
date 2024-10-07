package eu.faircode.xlua.interceptors.shell.handlers;

import android.util.Log;

import java.util.List;

import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.StringUtil;

public class SuIntercept extends CommandInterceptor implements ICommandIntercept {
    private static final String TAG = "XLua.SuIntercept";

    private static final String SU_INTERCEPT_SETTING = "intercept.shell.su.bool";

    @SuppressWarnings("unused")
    public SuIntercept() { this.command = "su"; }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        //Dead
        if(result != null && result.isValid) {
            UserContextMaps maps = result.getUserMaps();
            if(maps != null) {
                String low = result.getCommandLine().toLowerCase().trim();
                if(!StringUtil.isValidString(low)) {
                    Log.e(TAG, "Some how the String low is null or empty...");
                    return false;
                }

                List<String> parts = StringUtil.breakStringExtreme(low);
                if(CollectionUtil.isEmptyValuesOrInvalid(parts)) {
                    Log.e(TAG, "String List after low broken is NULL or Empty...");
                    return false;
                }

                if(parts.get(0).equals(this.command)) {
                    Log.w(TAG, "(" + this.command + ")" + " command was found...");
                    if(!keepGoing(maps, SU_INTERCEPT_SETTING)) return true;
                    result.setNewValue("");
                    result.setIsMalicious(true);
                    Log.i(TAG, "blocking (" + this.command + ") command from executing...");
                    return true;
                }
            }
        } return false;
    }
}
