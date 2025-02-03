package eu.faircode.xlua.x.hook.interceptors.file.stat;

import android.system.StructStat;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;

public class StatContainer {
    private static final String TAG = "XLua.StatContainer";

    public String file;
    public final StatMockSettings fakeSettings;

    public StatContainer(String file, XParam param) {
        this.file = file;
        fakeSettings = new StatMockSettings(this.file);
        if(param != null) {
            fakeSettings.setRomStartSeconds(param);
            fakeSettings.setTimeZoneOffset(param);
        }
    }

    public boolean interceptCommand(String output, ShellInterception result) {
        try {
            if(DebugUtil.isDebug()) {
                Log.d(TAG, "Parsing STAT Command Output for File=" + file + " Output=" + output);
                Log.d(TAG, "Parsing STAT Command Output for File=" + file + " Settings=" + fakeSettings.toString());
            }

            String newOutput = StatFileParser.parseFake(output, fakeSettings);
            if(DebugUtil.isDebug())
                Log.d(TAG, StrBuilder.create()
                        .ensureOneNewLinePer(true)
                        .appendFieldLine("File", file)
                        .appendLine("--------")
                        .appendFieldLine("Old Size", output.length())
                        .appendFieldLine("Old Hex", Str.toHex(output))
                        .appendLine("--------")
                        .appendFieldLine("Old", output)
                        .appendLine("--------")
                        .appendFieldLine("New Size", newOutput.length())
                        .appendFieldLine("New Hex", Str.toHex(newOutput))
                        .appendLine("--------")
                        .appendFieldLine("New", newOutput)
                        .toString(true));
            if(newOutput.equalsIgnoreCase(output)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Skipping Cleaning Process Output as its already been Clean");
                return false;//The Processes are the Same, No need to Intercept whats Already Cleaned
            }

            result.setIsMalicious(true);
            result.setNewValue(newOutput);
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Intercept STAT command output, File=" + file + " Output=" + output + " Error=" + e + " Stack=" + Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean interceptFileLastModified(XParam param) {
        try {
            //If its a APK ? then use the List of APPS ????
            Long result = param.tryGetResult(0L);
            if(result == null) return false;
            if(result != 0L) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Handling File Last Modified, on File=" + file + " Last Modified=" + result);
                if(DebugUtil.isDebug()) Log.d(TAG, fakeSettings.toString());

                String original = StatUtils.lastModifiedToString(result);
                String newValue = fakeSettings.getField("modify", original);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "File.lastModified(" + this.file + ") Original=" + original + " Original MILLIS=" + result + " New=" + newValue + " New MILLIS=" + StatUtils.stringToLastModified(newValue));

                param.setOldResult("File=" + file + "\nDate=" + original);
                param.setNewResult("File=" + file + "\nDate=" + newValue);

                param.setResult(StatUtils.stringToLastModified(newValue));
                if(DebugUtil.isDebug()) Log.d(TAG, fakeSettings.toString());
                return true;
            } else {
                Log.e(TAG, "File Last Modified is not 0 [File.lastModified] File=" + file);
                return false;
            }
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting File Last Modified, Error=" + e);
            return false;
        }
    }

    public boolean interceptStruct(XParam param) {
        try {
            Object result = param.tryGetResult(null);
            if(result instanceof StructStat) {
                fakeSettings.cleanStructure(result, param);



                param.setResult(result);
                if(DebugUtil.isDebug()) Log.d(TAG, fakeSettings.toString());
                return true;
            } else {
                Log.e(TAG, "Struct STAT is not Type [StructStat] ! Skipping, type=" + (result == null ? "null" : result.getClass().getName()) + " File=" + file);
                return false;
            }
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting Struct for STAT, Error=" + e + " Stack=" + Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean interceptCommand() {
        return false;
    }
}
