package eu.faircode.xlua.interceptors.shell.handlers;

import java.util.UUID;

import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.x.hook.interceptors.file.FileInterceptor;

public class CatIDIntercept extends CommandInterceptor implements ICommandIntercept {
    private static final String CAT_SERIAL_ID_SETTING = "intercept.shell.serial.bool";

    @SuppressWarnings("unused")
    public CatIDIntercept() {
        this.command = "serial";
        this.setting = CAT_SERIAL_ID_SETTING;
    }

    @Override
    public boolean isCommand(ShellInterception results) {
        FileInterceptor.ensureInit(results.param);
        boolean foundCat = false;
        for (String command : results.commandLine) {
            if(!foundCat) {
                if (command.equalsIgnoreCase("cat"))
                    foundCat = true;
            } else {
                if(command.contains("/") && !FileInterceptor.isAllowed(command)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        if (result != null && result.isValid) {
            boolean foundCat = false;
            for (String command : result.commandLine) {
                if(!foundCat) {
                    if (command.equalsIgnoreCase("cat"))
                        foundCat = true;
                } else {
                    if(command.contains("/") && !FileInterceptor.isAllowed(command)) {
                        result.setIsMalicious(true);
                        result.setNewValue("cat: " + command + ": No such file or directory");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}