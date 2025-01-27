package eu.faircode.xlua.x.ui.core;

import android.util.Log;
import android.view.View;

import java.util.Collection;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;

public class CoreUiLog {
    public static void logDataChanged(Collection<?> collection, String tag) { logDataChanged(collection, tag, DebugUtil.isDebug()); }
    public static void logDataChanged(Collection<?> collection, String tag, boolean log) {
        if(log)
            Log.d(tag, "Data from Observer List has Changed and or Updated! Data Size=" + ListUtil.size(collection));
    }

    public static void logGettingLiveData(String tag) { logGettingLiveData(tag, DebugUtil.isDebug()); }
    public static void logGettingLiveData(String tag, boolean log) {
        if(log)
            Log.d(tag, "Grabbing the Live Data from the List");
    }

    public static void logIsRefreshing(String tag) { logIsRefreshing(tag, DebugUtil.isDebug()); }
    public static void logIsRefreshing(String tag, boolean log) {
        if(log)
            Log.d(tag, "Is Refreshing Data Recycler View");
    }

    public static int getViewIdOnLongClickItem(View view) { return getViewIdOnLongClickItem(view, false, null); }
    public static int getViewIdOnLongClickItem(View view, String tagLog) { return getViewIdOnLongClickItem(view, DebugUtil.isDebug(), tagLog); }
    public static int getViewIdOnLongClickItem(View view, boolean log, String tagLog) {
        int id = view == null ? -1 :  view.getId();
        if(log && id != -1 && tagLog != null) Log.d(tagLog,  "onLongClick(item)=" + id);
        return id;
    }

    public static int getViewIdOnClickItem(View view) { return getViewIdOnClickItem(view, false, null); }
    public static int getViewIdOnClickItem(View view, String tagLog) { return getViewIdOnClickItem(view, DebugUtil.isDebug(), tagLog); }
    public static int getViewIdOnClickItem(View view, boolean log, String tagLog) {
        int id = view == null ? -1 : view.getId();
        if(log && id != -1 && tagLog != null) Log.d(tagLog,  "onClick(item)=" + id);
        return id;
    }



    public static int getViewIdOnLongClick(View view) { return getViewIdOnLongClick(view, false, null); }
    public static int getViewIdOnLongClick(View view, String tagLog) { return getViewIdOnLongClick(view, DebugUtil.isDebug(), tagLog); }
    public static int getViewIdOnLongClick(View view, boolean log, String tagLog) {
        int id = view == null ? -1 :  view.getId();
        if(log && id != -1 && tagLog != null) Log.d(tagLog,  "onLongClick=" + id);
        return id;
    }

    public static int getViewIdOnClick(View view) { return getViewIdOnClick(view, false, null); }
    public static int getViewIdOnClick(View view, String tagLog) { return getViewIdOnClick(view, DebugUtil.isDebug(), tagLog); }
    public static int getViewIdOnClick(View view, boolean log, String tagLog) {
        int id = view == null ? -1 : view.getId();
        if(log && id != -1 && tagLog != null) Log.d(tagLog,  "onClick=" + id);
        return id;
    }

    public static int getViewId(View view) { return getViewId(view, false, null, null); }
    public static int getViewId(View view, String eventLog, String tagLog) { return getViewId(view, DebugUtil.isDebug(), eventLog, tagLog); }
    public static int getViewId(View view, boolean log, String eventLog, String tagLog) {
        int id = view == null ? -1 : view.getId();
        if(log && id != -1 && tagLog != null) Log.d(tagLog, StrBuilder.create().append(eventLog).append("=").append(id).toString());
        return id;
    }
}
