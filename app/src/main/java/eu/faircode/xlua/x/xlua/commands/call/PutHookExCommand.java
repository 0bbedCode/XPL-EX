package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.hook.LuaHookPacket;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xlua.call.PutHookCommand;
import eu.faircode.xlua.api.xlua.provider.XLuaHookProvider;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.hook.AssignmentApi;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.hook.AssignmentsPacket;
//Hook is straight up just ID and DEF
//Couldnt make my life any easier


public class PutHookExCommand extends CallCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(PutHookExCommand.class);

    public static final String COMMAND_NAME = "putHookExCommand";
    public static final String FIELD_DATA = "raw";

    public PutHookExCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        String json = commandData.getExtraString(FIELD_DATA);
        if(json == null || Str.isEmpty(json)) {
            Log.e(TAG, "Failed to put Hook, JSON Data is null or empty!");
            return A_CODE.FAILED.toBundle();
        }

        try {
            //Init Cache ?
            if(!DatabaseHelpEx.ensureTableIsReady_locked(XLuaHook.TABLE_INFO, commandData.getDatabase())) {
                Log.e(TAG, "Failed to Prepare Database! Table=" + XLuaHook.TABLE_NAME);
                return A_CODE.FAILED.toBundle();
            }

            XLuaHook val = new XLuaHook();
            val.fromJSONObject(new JSONObject(json));
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Pushing (%s) Hook: %s",
                        val.getObjectId(),
                        Str.toStringOrNull(Str.ensureNoDoubleNewLines(json))));

            boolean result = DatabaseHelpEx.insertItem(commandData.getDatabase(), XLuaHook.TABLE_NAME, val);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Hook [%s] was Pushed ? (%s)\nHook=%s",
                        val.getObjectId(),
                        result,
                        Str.ensureNoDoubleNewLines(Str.toStringOrNull(val))));

            if(result) {
                UberCore888.updateHookCache(commandData.getContext(), val, val.getObjectId());
            }

            return A_CODE.result(result).toBundle();
        }catch (Exception e) {
            Log.e(TAG, "Error Putting Hooks, Error=" + e);
            return A_CODE.FAILED.toBundle();
        }
    }

    public static A_CODE put(Context context, XLuaHook hook) {
        try {
            Bundle send = new Bundle();
            send.putString(FIELD_DATA, hook.toJSON());
            Bundle result = XProxyContent.luaCall(context, COMMAND_NAME, send);
            return A_CODE.fromBundle(result);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Put, Error=" + e);
            return A_CODE.FAILED;
        }
    }
}