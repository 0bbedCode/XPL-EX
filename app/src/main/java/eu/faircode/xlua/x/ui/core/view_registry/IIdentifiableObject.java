package eu.faircode.xlua.x.ui.core.view_registry;

public interface IIdentifiableObject {
    String getId();
    void setId(String id);


    default boolean consumeId(Object o) { return false; }

    default String getCategory() {
        return null;
    }
}
