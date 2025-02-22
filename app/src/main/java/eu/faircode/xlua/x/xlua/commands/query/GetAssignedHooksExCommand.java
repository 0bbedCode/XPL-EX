package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.XLuaHookConversions;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.hook.AppProviderUtils;
import eu.faircode.xlua.x.xlua.hook.AssignmentApi;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

/*
    Remove the Unused Constructors
 */
public class GetAssignedHooksExCommand extends QueryCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(GetAssignedHooksExCommand.class);

    public GetAssignedHooksExCommand() { this.name = "getExAssignedHooks"; this.requiresPermissionCheck = false; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        MatrixCursor result = new MatrixCursor(new String[]{marshall ? "blob" : "json", "used"});

        SQLDatabase db = commandData.getDatabase();
        List<String> collections = db.executeWithWriteLock(() -> SettingsApi.getCollectionsValue(
                db,
                commandData.getUserId()));

        List<AssignmentPacket> assignments = db.executeWithReadLock(() -> AssignmentApi.getAssignments(
                db,
                commandData.getUserId(),
                commandData.getCategory()));

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Got Assignments, UserId=%s UID=%s Category=%s Size=%s", commandData.getUserId(), commandData.getUid(), commandData.getCategory(), ListUtil.size(assignments)));

        for(AssignmentPacket assignment : AppProviderUtils.filterAssignments(assignments, false, false))
            UberCore888.writeHookFromCache(
                    result,
                    assignment.hook,
                    String.valueOf(assignment.used),
                    assignment.getCategory(),
                    collections,
                    marshall);

        return result;
    }


    /*
     @Override
    public Cursor handle(QueryPacket_old commandData) throws Throwable {
        String[] selection = commandData.getSelection();
        boolean all = (selection != null && selection.length == 1 && "all".equals(selection[0]));
        return CursorUtil.toMatrixCursor(
                UberCore888.getHooks(commandData.getContext(), commandData.getDatabase(), all),
                marshall,
                XLuaHook.FLAG_WITH_LUA);
    }

    public static Cursor invoke(Context context, boolean marshall) {
        return XProxyContent.luaQuery(
                context,
                marshall ? "getHooks2" : "getHooks");
    }

    public static Cursor invoke(Context context, boolean marshall, boolean all) {
        return XProxyContent.luaQuery(
                context,
                marshall ? "getHooks2" : "getHooks", new String[] { all ? "all" : "some" });
    }
     */


    public static Collection<XLuaHook> get(Context context, boolean marshall, int uid, String packageName) {
        return XLuaHookConversions.fromCursor(
                XProxyContent.luaQuery(
                        context,
                        marshall ? "getExAssignedHooks2" : "getExAssignedHooks",
                        UserIdentity.createSnakeQueryUID(uid, packageName)), marshall, true);
    }
}
