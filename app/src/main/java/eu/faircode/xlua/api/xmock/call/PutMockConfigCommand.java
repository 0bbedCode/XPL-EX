package eu.faircode.xlua.api.xmock.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.xmock.database.MockConfigManager;
import eu.faircode.xlua.api.configs.MockConfigPacket;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;

public class PutMockConfigCommand extends CallCommandHandler {
    public static PutMockConfigCommand create() { return new PutMockConfigCommand(); };
    public PutMockConfigCommand() {
        name = "putMockConfig";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        MockConfigPacket packet = commandData.readExtrasAs(MockConfigPacket.class);
        if(packet == null) return XResult.create().setMethodName("putMockConfig").setFailed("Mock Config Packet is NULL!").toBundle();
        packet.ensureCode(MockConfigPacket.CODE_INSERT_UPDATE_CONFIG);
        switch (packet.getCode()) {
            case MockConfigPacket.CODE_DELETE_CONFIG:
            case MockConfigPacket.CODE_INSERT_UPDATE_CONFIG:
                return MockConfigManager.putMockConfig(commandData.getContext(), commandData.getDatabase(), packet).toBundle();
        }

        return XResult.create().setMethodName("putMockConfig").setFailed("Failed to find Command Handler for Mock Config Packet! Code=" + packet.getCode()).toBundle();
    }

    public static Bundle invoke(Context context, MockConfigPacket packet) {
        return XProxyContent.mockCall(
                context,
                "putMockConfig",
                packet.toBundle());
    }
}
