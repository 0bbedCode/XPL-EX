package eu.faircode.xlua.x.hook.interceptors.ipc.bases;

import android.text.TextUtils;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.x.hook.interceptors.ipc.holders.InterfaceBinderData;

public class BinderInterceptorBase implements IBinderInterceptor {
    private static final String TAG = "XLua.BinderInterceptorBase";
    protected String interfaceName;
    protected String settingName;

    public static BinderInterceptorBase create(String interfaceName, String settingName) {
        BinderInterceptorBase b = new BinderInterceptorBase();
        b.interfaceName = interfaceName;
        b.settingName = settingName;
        return b;
    }

    @Override
    public boolean intercept(XParam param, InterfaceBinderData helper) {
        try {
            helper.readReplyException();
            String oldId = helper.readReplyString();
            if(!TextUtils.isEmpty(oldId) && oldId.length() > 4) {
                String newId = param.getSetting("unique.google.advertising.id");    //Use this for now
                if(!TextUtils.isEmpty(newId)) {
                    if(DebugUtil.isDebug())
                        Log.w(TAG, "Is Target Interface [" + helper.code + "] Reply Size: " + helper.getReplySize() + " Code=" + helper.code + " Old=" + oldId + " New=" + newId);

                    //Have settings Control disable enable like shell for IPC
                    helper.resetParcelPositions();
                    helper.replaceReplyString(oldId, newId);

                    param.setOldResult(oldId);
                    param.setNewResult(newId);
                    param.setSettingResult(helper.interfaceName);
                    return true;
                }
            }
        }finally {
            helper.resetParcelPositions();
        }

        return false;
    }

    @Override
    public String getInterfaceName() { return interfaceName; }

    @Override
    public String getSettingName() { return settingName; }

    @Override
    public boolean isEnabled(XParam param) { return StringUtil.toBoolean(param.getSetting(settingName, "false"), false); }
}
