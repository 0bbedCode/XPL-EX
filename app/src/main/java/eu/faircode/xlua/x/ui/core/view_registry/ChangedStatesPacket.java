package eu.faircode.xlua.x.ui.core.view_registry;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;

public class ChangedStatesPacket {
    public String changedGroup;

    public int count = 0;
    public boolean isEnabled;
    public List<IStateChanged> changed;

    public boolean isEmpty() { return count == 0; }
    public boolean isFrom(String validIfGroup) { return count != 0 && (validIfGroup == null || changedGroup.equalsIgnoreCase(validIfGroup)); }

    public ChangedStatesPacket() { }

    public static ChangedStatesPacket create(String changedGroup, boolean isEnabled, List<IStateChanged> changed) {
        ChangedStatesPacket packet = new ChangedStatesPacket();
        packet.changedGroup = changedGroup;
        packet.count = ListUtil.size(changed);
        packet.changed = changed;
        packet.isEnabled = isEnabled;
        return packet;
    }

    public static ChangedStatesPacket create(String changedGroup, boolean isEnabled, IStateChanged... changed) {
        ChangedStatesPacket packet = new ChangedStatesPacket();
        packet.changedGroup = changedGroup;
        packet.changed = List.of(changed);
        packet.count = ListUtil.size(packet.changed);
        packet.isEnabled = isEnabled;
        return packet;
    }

    public static ChangedStatesPacket create(String changedGroup, boolean isEnabled, IStateChanged changed) {
        ChangedStatesPacket packet = new ChangedStatesPacket();
        packet.changedGroup = changedGroup;
        packet.count = 1;
        packet.changed = ListUtil.toSingleList(changed);
        packet.isEnabled = isEnabled;
        return packet;
    }

    public static ChangedStatesPacket create(String changedGroup, boolean isEnabled, int changeCount) {
        ChangedStatesPacket packet = new ChangedStatesPacket();
        packet.changedGroup = changedGroup;
        packet.count = changeCount;
        packet.changed = new ArrayList<>();
        packet.isEnabled = isEnabled;
        return packet;
    }

    public static ChangedStatesPacket create(String changedGroup) {
        ChangedStatesPacket packet = new ChangedStatesPacket();
        packet.changedGroup = changedGroup;
        packet.count = 1;
        packet.changed = new ArrayList<>();
        packet.isEnabled = false;
        return packet;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create().ensureOneNewLinePer(true)
                .appendFieldLine("Changed Group", this.changedGroup)
                .appendFieldLine("Count", this.count)
                .appendFieldLine("Count List", ListUtil.size(this.changed))
                .appendFieldLine("Is Enabled", this.isEnabled)
                .toString(true);
    }
}
