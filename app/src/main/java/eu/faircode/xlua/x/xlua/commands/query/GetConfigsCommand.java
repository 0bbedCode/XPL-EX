package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;
import android.os.Process;

import java.util.List;
import java.util.Map;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.configs.ConfigApi;
import eu.faircode.xlua.x.xlua.configs.XPConfig;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

public class GetConfigsCommand extends QueryCommandHandlerEx {

    public GetConfigsCommand() { this.name = "getConfigs"; this.requiresPermissionCheck = true; this.requiresSingleThread = true; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        return CursorUtil.toMatrixCursor_final(ConfigApi.getConfigs(
                commandData.getDatabase(),
                commandData.getUserId()), marshall, 0);
    }

    public static List<XPConfig> get(Context context) { return get(context, false, Process.myUid()); }
    public static List<XPConfig> get(Context context, boolean marshall, int uid) {
        return ListUtil.copyToArrayList(
                CursorUtil.readCursorAs_final(
                        XProxyContent.luaQuery(context, marshall ? "getConfigs2" : "getConfigs", SQLSnake.create().whereColumn("uid", uid).asSnake()),
                        marshall, XPConfig.class));
    }
}
