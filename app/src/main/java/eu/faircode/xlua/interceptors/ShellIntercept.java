package eu.faircode.xlua.interceptors;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.handlers.CatBootIDIntercept;
import eu.faircode.xlua.interceptors.shell.handlers.GetPropIntercept;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.interceptors.shell.handlers.LSIntercept;
import eu.faircode.xlua.interceptors.shell.handlers.LogcatIntercept;
import eu.faircode.xlua.interceptors.shell.handlers.MemInfoIntercept;
import eu.faircode.xlua.interceptors.shell.handlers.StatIntercept;
import eu.faircode.xlua.interceptors.shell.handlers.SuIntercept;
import eu.faircode.xlua.interceptors.shell.handlers.UnameIntercept;

public class ShellIntercept {
    private static final String TAG = "XLua.ShellIntercept";
    private static final List<CommandInterceptor> interceptors = new ArrayList<>();

    public static ShellInterception intercept(ShellInterception results) {
        try {
            if(results.isValid) {
                if(BuildConfig.DEBUG)
                    Log.i(TAG, "Checking command: " + results.getCommandLine());

                for(CommandInterceptor interceptor : getInterceptors()) {
                    if(results.hasCommand(interceptor.getCommand())) {
                        if(interceptor.interceptCommand(results)) {
                            if(BuildConfig.DEBUG)
                                Log.w(TAG, "Malicious command! " + results.getCommandLine());

                            return results;
                        }
                    }
                }
            }
        }catch (Exception e) { Log.e(TAG, "Error with Interceptor Core: e=" + e); }
        return results;
    }

    public static List<CommandInterceptor> getInterceptors() {
        if(interceptors.isEmpty()) {
            interceptors.add(new GetPropIntercept());
            interceptors.add(new MemInfoIntercept());
            //interceptors.add(new SuIntercept());
            interceptors.add(new UnameIntercept());
            interceptors.add(new LogcatIntercept());
            interceptors.add(new StatIntercept());
            interceptors.add(new LSIntercept());
            interceptors.add(new CatBootIDIntercept());
        }

        return interceptors;
    }
}
