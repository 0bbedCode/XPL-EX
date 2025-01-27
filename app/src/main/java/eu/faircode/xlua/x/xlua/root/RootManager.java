package eu.faircode.xlua.x.xlua.root;
//import com.topjohnwu.superuser.Shell;

import android.util.Log;

import com.topjohnwu.superuser.Shell;

import eu.faircode.xlua.DebugUtil;

public class RootManager {
    private static final String TAG = "XLua.RootManager";
    public static RootManager instance;

    public boolean hasRootAccess = false;

    public RootManager() {
        instance = this;
        Shell.enableVerboseLogging = true;
        Shell.setDefaultBuilder(
                Shell.Builder.create()
                        .setFlags(Shell.FLAG_REDIRECT_STDERR)
                        .setTimeout(10));
    }

    public void requestRoot() {
        //This is now crashing ?
        //XLog.i(TAG, "Requesting Root...", true);
        Shell.getShell(shell -> {
            Log.i(TAG, "Root Permission State! " + Shell.isAppGrantedRoot());
            hasRootAccess = Boolean.TRUE.equals(Shell.isAppGrantedRoot());
        });
    }

    /**
     * Copy files or directories from source to destination using root shell.
     *
     * @param fromDirectory The source directory.
     * @param toDirectory   The destination directory.
     */
    /*public void copyFiles(String fromDirectory, String toDirectory) {
        if (DebugUtil.isDebug())
            Log.d(TAG, "Copying Dir From [" + fromDirectory + "] To [" + toDirectory + "]");

        requestRoot();
        boolean isRoot2 = Shell.getShell().isRoot();
        if (DebugUtil.isDebug())
            Log.d(TAG, "Is Root=" + hasRootAccess + "  Is Root 2=" + isRoot2);

        // Ensure root access before executing shell commands
        if (!hasRootAccess && !isRoot2) {
            Log.e(TAG, "Cannot copy files: No root access!");
            return;
        }

        try {
            // Validate source directory

            //Shell.Result checkSourceResult = Shell.cmd("[ -d \"" + fromDirectory + "\" ] && echo \"Exists\" || echo \"NotExists\"").exec();
            //if (!checkSourceResult.getOut().contains("Exists")) {
            //    Log.e(TAG, "Source directory does not exist or is inaccessible: " + fromDirectory);
            //    return;
            //}

            Shell.cmd("setenforce 0").exec();


            // Validate destination directory or create it
            Shell.Result checkDestResult = Shell.cmd("[ -d \"" + toDirectory + "\" ] && echo \"Exists\" || echo \"NotExists\"").exec();
            if (!checkDestResult.getOut().contains("Exists")) {
                Log.w(TAG, "Destination directory does not exist. Attempting to create: " + toDirectory);
                Shell.Result createDestResult = Shell.cmd("mkdir -p \"" + toDirectory + "\"").exec();
                if (createDestResult.getCode() != 0) {
                    Log.e(TAG, "Failed to create destination directory: " + toDirectory);
                    Log.e(TAG, "Error: " + String.join("\n", createDestResult.getErr()));
                    return;
                }
            }

            // Execute copy command
            Shell.getShell(shell -> {
                Log.i(TAG, "Executing root shell commands to copy files...");

                // Use `cp -r` to copy recursively
                //String command = "cp -r \"" + fromDirectory + "\" \"" + toDirectory + "\"";
                String command = "cp -r " + fromDirectory + " " + toDirectory;
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Command: " + command);

                // Execute the command
                Shell.Result result = shell.newJob()
                        .add(command) // Add the command to the shell job
                        .exec(); // Execute the job

                // Handle the command output
                if (result.getCode() == 0) { // Exit code 0 means success
                    Log.i(TAG, "Copy command executed successfully!");
                    if (!result.getOut().isEmpty()) {
                        Log.i(TAG, "Output: " + String.join("\n", result.getOut()));
                    }
                } else {
                    Log.e(TAG, "Copy command failed with exit code: " + result.getCode());
                    if (!result.getErr().isEmpty()) {
                        Log.e(TAG, "Error: " + String.join("\n", result.getErr()));
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Copy Files Root Failed: " + e.getMessage(), e);
        }finally {
            Shell.cmd("setenforce 1").exec();
        }
    }*/

    public void copyFiles(String fromDirectory, String toDirectory) {
        Log.d(TAG, "Copying Dir From [" + fromDirectory + "] To [" + toDirectory + "]");

        try {
            Shell.Result whoamiResult = Shell.cmd("whoami").exec();
            Log.d(TAG, "Current User: " + String.join("\n", whoamiResult.getOut()));

            // Set SELinux to permissive mode
            Shell.cmd("setenforce 0").exec();

            Shell.cmd("mkdir -p \"" + toDirectory + "\"").exec();


            // Build the copy command
            //String command = "cp -r " + fromDirectory + " " + toDirectory;
            String command = "su -c 'cp -r " + fromDirectory + " " + toDirectory + "'";

            Log.d(TAG, "Executing Command: " + command);

            // Execute the copy command
            Shell.Result result = Shell.cmd(command).exec();

            if (result.getCode() == 0) {
                Log.i(TAG, "Copy command executed successfully!");
            } else {
                Log.e(TAG, "Copy command failed with exit code: " + result.getCode());
                Log.e(TAG, "Error Output: " + String.join("\n", result.getErr()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Copy Files Root Failed: " + e.getMessage(), e);
        } finally {
            // Restore SELinux enforcing mode
            Shell.cmd("setenforce 1").exec();
        }
    }
}
