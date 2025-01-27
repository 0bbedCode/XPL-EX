package eu.faircode.xlua.api.xmock.query;

import android.content.Context;
import android.database.Cursor;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.properties.MockPropPacket;
import eu.faircode.xlua.api.xmock.provider.MockPropProvider;
import eu.faircode.xlua.api.xstandard.QueryCommandHandler;
import eu.faircode.xlua.api.xstandard.command.QueryPacket_old;
import eu.faircode.xlua.utilities.CursorUtil;

public class GetMockPropertiesCommand extends QueryCommandHandler {
    public static GetMockPropertiesCommand create(boolean marshall) { return new GetMockPropertiesCommand(marshall); }

    @SuppressWarnings("unused")
    public GetMockPropertiesCommand() { this(false); }
    public GetMockPropertiesCommand(boolean marshall) {
        this.marshall = marshall;
        this.name = marshall ? "getModifiedProperties2" : "getModifiedProperties";
        this.requiresPermissionCheck = false;
    }

    @Override
    public Cursor handle(QueryPacket_old commandData) throws Throwable {
        MockPropPacket packet = commandData.readFullPacketFrom(MockPropPacket.class, MockPropPacket.USER_QUERY_PACKET_ONE);
        if(packet == null) return null;
        packet.resolveUserID();
        return CursorUtil.toMatrixCursor(
                MockPropProvider.getSettingsForPackage(
                        commandData.getContext(), commandData.getDatabase(), packet), marshall, 0);
    }

    public static Cursor invoke(Context context, boolean marshall, MockPropPacket packet) {
        return XProxyContent.mockQuery(
                context,
                marshall ? "getModifiedProperties2" : "getModifiedProperties",
                packet.generateSelectionArgsQuery(MockPropPacket.USER_QUERY_PACKET_ONE));
    }
}
