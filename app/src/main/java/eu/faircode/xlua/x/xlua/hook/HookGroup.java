package eu.faircode.xlua.x.xlua.hook;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.api.hook.XLuaHook;

public class HookGroup {
    public int resourceId;
    public String name;
    public String title;
    public String groupId;
    public boolean hasWarning;

    public boolean exception = false;
    public int installed = 0;
    public int optional = 0;
    public long used = -1;
    public int assigned = 0;

    public boolean isAllAssigned() { return assigned == hooks.size(); }

    public final List<XLuaHook> hooks = new ArrayList<>();
}
