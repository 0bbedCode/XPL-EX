package eu.faircode.xlua.x.xlua.settings.test;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.ui.core.view_registry.RegistryUtils;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.settings.test.interfaces.ICreateObject;

/*
    ToDo: Create a UI Stack, so the deeper it goes within the UI it keeps appending each UI
        Example:
                [0] Fragment
                [1] ViewHolder
                [3] ViewHolder... so on

 */

public class StateMap implements IIdentifiableObject {
    private static final byte STATE_CHECKED = 0x1;
    private static final byte STATE_EXPANDED = 0x2;

    public static final int INITIAL_CAPACITY = 16;
    public static final float LOAD_FACTOR = 0.9f;

    /**
     * Key=(ID) Value=(Pair<CHECKED, EXPANDED>)
     */
    private final Map<String, LongSparseArray<Byte>> _map = new HashMap<>(8, LOAD_FACTOR);

    @IntDef({STATE_CHECKED, STATE_EXPANDED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StateFlag { }

    private String id;
    public StateMap() { }
    public StateMap(String id) { this.id = id; }

    @Override
    public String getObjectId() { return id; }
    @Override
    public void setId(String id) { this.id = id; }

    public boolean setChecked(@NonNull String tag, @NonNull String id, boolean checked) { return  updateState(tag, id, STATE_CHECKED, checked); }
    public boolean isChecked(@NonNull String tag, @NonNull String id) { return (getTagStates(tag, false).get(SharedSpaceUtils.hashStringToLong_cache(id), (byte)0) & STATE_CHECKED) != 0; }
    public boolean isExpanded(@NonNull String tag, @NonNull String id) { return (getTagStates(tag, false).get(SharedSpaceUtils.hashStringToLong_cache(id), (byte)0) & STATE_EXPANDED) != 0; }

    @NonNull
    private LongSparseArray<Byte> getTagStates(String tag, boolean createIfMissing) {
        LongSparseArray<Byte> tagStates = _map.get(tag);
        if (tagStates == null && createIfMissing) {
            tagStates = new LongSparseArray<>(INITIAL_CAPACITY);
            _map.put(tag, tagStates);
        }
        return tagStates != null ? tagStates : new LongSparseArray<>(0);
    }

    private byte ensureState(String tag, String id) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, true);
        long hash = SharedSpaceUtils.hashStringToLong_cache(id);
        Byte state = tagStates.get(hash);
        if (state == null) {
            state = 0;
            tagStates.put(hash, state);
        }
        return state;
    }

    private boolean updateState(String tag, String id, @StateFlag int flag, boolean value) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, true);
        long hash = SharedSpaceUtils.hashStringToLong_cache(id);
        byte currentState = tagStates.get(hash, (byte)0);
        byte newState = (byte)(value ? (currentState | flag) : (currentState & ~flag));
        tagStates.put(hash, newState);
        return value;
    }

    private boolean toggleState(String tag, String id, @StateFlag int flag) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, true);
        long hash = SharedSpaceUtils.hashStringToLong_cache(id);
        byte currentState = tagStates.get(hash, (byte)0);
        boolean newValue = (currentState & flag) == 0;
        byte newState = (byte)(newValue ? (currentState | flag) : (currentState & ~flag));
        tagStates.put(hash, newState);
        return newValue;
    }

    public boolean setExpanded(@NonNull String tag, @NonNull String id, boolean expanded) { return updateState(tag, id, STATE_EXPANDED, expanded); }

    public boolean toggleChecked(@NonNull String tag, @NonNull String id) { return toggleState(tag, id, STATE_CHECKED); }

    public boolean toggleExpanded(@NonNull String tag, @NonNull String id) { return toggleState(tag, id, STATE_EXPANDED); }


    /**
     * Gets the count of checked items in a specific tag group
     * @param tag The tag group to count checked items from
     * @return The number of checked items in the tag group
     */
    public int getCheckedCount(@NonNull String tag) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, false);
        int count = 0;
        for (int i = 0; i < tagStates.size(); i++) {
            byte state = tagStates.valueAt(i);
            if ((state & STATE_CHECKED) != 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the count of expanded items in a specific tag group
     * @param tag The tag group to count expanded items from
     * @return The number of expanded items in the tag group
     */
    public int getExpandedCount(@NonNull String tag) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, false);
        int count = 0;
        for (int i = 0; i < tagStates.size(); i++) {
            byte state = tagStates.valueAt(i);
            if ((state & STATE_EXPANDED) != 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets both checked and expanded counts for a tag group
     * @param tag The tag group to get counts from
     * @return int array where [0] is checked count and [1] is expanded count
     */
    @NonNull
    public int[] getStatesCounts(@NonNull String tag) {
        LongSparseArray<Byte> tagStates = getTagStates(tag, false);
        int[] counts = new int[2]; // [checked, expanded]

        for (int i = 0; i < tagStates.size(); i++) {
            byte state = tagStates.valueAt(i);
            if ((state & STATE_CHECKED) != 0) counts[0]++;
            if ((state & STATE_EXPANDED) != 0) counts[1]++;
        }

        return counts;
    }

    @NonNull
    public ItemState getItemState(@NonNull String tag, @NonNull String id) {
        byte state = ensureState(tag, id);
        return new ItemState(
                (state & STATE_CHECKED) != 0,
                (state & STATE_EXPANDED) != 0
        );
    }

    public void clear() {
        _map.clear();
    }
}
