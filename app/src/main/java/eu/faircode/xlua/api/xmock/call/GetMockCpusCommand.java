package eu.faircode.xlua.api.xmock.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.cpu.MockCpuConversions;
import eu.faircode.xlua.api.xmock.database.MockCpuManager;

public class GetMockCpusCommand extends CallCommandHandler {
    public static GetMockCpusCommand create() { return new GetMockCpusCommand(); };

    public GetMockCpusCommand() {
        name = "getMockCpus";
        requiresPermissionCheck = false;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        return MockCpuConversions.toBundleArray(
                MockCpuManager.getCpuMaps(
                        commandData.getContext(),
                        commandData.getDatabase()));
    }

    public static Bundle invoke(Context context) {
        return XProxyContent.mockCall(
                context,
                "getMockCpus");
    }
}
