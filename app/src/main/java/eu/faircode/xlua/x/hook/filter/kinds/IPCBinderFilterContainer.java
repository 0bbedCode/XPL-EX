package eu.faircode.xlua.x.hook.filter.kinds;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.IFilterContainer;
import eu.faircode.xlua.x.hook.interceptors.ipc.InterfacesGlobal;
import eu.faircode.xlua.x.hook.interceptors.ipc.bases.IBinderInterceptor;

public class IPCBinderFilterContainer extends FilterContainerElement implements IFilterContainer {
    public static IFilterContainer create() { return new IPCBinderFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.IPC.Interface";
    public static final TypeMap DEFINITIONS =
            TypeMap.create()
            .add("android.os.Binder", "transact", "onTransact")
            .add("android.os.BinderProxy", "transact", "onTransact");

    public IPCBinderFilterContainer() { super(GROUP_NAME, DEFINITIONS); }

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            String method = hook.getMethodName().trim();
            for(IBinderInterceptor interceptor : InterfacesGlobal.INTERCEPTORS) {
                if(interceptor.getInterfaceName().equalsIgnoreCase(method)) {
                    createdSettings.put(interceptor.getSettingName(), "true");
                    break;
                }
            }
        }
        return isRule;
    }
}