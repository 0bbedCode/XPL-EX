package eu.faircode.xlua.x.xlua.database;

public enum ActionFlag {
    NONE(0),
    DELETE(1),
    UPDATE(2),
    PUSH(3),
    GET(4),
    APPLY(4),
    APPLY_CLEAN_OLD(5),

    FILLER(7);

    private final int value;
    ActionFlag(int value) { this.value = value; }
    public int getValue() { return value; }

    public static ActionFlag fromInt(int flags) {
        switch (flags) {
            case 1: return DELETE;
            case 2: return UPDATE;
            case 3: return PUSH;
            default: return NONE;
        }
    }

    public static boolean isDelete(int value) { return value == DELETE.value; }
    public static boolean isDelete(ActionFlag flag) { return flag == DELETE; }

    public static boolean hasFlag(int flags, ActionFlag flag) {
        return (flags & flag.getValue()) != 0;
    }

    public static int combineFlags(ActionFlag... flags) {
        int combined = 0;
        for (ActionFlag flag : flags) {
            combined |= flag.getValue(); // Bitwise OR to combine flags
        }
        return combined;
    }
}
