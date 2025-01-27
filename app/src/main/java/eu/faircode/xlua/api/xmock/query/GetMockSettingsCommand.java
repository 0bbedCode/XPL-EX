package eu.faircode.xlua.api.xmock.query;

import android.content.Context;
import android.database.Cursor;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.api.xmock.database.LuaSettingsManager;
import eu.faircode.xlua.api.xstandard.QueryCommandHandler;
import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.api.xstandard.command.QueryPacket_old;
import eu.faircode.xlua.utilities.CursorUtil;

public class GetMockSettingsCommand extends QueryCommandHandler {

    @SuppressWarnings("unused")
    public GetMockSettingsCommand() { this(false); }
    public GetMockSettingsCommand(boolean marshall) {
        this.marshall = marshall;
        this.name = marshall ? "getMockSettings2" : "getMockSettings";
        this.requiresPermissionCheck = false;
    }

    @Override
    public Cursor handle(QueryPacket_old commandData) throws Throwable {
        LuaSettingPacket packet = commandData.readFullPacketFrom(LuaSettingPacket.class, UserIdentityPacket.USER_QUERY_PACKET_ONE);
        if(packet == null) return null;
        packet.resolveUserID();
        packet.ensureCode(LuaSettingPacket.CODE_GET_MODIFIED);
        switch (packet.getCode()) {
            case LuaSettingPacket.CODE_GET_ALL:
                return CursorUtil.toMatrixCursor(LuaSettingsManager.getAllSettings(
                        commandData.getContext(),
                        commandData.getDatabase(), packet), marshall, 0);
            case LuaSettingPacket.CODE_GET_MODIFIED:
                return CursorUtil.toMatrixCursor(LuaSettingsManager.getSettings(
                        commandData.getDatabase(),
                        packet), marshall, 0);
        } return null;
    }

    public static Cursor invoke(Context context, boolean marshall, LuaSettingPacket packet) {
        return XProxyContent.luaQuery(
                context,
                marshall ? "getMockSettings2" : "getMockSettings",
                packet.generateSelectionArgsQuery(UserIdentityPacket.USER_QUERY_PACKET_ONE));
    }
}