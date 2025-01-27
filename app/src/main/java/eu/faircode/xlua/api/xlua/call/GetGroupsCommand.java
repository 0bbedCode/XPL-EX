package eu.faircode.xlua.api.xlua.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.utilities.BundleUtil;

public class GetGroupsCommand extends CallCommandHandler {
    public static GetGroupsCommand create() { return new GetGroupsCommand(); }
    public GetGroupsCommand() {
        name = "getGroups";
        requiresPermissionCheck = false;
        this.requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket_old commandData) throws Throwable {
        return BundleUtil.
                createFromStringList(
                        "groups",
                        UberCore888.getGroups(commandData.getContext(), commandData.getDatabase()));
    }

    public static Bundle invoke(Context context) {
        return XProxyContent.luaCall(
                context,
                "getGroups");
    }
}
