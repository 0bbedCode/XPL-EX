package eu.faircode.xlua.api.xmock.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.xmock.database.MockPropManager;
import eu.faircode.xlua.api.properties.MockPropPacket;
import eu.faircode.xlua.api.xmock.provider.MockPropProvider;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;

public class PutMockPropCommand extends CallCommandHandler {
    //This will register with the LUA Database not the MOCK Database
    public PutMockPropCommand() {
        name = "putMockProp";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        MockPropPacket packet = commandData.readExtrasAs(MockPropPacket.class);
        if(packet == null) return XResult.fromInvalidPacket(name, MockPropPacket.class).toBundle();

        packet.resolveUserID();
        packet.ensureCode(UserIdentityPacket.CODE_NULL_EMPTY);
        Log.i("XLua.PutMockPropCommand", "Prop packet=" + packet);

        switch (packet.getCode()) {
            case MockPropPacket.CODE_DELETE_PROP_MAP:
            case  MockPropPacket.CODE_INSERT_UPDATE_PROP_MAP:
                return MockPropProvider.putMockPropMap(commandData.getContext(), commandData.getDatabase(), packet).toBundle();
            case  MockPropPacket.CODE_DELETE_PROP_SETTING:
            case MockPropPacket.CODE_INSERT_UPDATE_PROP_SETTING:
                return MockPropManager.putPropertySetting(commandData.getDatabase(), packet).toBundle();
            case MockPropPacket.CODE_DELETE_PROP_MAP_AND_SETTING:
                XResult res1 = MockPropProvider.putMockPropMap(commandData.getContext(), commandData.getDatabase(), packet);
                XResult res2 =  MockPropManager.putPropertySetting(commandData.getDatabase(), packet);
                return XResult.combine(res1, res2).toBundle();
        }

        return XResult.create().setMethodName("putMockProp")
                .setExtra(packet.toString())
                .setFailed("Cannot find Correct Command Code Handler! code=" + packet.getCode()).toBundle();
    }

    public static Bundle invoke(Context context, MockPropPacket packet) {
        return XProxyContent.mockCall(
                context,
                "putMockProp",
                packet.toBundle());
    }
}
