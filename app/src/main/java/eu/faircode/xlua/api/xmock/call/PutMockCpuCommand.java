package eu.faircode.xlua.api.xmock.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.cpu.MockCpu;
import eu.faircode.xlua.api.xmock.provider.MockCpuProvider;

public class PutMockCpuCommand extends CallCommandHandler {
    public static PutMockCpuCommand create() { return new PutMockCpuCommand(); };
    public PutMockCpuCommand() {
        name = "putMockCpu";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        MockCpu packet = commandData.readExtrasAs(MockCpu.class);
        return MockCpuProvider.putMockCpuMap(
                        commandData.getDatabase(),
                        packet.getName(),
                        packet.isSelected()).toBundle();
    }

    public static Bundle invoke(Context context, String cpuMapName, boolean selected) {
        MockCpu map = new MockCpu(cpuMapName, null, null, null, selected);
        return invoke(context, map);
    }

    public static Bundle invoke(Context context, MockCpu packet) {
        return XProxyContent.mockCall(
                context,
                "putMockCpu",
                packet.toBundle());
    }
}
