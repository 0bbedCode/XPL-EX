package eu.faircode.xlua.x.ui.core.interfaces;

import androidx.viewbinding.ViewBinding;

import java.util.List;

public interface IListFragment<TElement extends IDiffFace, TElementBinding extends ViewBinding>
        extends IUserContext,
                IViewEventController,
                IDataObserver<TElement>,
                IFragmentController,
                IListRecyclerViewController<TElement, TElementBinding>,
                IListViewModelController<TElement> {

    //Move this into "IDataObserver" ?
    void onDataChanged(List<TElement> elements);

    /*LiveData<List<TElement>> getLiveData();

    ListGenericAdapter<TElement, ?, ?> getAdapter();

    boolean hasViewModel();
    IListViewModel<TElement> getViewModel();
    void setViewModel(IListViewModel<TElement> viewModel);
    <TViewModel extends ListBaseViewModel<TElement>> void createViewModel(Class<TViewModel> classModel, boolean setUserContext);

    void startObserver();
    void stopObserver();
    void setIsObserving(boolean isObserving);
    boolean isObserving();

    void setIsRefreshing(boolean isRefreshing);

    SwipeRefreshLayout getSwipeRefreshLayout();
    void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout);
    void initSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout, int refreshCircleColor);

    void setProgressBar(ProgressBar progressBar);
    ProgressBar getProgressBar();

    //ListGenericAdapter<TElement, TElementBinding, ?>

    void setAdapter(ListGenericAdapter<TElement, TElementBinding, ?> adapter);
    void setFloatingActionButtons(FloatingActionButton... floatingActionButtons);
    void initRecyclerView(RecyclerView recyclerView);

    void initFloatingActions(FloatingActionButton... floatingActionButtons)*/
}
