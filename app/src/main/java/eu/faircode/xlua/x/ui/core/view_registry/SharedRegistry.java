package eu.faircode.xlua.x.ui.core.view_registry;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.WeakHashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

/**
 * High-performance registry for view states using bitfields and optimized data structures.
 * Uses byte for state storage to minimize memory usage while maintaining fast operations.
 */
    /*
        ToDO: Clean up the "onChangers" system, Compress the Packet with CheckBoxState Object (or try too)
                    Make it more optimized
                    Compress Core UI Utils what not with this Nice system
                    Clean up base classes, as well as List Manager Class
                    For now if it works, it works!
              Clean this up make it more "generic" like States, Stacks, Objects etc, but everything is from Shared Objects including States
              The object for States ofc is a State Object or Something
              Use this class to help share Data between Views
              > Have this Handle EVERYTHING including Preferences etc , nice all in one system!
     */


public class SharedRegistry implements ISessionObject {
    private static final String TAG = LibUtil.generateTag(SharedRegistry.class);

    private static final Map<String, SharedRegistry> REGISTRY_CACHE = new WeakHashMap<>();
    private static final Map<String, Object> SHARED_OBJECTS = new HashMap<>();
    private static final Map<String, Stack<?>> STACKS = new HashMap<>();

    public static SharedRegistry get(String id) { return REGISTRY_CACHE.get(id); }
    public static SharedRegistry create() { return new SharedRegistry(); }
    public static SharedRegistry create(String id) { return new SharedRegistry(id); }

    public static void clearRegistryCache() { REGISTRY_CACHE.clear(); }

    private static final byte STATE_CHECKED = 0x1;
    private static final byte STATE_EXPANDED = 0x2;
    private static final int INITIAL_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.9f;

    public static final String STATE_TAG_GROUPS = "setting_container_groups";
    public static final String STATE_TAG_SETTINGS = "setting_settings";
    public static final String STATE_TAG_CONTAINERS = "setting_containers";

    public static final String STATE_TAG_COLLECTIONS = "collections";
    public static final String STATE_TAG_HOOKS = "hooks_hooks";
    public static final String STATE_TAG_GLOBAL = "*";

    public static final String SHARED_STACK = "stack_frame";

    @IntDef({STATE_CHECKED, STATE_EXPANDED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StateFlag {}

    private boolean pinThis = false;
    private final String ID;
    private final Map<String, LongSparseArray<Byte>> stateMap = new HashMap<>(8, LOAD_FACTOR);
    private final GroupRegistryNotifications groupNotifications = GroupRegistryNotifications.create(this);

    public final PrefManager preferences = new PrefManager();

    public void notifyGroupChange(String groupToNotify, String groupThatChanged) {
        groupNotifications.notifyGroupChange(groupToNotify, groupThatChanged);
    }

    public void putGroupChangeListener(IStateChanged onChangeListener, String groupName) {
        groupNotifications.putGroupChangeListener(onChangeListener, groupName);
    }

    public SharedRegistry() { this.ID = UUID.randomUUID().toString(); }
    public SharedRegistry(String sessionId) { this.ID = sessionId; }

    public SettingSharedRegistry asSettingShared() { return ObjectUtils.tryCast(this); }

    public PrefManager ensurePrefsOpen(Context context, String tagOrNamespace) { preferences.ensureIsOpen(context, tagOrNamespace); return preferences;  }

    @Override
    public SharedRegistry getRegistry() { return this; }

    @Override
    public String getSessionId() { return ID; }

    public void registerToCache() { REGISTRY_CACHE.put(ID, this); }

    public void removeFromCache() { REGISTRY_CACHE.remove(ID); }

    public void clearSharedObjectCache() { SHARED_OBJECTS.clear(); }

    public void removeSharedObject(String id) { SHARED_OBJECTS.remove(id); }

    public <T> T getSharedObject(String id) { return ObjectUtils.tryCast(SHARED_OBJECTS.get(id)); }

    public <T> void pushSharedObject(String id, T obj) { SHARED_OBJECTS.put(id, obj); }

    public void pin(boolean pinThis) { this.pinThis = pinThis; }    //improve

    public <T> T pop(Class<?> type) {
        Stack<T> stack = getStack(type, true);
        if(stack != null)
            return stack.pop();

        return null;
    }

    public <T> T push(T obj) {
        Stack<T> stack = getStack(obj.getClass(), true);
        if(stack != null)
            return stack.push(obj);

        return obj;
    }

    public <T> Stack<T> getStack(Class<?> type, boolean createIfNull) {
        try {
            String name = type.getName();
            Stack<T> stack = (Stack<T>) STACKS.get(name);
            if(stack == null) {
                stack = new Stack<>();
                STACKS.put(name, stack);
            }

            return stack;
        }catch (Exception e) {
            Log.e(TAG, "Failed to get Stack, Type=" + type + " Error=" + e);
            return null;
        }
    }

    @NonNull
    private LongSparseArray<Byte> getTagStates(String tag, boolean createIfMissing) {
        LongSparseArray<Byte> tagStates = stateMap.get(tag);
        if (tagStates == null && createIfMissing) {
            tagStates = new LongSparseArray<>(INITIAL_CAPACITY);
            stateMap.put(tag, tagStates);
        }
        return tagStates != null ? tagStates : new LongSparseArray<>(0);
    }

    private byte ensureState(String tag, String id) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, true);
        long hash = RegistryUtils.hashStringToLong_cache(id);
        Byte state = tagStates.get(hash);
        if (state == null) {
            state = 0;
            tagStates.put(hash, state);
        }
        return state;
    }

