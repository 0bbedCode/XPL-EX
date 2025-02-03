package eu.faircode.xlua.x.xlua.settings.test;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.test.interfaces.IUIViewControl;

/** @noinspection unused*/
public class SharedViewControl implements IIdentifiableObject {
    private static final String TAG = LibUtil.generateTag(SharedViewControl.class);


    public static final String G_SETTINGS = "settings";
    public static final String G_S_CONTAINERS = "setting_containers";
    public static final String G_S_GROUPS = "setting_groups";



    public static SharedViewControl create() { return new SharedViewControl(); }

    private final Object _lock = new Object();
    private final ExecutorService _executor = Executors.newFixedThreadPool(50);
    private SharedSpace _sharedSpace;
    private SharedViewControl _parentView;

    private String _tag;
    private Activity _activity;
    private Fragment _fragment;

    public String getTag() { return _tag; }

    public SharedSpace getShared() { return _sharedSpace; }
    public SharedViewControl getParent() { return _parentView; }

    public Activity getActivity() { return _activity; }
    public Fragment getFragment() { return _fragment; }

    public boolean hasShared() { return _sharedSpace != null; }
    public boolean hasParent() { return _parentView != null; }
    public boolean hasActivity() { return _activity != null; }
    public boolean hasFragment() { return _fragment != null; }

    private final StateMap _states = new StateMap();
    private final Map<String, IUIViewControl> _views = new HashMap<>();
    private final Map<String, IUIViewControl> _uniqueViews = new HashMap<>();

    private final Map<String, Map<String, IUIViewControl>> _eventListeners = new HashMap<>();

    //Another way
    //Each View has a List of things they want to update ?
    //But then how would we always get that list ?


    //Lets start really using the "ids" ?
    //So the Containers will register ID Event listeners for all the Settings
    //
    //Lets do as is actually, make it a BIT more simple
    //ALL the Item does is trigger an event notification BUT no DATA attached
    //Ah yes A RE INIT notification
    //If its a RE INIT notification it will do whatever checking it needs to do like check if any are checked then gray or blue or no check
    //Set Text Color ETC ETC Yes !
    //
    //Can take in Code, if code is "data" then it can handle "data" if needed ? Perfect

    public void notifyChecked(String groupChanged) {
        EventTrigger trigger = new EventTrigger();
        trigger.code = EventKind.CHECK.getValue();
        trigger.from = groupChanged;
        notifyEvent(trigger);
    }

    public void notifyEvent(EventTrigger event) {
        //Should we multi thread then Main Looper invoke ?
        //Make some system handle Recycling, Maybe a Original Cache ID system if the ID does not match Cache ignore it ?
        _executor.submit(() -> {
            try {
                Map<String, IUIViewControl> notifiers = _eventListeners.get(event.from); //As a copy ?
                //Create a copy ?
                if(ListUtil.isValid(notifiers)) {
                    for(Map.Entry<String, IUIViewControl> entry : notifiers.entrySet()) {
                        String originalId = entry.getKey();
                        IUIViewControl view = entry.getValue();
                        if(view != null) {
                            try {
                                if(!view.isView(event.from) && view.isView(originalId))
                                    new Handler(Looper.getMainLooper()).post(() -> view.onEvent(event));
                            }catch (Exception e) {
                                Log.e(TAG, "Failed to Execute Event Listener! Error=" + e);
                            }
                        }
                    }
                }
            }catch (Exception e) {
                Log.e(TAG, "Failed to Execute Event Notification: " + Str.toStringOrNull(event) + " Error=" + e);
            }
        });
    }

    public void registerEventListeners(IUIViewControl viewControl, String... notifyOnChangesFor) {
        if(ArrayUtils.isValid(notifyOnChangesFor)) {
            for(String n : notifyOnChangesFor) {
                registerEventListener(n, viewControl);
            }
        }
    }

