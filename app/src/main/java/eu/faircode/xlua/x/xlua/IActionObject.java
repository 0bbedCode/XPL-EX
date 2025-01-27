package eu.faircode.xlua.x.xlua;

import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public interface IActionObject {
    boolean hasAction();
    boolean hasIdentification();

    int getActionExtra();
    ActionFlag getActionFlags();
    boolean shouldKill();

    boolean isAction(ActionFlag flag);

    int getUid();
    int getUserId(boolean resolveUid);
    String getCategory();

    UserIdentity getUserIdentity();
    ActionPacket getActionPacket();
}
