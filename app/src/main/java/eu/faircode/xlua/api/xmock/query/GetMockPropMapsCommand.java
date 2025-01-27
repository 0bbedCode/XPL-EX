package eu.faircode.xlua.api.xmock.query;

import android.content.Context;
import android.database.Cursor;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xmock.provider.MockPropProvider;
import eu.faircode.xlua.api.xstandard.QueryCommandHandler;
import eu.faircode.xlua.api.xstandard.command.QueryPacket_old;
import eu.faircode.xlua.utilities.CursorUtil;

public class GetMockPropMapsCommand extends QueryCommandHandler {
    @SuppressWarnings("unused")
    public GetMockPropMapsCommand() { this(false); }
    public GetMockPropMapsCommand(boolean marshall) {
        this.marshall = marshall;
        this.name = marshall ? "getMockPropMaps2" : "getMockPropMaps";
        this.requiresPermissionCheck = false;
    }

    @Override
    public Cursor handle(QueryPacket_old commandData) throws Throwable {
        return CursorUtil.toMatrixCursor(
                MockPropProvider.getMockPropMaps(
                        commandData.getContext(),
                        commandData.getDatabase()), marshall, 0);
    }

    public static Cursor invoke(Context context, boolean marshall) {
        return XProxyContent.mockQuery(
                context,
                marshall ? "getMockPropMaps2" : "getMockPropMaps");
    }
}
