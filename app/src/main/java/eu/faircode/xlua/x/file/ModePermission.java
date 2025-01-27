package eu.faircode.xlua.x.file;

public enum ModePermission {
    NONE(0),
    READ(4),
    WRITE(2),
    EXECUTE(1),

    EXECUTE_WRITE(3),    // WRITE + EXECUTE (2 + 1)
    READ_EXECUTE(5),     // READ + EXECUTE (4 + 1)
    READ_WRITE(6),       // READ + WRITE (4 + 2)
    READ_WRITE_EXECUTE(7); // READ + WRITE + EXECUTE (4 + 2 + 1)

    private final int value;

    ModePermission(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Get the corresponding ModePermission for the given value.
     *
     * @param value The integer value of the permission (0-7).
     * @return The corresponding ModePermission, or NONE if the value is invalid.
     */
    public static ModePermission fromValue(int value) {
        for (ModePermission permission : values()) {
            if (permission.value == value) {
                return permission;
            }
        }
        // Return NONE if no matching value is found
        return NONE;
    }
}
