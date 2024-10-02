package eu.faircode.xlua.interceptors.shell.handlers;

import android.util.Log;

import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterceptionResult;
import eu.faircode.xlua.utilities.MemoryUtil;

public class StatIntercept extends CommandInterceptor implements ICommandIntercept {
    private static final String TAG = "XLua.StatIntercept";


    private static final String STAT_INTERCEPT_SETTING = "intercept.shell.stat.bool";

    @SuppressWarnings("unused")
    public StatIntercept() { this.command = "stat"; }

    @Override
    public boolean interceptCommand(ShellInterceptionResult result) {
        if(result != null && result.isValueValid()) {
            UserContextMaps maps = result.getUserMaps();
            if(maps != null) {
                if(!keepGoing(maps, STAT_INTERCEPT_SETTING)) return true;



                //int total = maps.getSettingInteger(MEMORY_TOTAL_SETTING, 100);
                //int available = maps.getSettingInteger(MEMORY_AVAILABLE_SETTING, 80);
                //if(total < available) total = available;

                //String fakeMemory = MemoryUtil.generateFakeMeminfoContents(total, available);
                //Log.d(TAG, "Generated Fake Memory Info map:\n" + fakeMemory);



                //result.setNewValue(fakeMemory);
                //result.setIsMalicious(true);
                return true;
            }
        } return false;
    }
}
