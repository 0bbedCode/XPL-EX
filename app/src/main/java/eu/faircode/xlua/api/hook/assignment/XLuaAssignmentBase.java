package eu.faircode.xlua.api.hook.assignment;

//import eu.faircode.xlua.XAssignment;

import eu.faircode.xlua.api.hook.XLuaHook;

public class XLuaAssignmentBase {
    protected XLuaHook hook;
    protected long installed = -1;
    protected long used = -1;
    protected boolean restricted = false;
    protected String exception;

    public XLuaAssignmentBase() { }
    public XLuaAssignmentBase(XLuaHook hook) {
        this.hook = hook;
    }

    public XLuaHook getHook() { return this.hook; }
    public long getInstalled() { return this.installed; }
    public long getUsed() { return this.used; }
    public boolean getRestricted() { return this.restricted; }
    public String getException() { return this.exception; }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof XLuaAssignmentBase))
            return false;
        XLuaAssignmentBase other = (XLuaAssignmentBase) obj;
        return this.hook.getSharedId().equals(other.hook.getSharedId());
    }

    @Override
    public int hashCode() {
        return this.hook.getSharedId().hashCode();
    }
}
