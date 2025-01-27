package eu.faircode.xlua.x.xlua.settings;

public enum NameInformationKind {
    UNKNOWN(-1),
    SINGLE_NO_PARENT(0),
    CHILD_HAS_PARENT(1),
    PARENT_HAS_CHILDREN_IS_CONTAINER(2);

    private final int value;
    NameInformationKind(int value) { this.value = value; }
    public int getValue() { return value; }

    public static NameInformationKind fromNamedInformation(NameInformation namedInformation) {
        if(namedInformation.hasChildren())
            return  PARENT_HAS_CHILDREN_IS_CONTAINER;

        if(namedInformation.endsWithNumber)
            return CHILD_HAS_PARENT;

        return SINGLE_NO_PARENT;
    }
}