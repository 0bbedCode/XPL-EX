package eu.faircode.xlua.x.ui.core.interfaces;

import eu.faircode.xlua.x.ui.core.model.ListBaseViewModel;

public interface IListViewModelController<TElement> {
    boolean hasViewModel();//Expand

    IListViewModel<TElement> getViewModel();
    void setViewModel(IListViewModel<TElement> viewModel);
    <TViewModel extends ListBaseViewModel<TElement>> void createViewModel(Class<TViewModel> classModel, boolean setUserContext);
}
