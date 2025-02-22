package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.util.List;

import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;

public class GetHookCommand extends CallCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(GetHooksCommand.class);

    public static final String FIELD = "hook";
    public static final String COMMAND_NAME = "getHook";

    public GetHookCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        this.requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        return BundleUtil.createSingleString(
                FIELD,
                JSONUtil.objectToString(
                        JSONUtil.toObject(
                                UberCore888.getHookEx(commandData.getExtraString(FIELD)))));
    }


    public static XLuaHook get(Context context, String hookId) {
        if(Str.isEmpty(hookId))
            return null;

        XLuaHook hook = new XLuaHook();
        Bundle res = XProxyContent.luaCall(context, COMMAND_NAME, BundleUtil.createSingleString(FIELD, hookId));
        if(res == null)
            return null;

        String jsonData = res.getString(FIELD);
        if(Str.isEmpty(jsonData))
            return null;

        try {
            hook.fromJSONObject(new JSONObject(jsonData));
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to read back Hook [%s] with Error [%s] JSON Data=%s", hookId, e, Str.ensureNoDoubleNewLines(jsonData)));
            return null;
        }

        return hook;
    }
}