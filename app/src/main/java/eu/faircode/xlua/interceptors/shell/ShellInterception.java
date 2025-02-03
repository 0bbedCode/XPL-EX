package eu.faircode.xlua.interceptors.shell;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.util.CommandOutputHelper;
import eu.faircode.xlua.utilities.ShellUtils;
import eu.faircode.xlua.x.process.ProcessUtils;


public class ShellInterception {
    private static final String TAG = "XLua.ShellInterception";

    public boolean isValid = true;
    public String[] commandLine = new String[]{ };
    public String[] cleanedCommandLine = null;

    public Process process;
    public XParam param;

    private UserContextMaps mContextMaps = null;
    private String mNewValue = null;
    private boolean mIsMalicious = false;
    private String originalOutput = null;

    public String getNewValue() { return mNewValue; }
    public boolean isMalicious() { return mIsMalicious; }
    public void setIsMalicious(boolean isMalicious) { this.mIsMalicious = isMalicious; }
    public void setNewValue(String newValue) { this.mNewValue = newValue; }
    public UserContextMaps getUserMaps() { return mContextMaps; }
    public String getCommandLine() { return Str.joinArray(commandLine, " "); }

    @SuppressWarnings("unused")
    public Process getEchoProcess() {
        if(DebugUtil.isDebug()) {
            String original = getCommandOutput();
            String newVal = this.getNewValue();
            if(DebugUtil.isDebug()) {
                Log.d(TAG, "Executing Echo Data Original Size: " + original.length() + " New Size: " + newVal.length() + " New Output=" + newVal);
                Log.d(TAG, "Executing Echo Data Original Hex=" + Str.toHex(original) + " New Hex=" + Str.toHex(newVal));
            }
        }
        return ShellUtils.echo(this.getNewValue());
    }

    public boolean hasCommand(String command) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Checking Command: " + command + "   To See if it has a Target Command: " + Str.joinArray(commandLine, " "));

        if(commandLine == null || !isValid) return false;
        if(cleanedCommandLine == null) {
            String[] badChars = new String[] { " ", "<", ">", "|", "~", "-", "-" };
            List<String> cleanerOutput = new ArrayList<>();
            for (int i = 0; i < commandLine.length; i++) {
                String c = commandLine[i].trim();
                for(String bc : badChars) {
                    if(c.contains(bc)) {
                        c = c.replaceAll(bc, "");
                    }
                }

                if(!TextUtils.isEmpty(c)) cleanerOutput.add(c);
            }

            cleanedCommandLine = cleanerOutput.toArray(new String[0]);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Cleaned Command Line= " + Str.joinArray(cleanedCommandLine, " "));
        }

        for(String c : cleanedCommandLine) {
            if(c.contains(command)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Command Is Bad! Original: " + Str.joinArray(cleanedCommandLine, " ") + "   >> Target Command: " + command);
                return true;
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Command is not Target Command, Original: " + Str.joinArray(commandLine, " ") + " >> Target Command: " + command);

        return false;
    }

    public String getCommandOutput() {
        if(originalOutput == null) {
            if(process == null) {
                try { this.process = (Process) param.getResult();
                }catch (Throwable e) { Log.e(TAG, "Failed to Get the Command Execution Result Value! Error: " + e); }
            }
            originalOutput = process == null ? "" : ProcessUtils.getProcessOutput(process);
        }

        return originalOutput;
    }

    private static boolean isEchoCommand(String command) {
        int index = command.indexOf("echo");
        boolean hasEch = index != -1 && index < 14;
        if(DebugUtil.isDebug())
            Log.d(TAG, "Command is a Echo Command ? =" + (hasEch) + " Skipping if true as this May be our Command! >> " + command);
        return hasEch;
    }

    public ShellInterception(XParam param, boolean isProcessBuilder, UserContextMaps maps) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Parsing Command: IsProcessBuilder=" + isProcessBuilder);

        this.mContextMaps = maps;
        this.param = param;
        try {
            if(isProcessBuilder) {
                ProcessBuilder pb = (ProcessBuilder) param.getThis();
                List<String> commands = pb.command();
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Parsing Command Process Builder: Command=" + Str.joinList(commands, " "));

                String[] args = new String[commands.size()];
                for(int i = 0; i < commands.size(); i++)
                    args[i] = commands.get(i);

                if(DebugUtil.isDebug())
                    Log.d(TAG, "Parsing Command Array from Process Builder: Command=" + Str.joinArray(args, " "));

                parseArray(args);
            }else {
                Object paramOne = param.getArgument(0);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Parsing Command: Type Arg One=" + paramOne.getClass().getName());

                if(paramOne instanceof String) {
                    String arg = (String)paramOne;
                    parseString(arg);
                }
                else if(paramOne instanceof String[]) {
                    String[] args = (String[]) paramOne;
                    parseArray(args);
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Error Intercepting Shell Command Arguments: " + e);
            isValid = false;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "[init] Command Is Valid ? " + (isValid) + " Command Line=" + Str.joinArray(this.commandLine, " "));
    }

    private void parseString(String arg) {
        try {
            if(TextUtils.isEmpty(arg) || isEchoCommand(arg)) {
                isValid = false;
                return;
            }

            String[] args = arg.trim().split(" ");
            if(DebugUtil.isDebug())
                Log.d(TAG, "Parsing String Command, Original:" + arg + " Split: " + Str.joinArray(args, " "));

            List<String> parts = new ArrayList<>();
            for(String s : args) {
                if(TextUtils.isEmpty(s) || s.equals(" ")) continue;
                String c = s.trim();
                if(TextUtils.isEmpty(c)) continue;
                if(c.contains(" ")) {
                    //Split again ? damn
                    String[] subParts = c.split(" ");
                    for (String ss : subParts) {
                        String cc = ss.trim();
                        if(TextUtils.isEmpty(cc) || cc.equals(" ")) continue;
                        parts.add(cc.toLowerCase());
                    }
                } else {
                    parts.add(c.toLowerCase());
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished Cleaning String Command, Original:" + arg + " Cleaned: " + Str.joinList(parts, " "));

            this.commandLine = parts.toArray(new String[0]);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Parse Command String: Error: " + e);
            isValid = false;
        }
    }

    private void parseArray(String[] args) {
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Parsing Command Line Array! Command=" + Str.joinArray(args, " "));

            if(args == null || args.length == 0) {
                isValid = false;
                return;
            }
            int min = Math.min(args.length, 3);
            //Small check for echo
            for(int i = 0; i < min; i++) {
                String p = args[i];
                String t = p.trim();
                if(t.startsWith("echo")) {
                    isValid = false;
                    return;
                }
            }

            List<String> parts = new ArrayList<>();
            for(int i = 0; i < args.length; i++) {
                String p = args[i];
                String t = p.trim();
                if(TextUtils.isEmpty(t) || t.equals(" ")) continue;
                if(t.contains(" ")) {
                    String[] subParts = t.split(" ");
                    for(int j = 0; j < subParts.length; j++) {
                        String pp = subParts[j];
                        String tt = pp.trim();
                        if(TextUtils.isEmpty(tt) || tt.equals(" ")) continue;
                        parts.add(tt.toLowerCase());
                    }
                } else {
                    parts.add(t.toLowerCase());
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Command Line has Been Cleaned Part one! Command=" + Str.joinList(parts, " "));

            this.commandLine = parts.toArray(new String[0]);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Parse Command String Array: Error: " + e);
            isValid = false;
        }
    }
}

