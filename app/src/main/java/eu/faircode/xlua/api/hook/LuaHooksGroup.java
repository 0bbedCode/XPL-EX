package eu.faircode.xlua.api.hook;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.faircode.xlua.api.app.XLuaApp;
import eu.faircode.xlua.api.hook.assignment.LuaAssignment;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.utilities.SettingUtil;

public class LuaHooksGroup {
    public String groupId;

    public int id;
    public String name;
    public String title;
    public boolean exception = false;
    public int installed = 0;
    public int optional = 0;
    public long used = -1;
    public int assigned = 0;
    public List<XLuaHook> hooks = new ArrayList<>();

    private String titleName = null;

    public String getCleanTitle() {
        if(titleName == null || titleName.isEmpty()) {
            titleName = SettingUtil.cleanSettingName(title);
        }

        return titleName;
    }

    public boolean hasWarning = false;

    public XLuaApp app;

    public LuaHooksGroup() { }
    public boolean hasException() {
        return (assigned > 0 && exception);
    }
    public boolean hasInstalled() {
        return (assigned > 0 && installed > 0);
    }
    public boolean allInstalled() {
        return (assigned > 0 && installed + optional == assigned);
    }
    public long lastUsed() {
        return used;
    }
    public boolean hasAssigned() {
        return (assigned > 0);
    }

    public boolean allAssigned() {
        return (assigned == hooks.size());
    }
}
