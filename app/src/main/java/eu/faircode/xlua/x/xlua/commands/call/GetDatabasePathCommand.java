package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;

public class GetDatabasePathCommand extends CallCommandHandlerEx {
    public static final String FIELD_GROUP = "path";

    private static final String TAG = LibUtil.generateTag(GetDatabasePathCommand.class);

    public GetDatabasePathCommand() {
        name = "getDatabasePath";
        requiresPermissionCheck = true;
        this.requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        String path = commandData.getDatabase().getDirectory();
        if(DebugUtil.isDebug())
            Log.d(TAG, " Get Database path Command= " + path);

        return BundleUtil.createSingleString(FIELD_GROUP, path);
    }

    public static String get(Context context) {
        return BundleUtil
                .readString(
                        XProxyContent.luaCall(context, "getDatabasePath"), FIELD_GROUP);
    }
}