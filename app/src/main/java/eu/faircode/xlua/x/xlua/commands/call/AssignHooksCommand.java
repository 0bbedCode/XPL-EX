package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.hook.AssignmentApi;
import eu.faircode.xlua.x.xlua.hook.AssignmentsPacket;

/*
    ToDo: Make Legacy version
 */
public class AssignHooksCommand extends CallCommandHandlerEx {
    public AssignHooksCommand() {
        name = "assignExHooksEx";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        return AssignmentApi.assignHooks(
                commandData.getContext(),
                commandData.getDatabase(),
                commandData.readExtraAs(AssignmentsPacket.class)).toBundle();
    }


    public static A_CODE call(Context context, AssignmentsPacket packet) {
        return A_CODE.fromBundle(XProxyContent.luaCall(
                context,
                "assignExHooksEx",
                packet.toBundle()));
    }
}