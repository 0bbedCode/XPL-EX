package eu.faircode.xlua.x.xlua.database.sql;

public enum SnakeAction {
    ERROR(-1),
    RESOLVE(8),
    NONE(0),
    QUERY(1),
    DELETE(2),
    UPDATE(3),
    INSERT(4);
    private final int value;
    SnakeAction(int value) { this.value = value; }
    public int getValue() { return value; }
}
