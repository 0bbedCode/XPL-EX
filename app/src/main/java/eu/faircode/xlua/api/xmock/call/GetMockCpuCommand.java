package eu.faircode.xlua.api.xmock.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.cpu.MockCpu;
import eu.faircode.xlua.api.xmock.provider.MockCpuProvider;
import eu.faircode.xlua.utilities.BundleUtil;

public class GetMockCpuCommand extends CallCommandHandler {
    @SuppressWarnings("unused")
    public GetMockCpuCommand() {
        name = "getMockCpu";
        requiresPermissionCheck = false;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        MockCpu packet = commandData.readExtrasAs(MockCpu.class);
        if(packet.getName() == null) {
            return BundleUtil.createFromISerial(
                    MockCpuProvider.getSelectedCpuMap(
                            commandData.getContext(),
                            commandData.getDatabase()), true);
        }
        return null;
    }

    public static Bundle invoke(Context context, String name) {
        return XProxyContent.mockCall(
                context,
                "getMockCpu",
                BundleUtil.createSingleString("name", name));
    }

    public static Bundle invoke(Context context) {
        return XProxyContent.mockCall(
                context,
                "getMockCpu",
                new Bundle());
    }
}
