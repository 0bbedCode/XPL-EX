package eu.faircode.xlua.x.xlua.settings.test.interfaces;

import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.settings.test.EventTrigger;
import eu.faircode.xlua.x.xlua.settings.test.SharedViewControl;

public interface IUIViewControl extends IIdentifiableObject {
    //String getId();
    //SharedSpace getShared();
    //void setShared(SharedSpace shared);

    SharedViewControl getSharedViewControl();
    void setSharedViewControl(SharedViewControl viewControl);

    default void onEvent(EventTrigger event) { }
    default void onView() {  }
    default void onClean() { }

    default boolean isView(String id) {
        return true;
    }
}