    public void registerEventListener(String notifyOnChangesFor, IUIViewControl view) {
        if(notifyOnChangesFor != null) {
            synchronized (_lock) {
                String id = view.getSharedId();
                Map<String, IUIViewControl> notifiers = _eventListeners.get(notifyOnChangesFor);
                if(notifiers == null) {
                    notifiers = new HashMap<>(1);
                    notifiers.put(id, view);
                    _eventListeners.put(notifyOnChangesFor, notifiers);
                } else {
                    notifiers.put(id, view);
                }
            }
        }
    }

    public SharedViewControl setParent(SharedViewControl parent) { this._parentView = parent; return this; }
    public SharedViewControl setSharedSpace(SharedSpace sharedSpace) { this._sharedSpace = sharedSpace; return this; }

    public SharedViewControl setTag(String tag) { this._tag = tag; return this; }
    public SharedViewControl setActivity(Activity activity) { this._activity = activity; return this; }
    public SharedViewControl setFragment(Fragment fragment) { this._fragment = fragment; return this; }

    public SharedViewControl() { }
    public SharedViewControl(String tag) { this(null, tag, null, null, null); }
    public SharedViewControl(String tag, Activity activity) { this(null, tag, activity, null, null); }
    public SharedViewControl(String tag, Activity activity, Fragment fragment) { this(null, tag, activity, fragment, null); }
    public SharedViewControl(String tag, Activity activity, SharedSpace sharedSpace) { this(null, tag, activity, null, sharedSpace); }
    public SharedViewControl(SharedViewControl parent, String tag) { this(parent, tag, null, null, null); }
    public SharedViewControl(SharedViewControl parent, String tag, Activity activity) { this(parent, tag, activity, null, null); }
    public SharedViewControl(SharedViewControl parent, String tag, Fragment fragment) { this(parent, tag, null, fragment, null); }
    public SharedViewControl(SharedViewControl parent, String tag, SharedSpace sharedSpace) { this(parent, tag, null, null, sharedSpace); }
    public SharedViewControl(SharedViewControl parent, String tag, Activity activity, Fragment fragment, SharedSpace sharedSpace) {
        this._parentView = parent;
        this._tag = tag;
        this._activity = activity;
        this._fragment = fragment;
        this._sharedSpace = sharedSpace;
    }

    public boolean isChecked(String tag, String id) { return _states.isChecked(tag, id); }
    public boolean isExpanded(String tag, String id) { return _states.isExpanded(tag, id); }

    /* Link these to events invokers, it will run down the list of views to notify */
    public boolean setChecked(String tag, String id, boolean isChecked) { return _states.setChecked(tag, id, isChecked); }
    public boolean setExpanded(@NonNull String tag, @NonNull String id, boolean expanded) { return _states.setExpanded(tag, id, expanded); }
    public boolean toggleChecked(@NonNull String tag, @NonNull String id) { return _states.toggleChecked(tag, id); }
    public boolean toggleExpanded(@NonNull String tag, @NonNull String id) { return _states.toggleExpanded(tag, id); }

    //If a check event happens, then it will be tied to a ID
    //If the event handler for check kinds use TAG etc
    //Like shared registry

    //When passed a view
    //We can check if it has certain things to know where to fit it ?
    //Hmm I want to use a System where objects like "holder" can actually link to the bullshit they need
    //Make this hectic system easier and faster!


    //We can have Codes Map<CODE, List<EVENTS_TO_NOTIFY>>
    //Lets just do this "build as we go" ! alot easier

    //We can maybe do something like
    //onListChange(List<SettingPacket> items, Clazz)

    //We can have groups, caller can notify many !

    //Can send something like this across ?
    //We can inherit over for like "check" event
    //Map<EVENT_ID,...>   then "notify(EventNotification notification, Event Ids...)
    public static class EventNotification {
        public int code;
        public Object data;
    }

    public void triggerEvent(int code) {

    }

    public void notifyFragment(EventTrigger event) {

    }


    /* Invoke changes with result ? or ? */

    public void tryExecute(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to execute VOID Task ID [%s] Error=%s", _tag, e));
        }
    }

    public <T> T tryExecute(Callable<T> action) {
        try {
            return action.call();
        } catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to execute Task ID [%s] Error=%s", _tag, e));
            return null;
        }
    }
}
