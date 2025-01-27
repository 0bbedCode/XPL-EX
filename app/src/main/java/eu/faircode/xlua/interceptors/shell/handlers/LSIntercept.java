package eu.faircode.xlua.interceptors.shell.handlers;

import android.util.Log;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.interceptors.shell.util.CommandOutputHelper;

public class LSIntercept extends CommandInterceptor implements ICommandIntercept {
    private static final String TAG = "XLua.LSIntercept";

    private static final String LS_INTERCEPT_SETTING = "intercept.shell.ls.bool";

    @SuppressWarnings("unused")
    public LSIntercept() { this.command = "ls"; this.setting = LS_INTERCEPT_SETTING; }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        if(result != null && result.isValid) {
            UserContextMaps maps = result.getUserMaps();
            if(maps != null) {
                if(!keepGoing(maps, LS_INTERCEPT_SETTING))
                    return true;

                String com = result.getCommandLine();
                if(com.length() <= 3)
                    return false;

                String afterLs = "";
                String toRemove = "ls";
                int index = com.indexOf(toRemove);
                if(index != -1) {
                    //make sure this is safe
                    try {
                        afterLs = com.substring(0, index) + com.substring(index + toRemove.length());
                    }catch (Exception ignored) { }
                }

                if(afterLs.isEmpty())
                    return false;

                afterLs = afterLs.trim();
                if(afterLs.startsWith("-")) {
                    for(int i = 0; i < afterLs.length(); i++) {
                        char c = afterLs.charAt(i);
                        if(i != 0) {
                            if(!Character.isAlphabetic(c))
                                break;

                            if(c == 'i' || c == 'l') {
                                result.setNewValue("ls: cannot access '': No such file or directory");
                                result.setIsMalicious(true);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
