package eu.faircode.xlua.api.xmock.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.properties.MockPropMap;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;

public class PutGroupStateCommand extends CallCommandHandler {
    public static PutGroupStateCommand create() { return new PutGroupStateCommand(); };
    public PutGroupStateCommand() {
        name = "putGroupState";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        /*MockPropMap packet = commandData.read(MockPropMap.class);
        return BundleUtil.createResultStatus(
                XMockPropertiesProvider.setGroupState(commandData.getContext(), commandData.getDatabase(), packet));*/

        return null;
    }

    public static Bundle invoke(Context context, MockPropMap settingPacket) {
        return XProxyContent.mockCall(
                context,
                "putGroupState", settingPacket.toBundle());
    }
}
