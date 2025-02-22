package eu.faircode.xlua.x.file;

public class UnixAccessControl {
    public final ModePermission ownerMode;
    public final ModePermission groupMode;
    public final ModePermission otherMode;
    public final UnixUser owner;
    public final UnixUser group;

    public int getMode() { return FileUtils.getModeValue(ownerMode, groupMode, otherMode); }

    public int getOwnerId() { return owner.getUid(); }
    public String getOwnerName() { return owner.getName(); }

    public int getGroupId() { return group.getUid(); }
    public String getGroupName() { return group.getName(); }

    UnixAccessControl(UnixAccessBuilder builder) {
        this.ownerMode = builder.getOwnerMode();
        this.groupMode = builder.getGroupMode();
        this.otherMode = builder.getOtherMode();
        this.owner = UnixUser.create(builder.getOwnerUid(), builder.getOwnerName());
        this.group = UnixUser.create(builder.getGroupUid(), builder.getGroupName());
    }
}
