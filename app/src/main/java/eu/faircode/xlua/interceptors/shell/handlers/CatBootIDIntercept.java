package eu.faircode.xlua.interceptors.shell.handlers;

import java.util.UUID;

import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.utilities.MemoryUtil;

public class CatBootIDIntercept extends CommandInterceptor implements ICommandIntercept {
    private static final String CAT_BOOT_ID_INTERCEPT_SETTING = "intercept.shell.boot_id.bool";

    @SuppressWarnings("unused")
    public CatBootIDIntercept() { this.command = "boot_id"; this.setting = CAT_BOOT_ID_INTERCEPT_SETTING; }
    ///proc/sys/kernel/random/boot_id

    @Override
    public boolean isCommand(ShellInterception results) {
        for(String command : results.commandLine)
            if(command.toLowerCase().contains("boot_id"))
                return true;

        return false;
    }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        if (result != null && result.isValid) {
            UserContextMaps maps = result.getUserMaps();
            if (maps != null) {
                //Improve this ? make some storage emulation shit ?
                //ashmem device etc stat
                if (!keepGoing(maps, CAT_BOOT_ID_INTERCEPT_SETTING))
                    return true;
                String setting = maps.getSetting("unique.boot.id");
                String newValue = setting == null ? UUID.randomUUID().toString() : setting;

                result.setNewValue(newValue);
                result.setIsMalicious(true);

                result.param.setOldResult("N/A");
                result.param.setNewResult(newValue);
                result.param.setSettingResult("cat boot_id");
                return true;
            }
        }
        return false;
    }
}