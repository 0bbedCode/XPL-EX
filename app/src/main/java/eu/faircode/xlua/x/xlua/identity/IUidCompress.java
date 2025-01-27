package eu.faircode.xlua.x.xlua.identity;

import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;

public interface IUidCompress extends ICursorType, IIdentifiableObject {
    String getId();     //Move this
    String getCategory();
    int getUid();
}
