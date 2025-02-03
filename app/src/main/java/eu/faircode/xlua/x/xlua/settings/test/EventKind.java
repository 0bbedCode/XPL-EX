package eu.faircode.xlua.x.xlua.settings.test;

import eu.faircode.xlua.x.xlua.settings.NameInformation;
import eu.faircode.xlua.x.xlua.settings.NameInformationKind;

public enum EventKind {
    UNKNOWN(-1),
    UPDATE(0),
    PUSH(1),
    CHECK(2),

    REMOVE(2);

    private final int value;
    EventKind(int value) { this.value = value; }
    public int getValue() { return value; }
}
