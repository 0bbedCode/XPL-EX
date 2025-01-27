package eu.faircode.xlua.x.hook.interceptors.ipc;

import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.hook.interceptors.ipc.bases.IBinderInterceptor;
import eu.faircode.xlua.x.hook.interceptors.ipc.holders.InterfaceBinderData;

public class BinderInterceptor {
    private static final String TAG = "XLua.BinderInterceptor";

    public static boolean intercept(XParam param, boolean getResult) {
        InterfaceBinderData helper = InterfaceBinderData.create(param, getResult);
        if(!helper.hasInterfaceName()) {
            if(DebugUtil.isDebug())
                Log.w(TAG, "Interface Name is NULL for IPC Call Returning...");

            return false;
        }

        if(!helper.hasReply()) {
            if(DebugUtil.isDebug())
                Log.w(TAG, "Reply Parcel for IPC Call is Not Valid Size=" + helper.getReplySize());

            return false;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Checking Interface IPC Call => " + helper.interfaceName);

        for(IBinderInterceptor i : InterfacesGlobal.INTERCEPTORS) {
            if(helper.isInterfaceName(i.getInterfaceName())) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Found Interface => " + i.getInterfaceName());

                return i.intercept(param, helper);
            }
        }

        return false;
    }
}
