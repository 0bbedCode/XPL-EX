package eu.faircode.xlua.x.ui.core.interfaces;

import eu.faircode.xlua.x.data.interfaces.IValidator;

public interface IDiffFace extends IValidator {
    //String getId();
    boolean areItemsTheSame(IDiffFace newItem);
    boolean areContentsTheSame(IDiffFace newItem);
    default Object getChangePayload(IDiffFace newItem) {
        return null;
    }
}
