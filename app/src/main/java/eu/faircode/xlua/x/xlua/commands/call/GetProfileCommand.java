package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.configs.AppProfile;
import eu.faircode.xlua.x.xlua.configs.ProfileApi;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;

public class GetProfileCommand extends CallCommandHandlerEx {
    private static final String TAG = "XLua.GetProfileCommand";

    public static final String COMMAND_NAME = "getProfile";

    public GetProfileCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        this.requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        int userId = commandData.getUserId();
        String packageName = commandData.getCategory();
        String name = commandData.getExtraString(AppProfile.FIELD_NAME);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("UserId=%s  Pkg=%s  Name=%s", userId, packageName, name));

        boolean prepRes = DatabaseHelpEx.prepareDatabase(commandData.getDatabase(), AppProfile.TABLE_INFO);
        if(!prepRes) {
            Log.e(TAG, "Failed to Prepare Table: " + Str.toStringOrNull(AppProfile.TABLE_INFO));
            return A_CODE.FAILED.toBundle();
        }

        //ToDO: Lock
        AppProfile profile = ProfileApi.get(commandData.getDatabase(), userId, packageName, name);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("UserId=%s  Pkg=%s  Name=%s Profile==%s", userId, packageName, name, Str.toStringOrNull(profile)));

        return profile.toBundle();
    }

    public static AppProfile get(Context context, int uid, String packageName, String name) {
        Bundle dat = new Bundle();
        UserIdentity userIdentity = UserIdentity.fromUid(uid, packageName);
        UserIdentityIO.toBundleEx(userIdentity, dat);
        dat.putString(AppProfile.FIELD_NAME, name);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Calling [getProfile] uid=%s  Pkg=%s  Name=%s", uid, packageName, name));

        Bundle res = XProxyContent.luaCall(context, COMMAND_NAME, dat);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Called [getProfile] uid=%s  Pkg=%s  Name=%s Bundle=%s", uid, packageName, name, Str.toStringOrNull(res)));

        AppProfile profile = new AppProfile();
        profile.populateFromBundle(res);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Returning result [getProfile] uid=%s  Pkg=%s  Name=%s Result=%s", uid, packageName, name, Str.toStringOrNull(profile)));

        return profile;
    }
}