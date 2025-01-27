package eu.faircode.xlua.interceptors.shell.handlers;

import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.utilities.MemoryUtil;

public class MemInfoIntercept extends CommandInterceptor implements ICommandIntercept {
    private static final String MEMORY_TOTAL_SETTING = "hardware.memory.total";
    private static final String MEMORY_AVAILABLE_SETTING = "hardware.memory.available";
    private static final String MEMORY_INTERCEPT_SETTING = "intercept.shell.meminfo.bool";

    @SuppressWarnings("unused")
    public MemInfoIntercept() { this.command = "meminfo"; this.setting = MEMORY_INTERCEPT_SETTING; }


    @Override
    public boolean isCommand(ShellInterception results) {
        for(String com : results.commandLine)
            if(com.toLowerCase().contains("meminfo"))
                return true;

        return false;
    }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        if(result != null && result.isValid) {
            UserContextMaps maps = result.getUserMaps();
            if(maps != null) {
                if(!keepGoing(maps, MEMORY_INTERCEPT_SETTING)) return true;

                int total = maps.getSettingInteger(MEMORY_TOTAL_SETTING, 100);
                int available = maps.getSettingInteger(MEMORY_AVAILABLE_SETTING, 80);
                if(total < available) total = available;

                String fakeMemory = MemoryUtil.generateFakeMeminfoContents(total, available);
                result.setNewValue(fakeMemory);
                result.setIsMalicious(true);
                return true;
            }
        }
        return false;
    }
}
