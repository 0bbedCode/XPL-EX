package eu.faircode.xlua.x.ui.core.interfaces;

import eu.faircode.xlua.x.ui.adapters.ObjectState;
import eu.faircode.xlua.x.ui.adapters.ExpandableStateFactory;

public interface IBindingView {
    String getId();
    void expand();
    void unExpand();

    void check();
    void unCheck();


    //boolean isChecked()


}
