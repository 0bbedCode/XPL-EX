package eu.faircode.xlua.x.ui.adapters;

public enum ObjectState {
    STATE_TRUE(1),
    STATE_FALSE(2),
    EXPANDED_UNKNOWN(3);

    private final int value;
    ObjectState(int value) { this.value = value; }
    public int getValue() { return value; }

    public static ObjectState of(Boolean boolObject) {
        if(boolObject == null) return EXPANDED_UNKNOWN;
        return boolObject ? STATE_TRUE : STATE_FALSE;
    }

    public boolean toBoolValue() { return this != EXPANDED_UNKNOWN && this == STATE_TRUE; }
    public boolean toBoolValue(boolean isValueUnknown) { return this == EXPANDED_UNKNOWN ? isValueUnknown : this == STATE_TRUE; }

    public ObjectState toOpposite() { return toOpposite(STATE_FALSE); }
    public ObjectState toOpposite(ObjectState ifUnknownValue) {
        return this == EXPANDED_UNKNOWN ? ifUnknownValue : this == STATE_TRUE ? STATE_FALSE : STATE_TRUE;
    }

}
