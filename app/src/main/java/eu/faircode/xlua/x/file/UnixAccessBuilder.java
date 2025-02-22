package eu.faircode.xlua.x.file;


import android.os.Process;

import androidx.annotation.NonNull;

import eu.faircode.xlua.x.data.string.StrBuilder;

public class UnixAccessBuilder {
    public static UnixAccessBuilder create() {
        return new UnixAccessBuilder();
    }

    private ModePermission ownerMode = ModePermission.NONE;
    private ModePermission groupMode = ModePermission.NONE;
    private ModePermission otherMode = ModePermission.NONE;
    private String ownerName;
    private String groupName;
    private int ownerUid;
    private int groupUid;

    public ModePermission getOwnerMode() { return ownerMode; }
    public ModePermission getGroupMode() { return groupMode; }
    public ModePermission getOtherMode() { return otherMode; }

    public UnixAccessBuilder addOwnerMode(ModePermission mode) { return setOwnerMode(FileUtils.combinePermissionModes(this.ownerMode, mode)); }
    public UnixAccessBuilder setOwnerMode(ModePermission mode) {
        this.ownerMode = mode;
        return this;
    }

    public UnixAccessBuilder addGroupMode(ModePermission mode) { return setGroupMode(FileUtils.combinePermissionModes(this.groupMode, mode)); }
    public UnixAccessBuilder setGroupMode(ModePermission mode) {
        this.groupMode = mode;
        return this;
    }

    public UnixAccessBuilder addOtherMode(ModePermission mode) { return setOtherMode(FileUtils.combinePermissionModes(this.otherMode, mode)); }
    public UnixAccessBuilder setOtherMode(ModePermission mode) {
        this.otherMode = mode;
        return this;
    }

    public UnixAccessBuilder setOwnerUid() { return setOwnerUid(Process.myUid()); }
    public UnixAccessBuilder setOwnerUid(UnixUserId owner) { return setOwnerUid(owner.getValue()); }
    public UnixAccessBuilder setOwnerUid(int ownerUid) {
        this.ownerUid = ownerUid;
        return this;
    }

    public int getOwnerUid() {
        return ownerUid;
    }
    public String getOwnerName() { return ownerName; }
    public UnixAccessBuilder setOwnerName(String owner) {
        this.ownerName = owner;
        return this;
    }

    public UnixAccessBuilder setGroupUid() { return setGroupUid(Process.myUid()); }
    public UnixAccessBuilder setGroupUid(UnixUserId group) { return setGroupUid(group.getValue()); }
    public UnixAccessBuilder setGroupUid(int groupUid) {
        this.groupUid = groupUid;
        return this;
    }

    public int getGroupUid() {
        return groupUid;
    }
    public String getGroupName() { return groupName; }
    public UnixAccessBuilder setGroupName(String group) {
        this.groupName = group;
        return this;
    }

    public UnixAccessControl build() { return new UnixAccessControl(this); }

    public int getMode() { return FileUtils.getModeValue(ownerMode, groupMode, otherMode); }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Owner", this.ownerName)
                .appendFieldLine("Owner UID", this.ownerUid)
                .appendFieldLine("Owner Permissions", this.ownerMode.getValue())
                .appendFieldLine("Owner Permissions Name", this.ownerMode.name())
                .appendFieldLine("Group", this.groupName)
                .appendFieldLine("Group UID", this.groupUid)
                .appendFieldLine("Group Permissions", this.groupMode.getValue())
                .appendFieldLine("Group Permissions Name", this.groupMode.name())
                .appendFieldLine("Other Permissions", this.otherMode.getValue())
                .appendFieldLine("Other Permissions Name", this.otherMode.name())
                .toString(true);
    }
}
