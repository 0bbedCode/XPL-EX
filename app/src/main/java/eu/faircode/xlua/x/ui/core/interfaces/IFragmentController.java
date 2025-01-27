package eu.faircode.xlua.x.ui.core.interfaces;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import eu.faircode.xlua.x.ui.core.FilterRequest;

public interface IFragmentController {
    void clear();
    void refresh();
    void updatedSortedList(FilterRequest request);

    Fragment getFragment();
    FragmentManager getFragmentMan();

    IFragmentController getController();
    void setController(IFragmentController controller);
}
