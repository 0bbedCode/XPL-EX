package eu.faircode.xlua.x.ui.core.interfaces;

import androidx.lifecycle.LiveData;

import java.util.List;

import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.adapter.ListGenericAdapter;

public interface IDataObserver<TElement extends IDiffFace> {
    void startObserver();
    void stopObserver();
    void setIsObserving(boolean isObserving);
    boolean isObserving();

    void refresh();
    void clear();
    void updatedSortedList(FilterRequest request);

    LiveData<List<TElement>> getLiveData();
}
