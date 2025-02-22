package eu.faircode.xlua.x.xlua.commands.query;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.hook.AssignmentApi;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

public class GetAssignedHooksLegacyCommand extends QueryCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(GetAssignedHooksLegacyCommand.class);

    public GetAssignedHooksLegacyCommand() { this.name = "getAssignedHooks"; this.requiresPermissionCheck = true; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        MatrixCursor result = new MatrixCursor(new String[]{marshall ? "blob" : "json", "used"});
        if(DebugUtil.isDebug())
            Log.d(TAG, "Get Assignments Legacy Command Invoked!");

        /*Make this more legacy*/
        SQLDatabase db = commandData.getDatabase();
        List<String> collection = db.executeWithWriteLock(() -> SettingsApi.getCollectionsValue(db, commandData.getUserId(true)));
        Collection<AssignmentPacket> assignments = db.executeWithReadLock(() -> AssignmentApi.getAssignments(db, commandData.getUserId(true), commandData.getCategory(true)));
        if (DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Got Assigned Hooks Legacy, Count=%s Collection Count=%s UserInfo=%s", ListUtil.size(assignments), Str.joinList(collection), Str.noNL(Str.toStringOrNull(commandData.getUserIdentification(true)))));

        for (AssignmentPacket assignment : assignments)
            UberCore888.writeHookFromCache(
                    result,
                    assignment.hook,
                    String.valueOf(assignment.used),
                    assignment.getCategory(),
                    collection,
                    marshall);

        return result;
    }
}