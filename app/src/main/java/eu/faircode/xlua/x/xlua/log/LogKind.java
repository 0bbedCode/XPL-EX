package eu.faircode.xlua.x.xlua.log;

public enum LogKind {
    NONE(0),
    USAGE(1),
    DEPLOYED(2),
    EXECUTED(3),
    UPDATE(5),

    ERROR_DB(666),
    ERROR_SERVICE(777),
    ERROR_CLIENT(898),
    ERROR_HOOK(999);

    private final int value;
    LogKind(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static String mappedDefinition(LogKind kind) {
        //ToDO:
        return null;
    }
}