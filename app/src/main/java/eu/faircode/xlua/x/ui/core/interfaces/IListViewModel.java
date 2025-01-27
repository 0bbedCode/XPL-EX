package eu.faircode.xlua.x.ui.core.interfaces;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.model.ListBaseViewModel;
import eu.faircode.xlua.x.ui.core.util.ListFragmentUtils;
import kotlin.Pair;

public interface IListViewModel<TElement>  {
    void refresh();
    void updateList(FilterRequest request);
    LiveData<List<TElement>> getRawLiveData();
    void updateList(Pair<String, List<String>> filter, String query, boolean isReversed);
    IUserContext getAsUserContext();
    AndroidViewModel getAsViewModel();
}
