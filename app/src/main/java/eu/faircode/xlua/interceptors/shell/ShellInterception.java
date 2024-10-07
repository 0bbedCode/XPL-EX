package eu.faircode.xlua.interceptors.shell;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.Str;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.util.CommandOutputHelper;
import eu.faircode.xlua.utilities.ShellUtils;


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

    public String getNewValue() { return mNewValue; }
    public boolean isMalicious() { return mIsMalicious; }
    public void setIsMalicious(boolean isMalicious) { this.mIsMalicious = isMalicious; }
    public void setNewValue(String newValue) { this.mNewValue = newValue; }
    public UserContextMaps getUserMaps() { return mContextMaps; }
    public String getCommandLine() { return Str.joinArray(commandLine, " "); }


    @SuppressWarnings("unused")
    public Process getEchoProcess() { return ShellUtils.echo(this.getNewValue()); }

    public boolean hasCommand(String command) {
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
        }

        for(String c : cleanedCommandLine) {
            if(c.equalsIgnoreCase(command)) {
                if(BuildConfig.DEBUG)
                    Log.i(TAG, "BAD COMMAND => " + command);
                return true;
            }
        }

        return false;
    }

    public String getCommandOutput() {
        if(process == null) {
            try {
                this.process = (Process) param.getResult();
            }catch (Throwable e) {
                Log.e(TAG, "Failed to Get the Command Execution Result Value! Error: " + e);
            }
        }
        return process == null ? "" : CommandOutputHelper.readProcessOutput(process);
    }

    private static boolean isEchoCommand(String command) {
        int index = command.indexOf("echo");
        return index != -1 && index < 10;
    }

    public ShellInterception(XParam param, boolean isProcessBuilder, UserContextMaps maps) {
        this.mContextMaps = maps;
        this.param = param;
        try {
            if(isProcessBuilder) {
                ProcessBuilder pb = (ProcessBuilder) param.getThis();
                List<String> commands = pb.command();
                String[] args = new String[commands.size()];
                for(int i = 0; i < commands.size(); i++)
                    args[i] = commands.get(i);

                parseArray(args);
            }else {
                Object paramOne = param.getArgument(0);
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
    }

    private void parseString(String arg) {
        try {
            if(TextUtils.isEmpty(arg) || isEchoCommand(arg)) {
                isValid = false;
                return;
            }

            String[] args = arg.trim().split(" ");
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

            this.commandLine = parts.toArray(new String[0]);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Parse Command String: Error: " + e);
            isValid = false;
        }
    }

    private void parseArray(String[] args) {
        try {
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

            this.commandLine = parts.toArray(new String[0]);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Parse Command String Array: Error: " + e);
            isValid = false;
        }
    }
}

