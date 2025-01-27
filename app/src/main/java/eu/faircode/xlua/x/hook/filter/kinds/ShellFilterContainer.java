package eu.faircode.xlua.x.hook.filter.kinds;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.interceptors.ShellIntercept;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.IFilterContainer;

public class ShellFilterContainer extends FilterContainerElement implements IFilterContainer {
    public static IFilterContainer create() { return new ShellFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.Shell";
    public static final TypeMap DEFINITIONS = TypeMap.create().add(Runtime.class, "exec").add(ProcessBuilder.class, "start");

    public ShellFilterContainer() { super(GROUP_NAME, DEFINITIONS); }

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            String method = hook.getMethodName();
            for(CommandInterceptor interceptor : ShellIntercept.getInterceptors()) {
                if(interceptor.getCommand().toLowerCase().endsWith(method.toLowerCase())) {
                    settings.put(interceptor.getSetting(), "true");
                    break;
                }
            }
        }

        return isRule;
    }
}
