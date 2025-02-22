package eu.faircode.xlua.x.file;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import eu.faircode.xlua.x.Str;

public class UnixUser {
    public static UnixUser create(UnixUserId uid) { return new UnixUser(uid); }
    public static UnixUser create(UnixUserId uid, String name) { return new UnixUser(uid, name); }
    public static UnixUser create(int uid, String name) { return new UnixUser(uid, name); }
    public static UnixUser create(int uid) { return new UnixUser(uid); }

    private final int uid;
    private String name;

    public UnixUser(UnixUserId uid) { this(uid.getValue()); }
    public UnixUser(UnixUserId uid, String name) { this(uid.getValue(), name); }
    public UnixUser(int uid) { this.uid = uid; }
    public UnixUser(int uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public int getUid() { return uid; }
    public UnixUserId getUserType() { return UnixUserId.fromValue(uid); }
    public String getName() {
        if(Str.isEmpty(name)) this.name = UnixUserUtils.getName(uid);
        return this.name;
    }

    @NonNull
    @Override
    public String toString() { return "uid=" + uid + " name=" + getName(); }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof Integer)
            return uid == (int)obj;
        if(obj instanceof String) {
            String s = (String) obj;
            return s.equalsIgnoreCase(getName()) || s.equals(String.valueOf(uid));
        }
        if(obj instanceof UnixUser) {
            UnixUser o = (UnixUser) obj;
            return uid == o.uid || (!Str.isEmpty(getName()) && !Str.isEmpty(o.getName()) && getName().equalsIgnoreCase(o.getName()));
        }

        return false;
    }
}
