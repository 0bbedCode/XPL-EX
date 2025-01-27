package eu.faircode.xlua.api.xlua.query;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.settings.LuaSetting;
import eu.faircode.xlua.api.xstandard.QueryCommandHandler;
import eu.faircode.xlua.api.xstandard.command.QueryPacket_old;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;


public class GetSettingsCommand extends QueryCommandHandler {
    public static GetSettingsCommand create() { return new GetSettingsCommand(); };
    public GetSettingsCommand() {
        name = "getSettings";
        requiresPermissionCheck = false;
    }

    @Override
    public Cursor handle(QueryPacket_old commandData) throws Throwable {
        MatrixCursor result = new MatrixCursor(new String[]{"name", "value"});
        String[] selection = commandData.getSelection();
        XDatabaseOld db = commandData.getDatabase();
        if(selection == null || selection.length == 0)
            return null;

        String packageName = selection[0];
        int userid = 0;
        if(selection.length > 1) {
            int uid = Integer.parseInt(selection[1]);
            userid = XUtil.getUserId(uid);
        }

        SqlQuerySnake snake = SqlQuerySnake
                .create(db, LuaSetting.Table.NAME)
                .whereColumn("user", Integer.toString(userid))
                .whereColumn("category", packageName)
                .onlyReturnColumns("name", "value");

        db.readLock();
        Cursor c = snake.query();
        try {
            while (c.moveToNext())
                result.addRow(new String[]{c.getString(0), c.getString(1)});
        }finally {
            snake.clean(c);
            db.readUnlock();
        }

        return result;
    }

    public static Cursor invoke(Context context, String packageOrName, int uid) {
        SqlQuerySnake snake = SqlQuerySnake
                .create()
                .whereColumn("pkg", packageOrName)
                .whereColumn("uid", uid);

        return XProxyContent.luaQuery(
                context,
                "getSettings",
                snake.getSelectionCompareValues(),
                snake.getSelectionArgs());
    }
}
