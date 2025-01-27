package eu.faircode.xlua.x.hook.interceptors.ipc.bases;

import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.hook.interceptors.ipc.holders.InterfaceBinderData;

public interface IBinderInterceptor {
    String getInterfaceName();
    String getSettingName();
    boolean isEnabled(XParam param);
    boolean intercept(XParam param, InterfaceBinderData helper);
}
