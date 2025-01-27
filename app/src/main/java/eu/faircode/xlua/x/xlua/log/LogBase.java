package eu.faircode.xlua.x.xlua.log;

import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

/*
    Lets make interchange able core, do note we need a "service"
 */
public abstract class LogBase {
    //public static RootManager MANAGER = RootUtils.MANAGER;

    public abstract void usage(UserIdentity app);
    public abstract void usage(AppXpPacket app);
    public abstract void usage(UserClientAppContext app);
    public abstract void usage(int userId, String packageName);

    public abstract void error(UserIdentity app);
    public abstract void error(AppXpPacket app);
    public abstract void error(UserClientAppContext app);
    public abstract void error(int userId, String packageName);

    public abstract void deploy(UserIdentity app);
    public abstract void deploy(AppXpPacket app);
    public abstract void deploy(UserClientAppContext app);
    public abstract void deploy(int userId, String packageName);

}
