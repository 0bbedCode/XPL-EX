package eu.faircode.xlua.x.file;


import androidx.annotation.NonNull;

import eu.faircode.xlua.x.data.string.StrBuilder;

public class ChmodModeBuilder {
    public static ChmodModeBuilder create() {
        return new ChmodModeBuilder();
    }

    private int mOwnerPermissions = 0;
    private int mGroupPermissions = 0;
    private int mOtherPermissions = 0;
    private String owner; // Owner as a string (e.g., "u0_a203")
    private String group;
    private int uid;      // UID as an integer
    private int guid;

    public ChmodModeBuilder addOwnerPermissions(ModePermission permissions) {
        this.mOwnerPermissions = addIfNotExist(this.mOwnerPermissions, permissions);
        return this;
    }

    public ChmodModeBuilder setOwnerPermissions(ModePermission permissions) {
        this.mOwnerPermissions = permissions.getValue();
        return this;
    }

    public ChmodModeBuilder addGroupPermissions(ModePermission permissions) {
        this.mGroupPermissions = addIfNotExist(this.mGroupPermissions, permissions);
        return this;
    }

    public ChmodModeBuilder setGroupPermissions(ModePermission permissions) {
        this.mGroupPermissions = permissions.getValue();
        return this;
    }

    public ChmodModeBuilder addOtherPermissions(ModePermission permissions) {
        this.mOtherPermissions = addIfNotExist(this.mOtherPermissions, permissions);
        return this;
    }

    public ChmodModeBuilder setOtherPermissions(ModePermission permissions) {
        this.mOtherPermissions = permissions.getValue();
        return this;
    }


    public ChmodModeBuilder setGroup(String group) {
        this.group = group;
        return this;
    }

    public ChmodModeBuilder setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public String getGroup() {
        return group;
    }

    public ChmodModeBuilder setGuid(int guid) {
        this.guid = guid;
        return this;
    }

    public ChmodModeBuilder setUid(int uid) {
        this.uid = uid;
        return this;
    }

    public int getUid() {
        return uid;
    }

    public int getGuid() {
        return guid;
    }

    public int getMode() {
        return (mOwnerPermissions * 100) + (mGroupPermissions * 10) + mOtherPermissions;
    }

    private int addIfNotExist(int old, ModePermission newAdd) {
        if ((old & newAdd.getValue()) == 0) old += newAdd.getValue();
        return old;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Owner", this.owner)
                .appendFieldLine("Owner UID", this.uid)
                .appendFieldLine("Owner Permissions", this.mOwnerPermissions)
                .appendFieldLine("Owner Permissions Name", ModePermission.fromValue(this.mOwnerPermissions).name())
                .appendFieldLine("Group", this.group)
                .appendFieldLine("Group UID", this.guid)
                .appendFieldLine("Group Permissions", this.mGroupPermissions)
                .appendFieldLine("Group Permissions Name", ModePermission.fromValue(this.mGroupPermissions).name())
                .appendFieldLine("Other Permissions", this.mOtherPermissions)
                .appendFieldLine("Other Permissions Name", ModePermission.fromValue(this.mOtherPermissions).name())
                .toString(true);
    }
}
