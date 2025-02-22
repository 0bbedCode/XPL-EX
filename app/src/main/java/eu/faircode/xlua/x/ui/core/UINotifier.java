package eu.faircode.xlua.x.ui.core;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;

public class UINotifier {
    private static final String TAG = LibUtil.generateTag(UINotifier.class);

    public interface IUINotification {
        String getNotifierId();
        void notify(int code, String notifier, Object extra);
    }

    //Put the groups here



    public static final int CODE_CHECK = 1;
    public static final int CODE_DATA_CHANGED = 2;

    private final Map<String, UINotifierGroup> groups = new HashMap<>();

    public boolean hasGroups() { return !groups.isEmpty(); }

    public void notifyChange(String itemChanged, int code) {
        if(!Str.isEmpty(itemChanged)) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Notifying Group of Change, Item changed=" + itemChanged + " Code=" + code);

            UINotifierGroup group = groups.get(itemChanged);
            if(group != null) {
                group.notify(itemChanged, code);
            }
        }
    }

    public void prepareGroup(String groupId, String prepareName) {
        if(!Str.isEmpty(groupId) && !Str.isEmpty(prepareName)) {
            UINotifierGroup group = groups.get(groupId);
            if(group != null)  {
                group.prepare(prepareName);
                groups.put(prepareName, group);
            }
        }
    }

    public void unsubscribeGroup(IUINotification notifier) {
        if(notifier != null) {
            String id = notifier.getNotifierId();
            if(!Str.isEmpty(id)) {
                UINotifierGroup group = groups.get(id);
                if(group != null) {
                   group.unsubscribe(notifier);
                }
            }
        }
    }

    public void subscribeGroup(IUINotification notifier) {
        if(notifier != null) {
            String id = notifier.getNotifierId();
            if(!Str.isEmpty(id)) {
                UINotifierGroup group = groups.get(id);
                if(group == null) {
                    group = new UINotifierGroup();
                    group.subscribe(notifier);
                    groups.put(id, group);
                } else {
                    group.subscribe(notifier);
                }
            }
        }
    }


    private final Map<String, Map<String, IUINotification>> notifications = new HashMap<>();
    private final Object lock = new Object(); // Lock object for thread safety


    public static String groupName(String groupName) { return Str.ensureStartsWith("group:", groupName); }
    public static String containerName(String containerName) { return Str.ensureStartsWith("container:", containerName); }
    public static String settingName(String settingName) { return Str.ensureStartsWith("setting:", settingName); }

    public static boolean isGroupPrefix(String id) { return id != null && id.startsWith("group:"); }
    public static boolean isContainerPrefix(String id) { return id != null && id.startsWith("container:"); }
    public static boolean isSettingPrefix(String id) { return id != null && id.startsWith("setting:"); }

    public UINotifier subscribe(String groupToSubscribeTo, IUINotification notification) {
        if(!Str.isEmpty(groupToSubscribeTo) && notification != null) {
            String notifierId = notification.getNotifierId();
            if(!Str.isEmpty(notifierId)) {
                synchronized(lock) {
                    // Get or create the map for this group
                    Map<String, IUINotification> notifierMap = notifications.get(groupToSubscribeTo);
                    if(notifierMap == null) {
                        notifierMap = new HashMap<>();
                        notifications.put(groupToSubscribeTo, notifierMap);
                    }

                    // Store the notification callback with notifierId as key
                    notifierMap.put(notifierId, notification);
                }
            }
        }
        return this;
    }

    public UINotifier unsubscribe(String groupToUnsubscribeFrom, IUINotification notification) {
        if(!Str.isEmpty(groupToUnsubscribeFrom) && notification != null) {
            String notifierId = notification.getNotifierId();
            if(!Str.isEmpty(notifierId)) {
                synchronized(lock) {
                    // Get the map for this group
                    Map<String, IUINotification> notifierMap = notifications.get(groupToUnsubscribeFrom);
                    if(notifierMap != null) {
                        // Remove the notification callback for this notifierId
                        notifierMap.remove(notifierId);

                        // Clean up empty maps
                        if(notifierMap.isEmpty()) {
                            notifications.remove(groupToUnsubscribeFrom);
                        }
                    }
                }
            }
        }
        return this;
    }

    public UINotifier notifyChecked(String groupToNotify, IUINotification notifier) { return notify(groupToNotify, notifier.getNotifierId(), CODE_CHECK, null); }
    public UINotifier notifyChecked(String groupToNotify, String groupInvokingNotifier) { return notify(groupToNotify, groupInvokingNotifier, CODE_CHECK, null); }

    public UINotifier notify(String groupToNotify, String groupInvokingNotifiers, int code, Object extra) {
        if(!Str.isEmpty(groupToNotify)) {
            Map<String, IUINotification> notifierMap;
            synchronized(lock) {
                // Get a copy of the notifier map to avoid concurrent modification
                notifierMap = notifications.get(groupToNotify);
                if(notifierMap != null) {
                    notifierMap = new HashMap<>(notifierMap);
                }
            }

            if(notifierMap != null) {
                // Notify all subscribers outside the synchronized block to avoid deadlocks
                for(Map.Entry<String, IUINotification> entry : notifierMap.entrySet()) {
                    String notifierId = entry.getKey();
                    IUINotification notification = entry.getValue();
                    if(notification != null && notifierId.equalsIgnoreCase(notification.getNotifierId())) {
                        try {
                            notification.notify(code, groupInvokingNotifiers, extra);
                        } catch(Exception e) {
                            // Prevent one bad notification from stopping others
                            // Consider logging this exception
                        }
                    }
                }
            }
        }
        return this;
    }
}