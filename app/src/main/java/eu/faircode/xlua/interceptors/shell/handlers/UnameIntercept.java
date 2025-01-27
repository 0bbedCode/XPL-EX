package eu.faircode.xlua.interceptors.shell.handlers;

import android.util.Log;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.utilities.StringUtil;

public class UnameIntercept extends CommandInterceptor implements ICommandIntercept  {
    private static final String TAG = "XLua.SuIntercept";

    private static final String UNAME_INTERCEPT_SETTING = "intercept.shell.uname.bool";

    //Android Kernel Default Settings
    //{ "name": "android.kernel.sys.name", "description": "Kernel Kind Name (Linux, Unix...)", "value": "Linux" },
    //{ "name": "android.kernel.version", "description": "Kernel Version Info long", "value": "SMP PREEMPT Tue Oct 3 17:44:36 IDT 2023" },
    //{ "name": "android.kernel.release", "description": "Kernel Release Number", "value": "4.14.326" },
    //{ "name": "android.kernel.node.name", "description": "Kernel Build nodename (localhost)", "value": "localhost" },

    @SuppressWarnings("unused")
    public UnameIntercept() { this.command = "uname"; this.setting = UNAME_INTERCEPT_SETTING; }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        if(result != null && result.isValid) {
            UserContextMaps maps = result.getUserMaps();
            if(maps != null) {
                String low = result.getCommandLine().toLowerCase().trim();
                if(!StringUtil.isValidString(low)) {
                    if(BuildConfig.DEBUG)
                        Log.e(TAG, "Some how the String low is null or empty...");

                    return false;
                }

                if(!keepGoing(maps, UNAME_INTERCEPT_SETTING)) {
                    if(BuildConfig.DEBUG)
                        Log.w(TAG, "Found " + this.command + " but Setting is not allowing interception bye bye");

                    return true;
                }

                String sysName = maps.getSetting("android.kernel.sys.name", "Linux");                                   //-s    [sys name]
                String version = maps.getSetting("android.kernel.version", "SMP PREEMPT Tue Sep 3 14:02:52 KST 2019");  //-v    [version]
                String release = maps.getSetting("android.kernel.release", "4.9.112-16352588");                         //-r    [release]
                String nodeNme = maps.getSetting("android.kernel.node.name", "localhost");                              //-n    [node name]
                String machine = maps.getSetting("soc.cpu.architecture", "aarch64");                                                //-m    [machine]
                String systems = maps.getSetting("android.build.base.os", "Android");                                   //

                if(low.contains("-s")) {
                    if(BuildConfig.DEBUG)
                        Log.w(TAG, "Command is -s (system name) : " + this.command);

                    result.setNewValue(sysName);
                    result.setIsMalicious(true);
                    return true;
                }

                if(low.contains("-v")) {
                    if(BuildConfig.DEBUG)
                        Log.w(TAG, "Command is -v (version) : " + this.command);

                    result.setNewValue(version);
                    result.setIsMalicious(true);
                    return true;
                }

                if(low.contains("-r")) {
                    if(BuildConfig.DEBUG)
                        Log.w(TAG, "Command is -r (release) : " + this.command);

                    result.setNewValue(release);
                    result.setIsMalicious(true);
                    return true;
                }

                if(low.contains("-n")) {
                    if(BuildConfig.DEBUG)
                        Log.w(TAG, "Command is -n (node name) : " + this.command);

                    result.setNewValue(nodeNme);
                    result.setIsMalicious(true);
                    return true;
                }

                if(low.contains("-m")) {
                    if(BuildConfig.DEBUG)
                        Log.w(TAG, "Command is -m (machine arch) : " + this.command);

                    result.setNewValue(machine);
                    result.setIsMalicious(true);
                    return true;
                }

                if(low.contains("-a")) {
                    if(BuildConfig.DEBUG)
                        Log.w(TAG, "Command is -a (all) : " + this.command);

                    result.setNewValue(new StringBuilder()
                            .append(sysName).append(" ")
                            .append(nodeNme).append(" ")
                            .append(release).append(" ")
                            .append(version).append(" ")
                            .append(machine).append(" ")
                            .append(systems).toString());

                    result.setIsMalicious(true);
                    return true;
                }

                String after = StringUtil.startAtString(this.command, low);
                if(after.equals(this.command)) {
                    if(BuildConfig.DEBUG)
                        Log.w(TAG, "Command is single: " + this.command + " setting as sys name: " + sysName);

                    result.setNewValue(sysName);
                    result.setIsMalicious(true);
                    return true;
                }else {
                    if(BuildConfig.DEBUG)
                        Log.w(TAG, "Could not find mapping for command: " + after);
                }
            }
        } return false;
    }
}
