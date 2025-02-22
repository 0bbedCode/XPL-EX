package eu.faircode.xlua.x.ui.core;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;

public class UINotifierGroup {
    private static final String TAG = LibUtil.generateTag(UINotifierGroup.class);


    private final Map<String, UINotifier.IUINotification> notifiers = new HashMap<>();

    public void notify(String groupChanged, int code) {
        if(!Str.isEmpty(groupChanged)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Notifying this Group, This Group Notification Count=" + notifiers.size() + " Event notifying=" + groupChanged + " Code=" + code);

            for(Map.Entry<String, UINotifier.IUINotification> entry : new HashMap<>(notifiers).entrySet()) {
                String id = entry.getKey();
                UINotifier.IUINotification notification = entry.getValue();
                if(notification != null && !groupChanged.equalsIgnoreCase(id)) {
                    try {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Notifying Item:" + id + " From:" + groupChanged + " Code=" + code);

                        //Have a main looper check ?
                        notification.notify(code, groupChanged, null);
                    }catch (Exception ignored) { }
                }
            }
        }
    }

    public void prepare(String id) {
        if(!Str.isEmpty(id)) {
            if(!notifiers.containsKey(id)) {
                notifiers.put(id, null);
            }
        }
    }

    public void unsubscribe(UINotifier.IUINotification notifier) {
        if(notifier != null) {
            String id = notifier.getNotifierId();
            if(!Str.isEmpty(id)) {
                notifiers.put(id, null);
            }
        }
    }

    public void subscribe(UINotifier.IUINotification notifier) {
        if(notifier != null) {
            String id = notifier.getNotifierId();
            if(!Str.isEmpty(id)) {
                notifiers.put(id, notifier);
            }
        }
    }
}