    private boolean updateState(String tag, String id, @StateFlag int flag, boolean value) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, true);
        long hash = RegistryUtils.hashStringToLong_cache(id);
        byte currentState = tagStates.get(hash, (byte)0);
        byte newState = (byte)(value ? (currentState | flag) : (currentState & ~flag));
        tagStates.put(hash, newState);
        return value;
    }

    private boolean toggleState(String tag, String id, @StateFlag int flag) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, true);
        long hash = RegistryUtils.hashStringToLong_cache(id);
        byte currentState = tagStates.get(hash, (byte)0);
        boolean newValue = (currentState & flag) == 0;
        byte newState = (byte)(newValue ? (currentState | flag) : (currentState & ~flag));
        tagStates.put(hash, newState);
        return newValue;
    }

    public boolean isChecked(@NonNull String tag, @NonNull String id) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, false);
        return (tagStates.get(RegistryUtils.hashStringToLong_cache(id), (byte)0) & STATE_CHECKED) != 0;
    }

    public boolean isExpanded(@NonNull String tag, @NonNull String id) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, false);
        return (tagStates.get(RegistryUtils.hashStringToLong_cache(id), (byte)0) & STATE_EXPANDED) != 0;
    }

    public boolean setChecked(@NonNull String tag, @NonNull String id, boolean checked) {
        return  updateState(tag, id, STATE_CHECKED, checked);
    }

    public <T extends IIdentifiableObject> void setCheckedBulk(@NonNull String tag, @NonNull List<T> objects, boolean checked) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, true);
        if(!ListUtil.isValid(objects)) return;
        for(IIdentifiableObject o : objects) {
            if(o != null) {
                long hash = RegistryUtils.hashStringToLong_cache(o.getId());
                byte currentState = tagStates.get(hash, (byte)0);
                byte newState = (byte)(checked ? (currentState | STATE_CHECKED) : (currentState & ~STATE_CHECKED));
                tagStates.put(hash, newState);
            }
        }
    }

    public boolean setExpanded(@NonNull String tag, @NonNull String id, boolean expanded) {
        return updateState(tag, id, STATE_EXPANDED, expanded);
    }

    public boolean toggleChecked(@NonNull String tag, @NonNull String id) {
        return toggleState(tag, id, STATE_CHECKED);
    }

    public boolean toggleExpanded(@NonNull String tag, @NonNull String id) {
        return toggleState(tag, id, STATE_EXPANDED);
    }

    @NonNull
    public ItemState getItemState(@NonNull String tag, @NonNull String id) {
        byte state = ensureState(tag, id);
        return new ItemState(
                (state & STATE_CHECKED) != 0,
                (state & STATE_EXPANDED) != 0
        );
    }

    public static final class ItemState {
        public final boolean isChecked;
        public final boolean isExpanded;

        ItemState(boolean isChecked, boolean isExpanded) {
            this.isChecked = isChecked;
            this.isExpanded = isExpanded;
        }
    }

    public void clearTag(@NonNull String tag) {
        LongSparseArray<Byte> tagStates = stateMap.remove(tag);
        if (tagStates != null)
            tagStates.clear();
    }

    public void clear() {
        ListUtil.clearMap(stateMap);
        //HASH_CACHE.clear();
    }

    public <T extends IIdentifiableObject> int getEnabledCount(List<T> objects, String tag) {
        int count = 0;
        if(objects == null || TextUtils.isEmpty(tag))
            return count;

        LongSparseArray<Byte> tagStates = stateMap.get(tag);
        if(tagStates == null || tagStates.isEmpty())
            return count;

        for(IIdentifiableObject o : objects) {
            if(o != null) {
                long hash = RegistryUtils.hashStringToLong_cache(o.getId());
                if(tagStates.get(hash, (byte)0) != 0)
                    count++;

            }
        }

        return count;
    }

    @Override
    protected void finalize() throws Throwable {
        if(!pinThis) {
            try {
                removeFromCache(); // Ensure removal from the cache
            } finally {
                super.finalize();
            }
        }
    }
}