package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.configs.AppProfile;
import eu.faircode.xlua.x.xlua.configs.ProfileApi;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;

public class GetProfileListCommand extends CallCommandHandlerEx {
    private static final String TAG = "XLua.GetProfileListCommand";

    public static final String COMMAND_NAME = "getProfileList";
    public static final String FIELD_PROFILES = "profiles";
    public GetProfileListCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        this.requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        int userId = commandData.getUserId();
        String packageName = commandData.getCategory();
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("UserId=%s  Pkg=%s", userId, packageName));

        boolean prepRes = DatabaseHelpEx.prepareDatabase(commandData.getDatabase(), AppProfile.TABLE_INFO);
        if(!prepRes) {
            Log.e(TAG, "Failed to Prepare Table: " + Str.toStringOrNull(AppProfile.TABLE_INFO));
            return A_CODE.FAILED.toBundle();
        }

        //ToDO: lock
        Collection<AppProfile> profiles = ProfileApi.getProfiles(commandData.getDatabase(), userId, packageName);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("UserId=%s  Pkg=%s  Profiles Count=%s", userId, packageName, profiles.size()));

        List<String> strList = new ArrayList<>();
        for(AppProfile appProfile : profiles)
            if(!strList.contains(appProfile.name))
                strList.add(appProfile.name);

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Sending UserId=%s  Pkg=%s Profiles Count=%s Profile List=%s", userId, packageName, profiles.size(), Str.joinList(strList)));

        Bundle res = new Bundle();
        res.putStringArrayList(FIELD_PROFILES, new ArrayList<>(strList));
        if(DebugUtil.isDebug())
            Log.d(TAG, "Sending back Bundle! " + Str.toStringOrNull(res));

        return res;
    }

    public static List<String> get(Context context, int uid, String packageName) {
        Bundle dat = new Bundle();
        UserIdentity userIdentity = UserIdentity.fromUid(uid, packageName);
        UserIdentityIO.toBundleEx(userIdentity, dat);   //CHECK CHECK
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Calling [getProfileList] uid=%s  Pkg=%s", uid, packageName));

        Bundle res = XProxyContent.luaCall(context, COMMAND_NAME, dat);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Called [getProfileList] uid=%s  Pkg=%s Bundle=%s", uid, packageName, Str.toStringOrNull(res)));

        List<String> list = res.getStringArrayList(FIELD_PROFILES);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Returning result [getProfileList] uid=%s  Pkg=%s Result=%s  Count=%s", uid, packageName, Str.joinList(list), ListUtil.size(list)));

        return list;
    }
}