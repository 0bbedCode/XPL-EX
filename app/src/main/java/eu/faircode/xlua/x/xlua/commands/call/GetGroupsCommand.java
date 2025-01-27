package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import eu.faircode.xlua.UberCore888;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;

public class GetGroupsCommand extends CallCommandHandlerEx {
    public static final String FIELD_GROUP = "groups";
    public static final String COMMAND_NAME = "getGroups";
    public GetGroupsCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        this.requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        return BundleUtil.
                createFromStringList(
                        FIELD_GROUP,
                        UberCore888.getGroupsEx(commandData.getDatabase()));
    }

    public static List<String> get(Context context) {
        return BundleUtil
                .getStringArrayList(
                        XProxyContent.luaCall(context, COMMAND_NAME), FIELD_GROUP);
    }
}