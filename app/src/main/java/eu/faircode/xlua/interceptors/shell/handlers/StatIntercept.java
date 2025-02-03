package eu.faircode.xlua.interceptors.shell.handlers;

import android.text.TextUtils;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.x.hook.interceptors.file.StatInterceptor;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class StatIntercept extends CommandInterceptor implements ICommandIntercept {
    public static final String REGEX_FILE = "\\s*File:\\s*";
    private static final String TAG = LibUtil.generateTag(StatIntercept.class);


    private static final String STAT_INTERCEPT_SETTING = "intercept.shell.stat.bool";

    @SuppressWarnings("unused")
    public StatIntercept() { this.command = "stat"; this.setting = STAT_INTERCEPT_SETTING; }

    @Override
    public boolean isCommand(ShellInterception results) {
        if(results.hasCommand("stat")) {
            if(DebugUtil.isDebug())
                Log.w(TAG, "Command Contains [stat]! Command Line=" + results.getCommandLine());
            return true;
        }

        return false;
    }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        if(result != null && result.isValid) {
            UserContextMaps maps = result.getUserMaps();
            if(maps != null) {
                if(!keepGoing(maps, STAT_INTERCEPT_SETTING)) return true;
                final String output = result.getCommandOutput();
                if(TextUtils.isEmpty(output)) {
                    Log.e(TAG, "STAT Will not be intercepted, Output is Empty! Args=" + result.getCommandLine());
                    return false;
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "STAT Output=" + output);

                String file = "";
                //Get it from the output
                if(output.contains("File:")) {
                    String[] lines = output.split("\\n");
                    for(String line : lines) {
                        if(line.contains("File:")) {
                            file = line.replaceFirst(REGEX_FILE, "").trim();
                            break;
                        }
                    }
                }

                if(TextUtils.isEmpty(file)) {
                    boolean waitingForFile = false;
                    for(String com : result.commandLine) {
                        String c = com.trim();
                        if(!waitingForFile) {
                            if(c.equalsIgnoreCase("stat")) {
                                waitingForFile = true;
                            }
                        } else {
                            if(c.startsWith("-"))
                                continue;

                            if(c.startsWith("/")) {
                                file = c;
                                break;
                            }
                        }
                    }
                }

                if(TextUtils.isEmpty(file) || file.length() < 5) {
                    Log.e(TAG, "Failed to Extract File from STAT, Full Command Line=" + result.getCommandLine() + " Output=" + output);
                    return false;
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "Command Interceptor is Now Cleaning STAT command, for File=" + file + " Output=" + output);

                return StatInterceptor.interceptStatCommand(file, output, result);
            } else {
                Log.e(TAG, "Maps for [stat] Command interception is NULL! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
            }
        } else {
            Log.e(TAG, "Result for Command [stat] Interception is Invalid Some how! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
        }
        return false;
    }
}
