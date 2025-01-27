package eu.faircode.xlua.x.ui.core.util;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ListAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.fragment.ListFragment;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;
import eu.faircode.xlua.x.ui.core.interfaces.IListFragment;
import eu.faircode.xlua.x.ui.core.interfaces.IListViewModel;
import eu.faircode.xlua.x.ui.core.interfaces.IUserContext;
import eu.faircode.xlua.x.ui.core.model.ListBaseViewModel;

public class ListFragmentUtils  {
    private static final String TAG = "XLua.ListFragmentUtils";
    private static final Map<IListFragment<? extends IDiffFace, ? extends ViewBinding>, Observer<?>> observerMapMain = new WeakHashMap<>();


    public static FragmentManager getManager(Fragment fragment) {
        if(fragment == null)
            return null;

        //fragment::getParentFragmentManager
        return ObjectUtils.tryInvokeFirstNonNull(
                fragment::getFragmentManager,
                () -> fragment.requireActivity().getSupportFragmentManager());
    }


    @SuppressWarnings("unchecked")
    public static <
            TElement extends IDiffFace,
            TElementBinding extends ViewBinding,
            TFragment extends Fragment & IListFragment<TElement, TElementBinding>>
    void setRecyclerViewDataObservingState(TFragment holder, boolean startObserving) {
        try {
            if (startObserving) {
                LiveData<List<TElement>> liveData = holder.getLiveData();
                if (holder.isObserving() || liveData.hasObservers()) {
                    return;
                }

                Observer<List<TElement>> observer = new Observer<List<TElement>>() {
                    @Override
                    public void onChanged(List<TElement> elements) {
                        //ListGenericAdapter<TElement, ?, ?> adapter = holder.getAdapter();
                        //Get it as Adapter Object
                        ListAdapter<TElement, ?> adapter = holder.getAdapter().getAsListAdapterUnsafe();
                        if (adapter != null) {
                            adapter.submitList(elements);
                            holder.setIsRefreshing(false);
                            if (elements != null) {
                                holder.onDataChanged(elements);
                            }
                        }
                    }
                };

                liveData.observe(holder.getViewLifecycleOwner(), observer);
                observerMapMain.put(holder, observer);
                holder.setIsObserving(true);
            } else {
                if (!holder.isObserving()) {
                    return;
                }

                LiveData<List<TElement>> liveData = holder.getLiveData();
                Observer<List<TElement>> observer = (Observer<List<TElement>>) observerMapMain.remove(holder);
                if (observer != null) {
                    liveData.removeObserver(observer);
                }
                holder.setIsObserving(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error managing observer state: " + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
        }
    }


    public static <TElement extends IDiffFace, TElementBinding extends ViewBinding, T extends Fragment & IListFragment<TElement, TElementBinding>>
    void attachSwipeRefreshLayout(T holder, SwipeRefreshLayout swipeRefresh, int colorAccent) {
        try {
            if (swipeRefresh != null) {
                holder.setSwipeRefreshLayout(swipeRefresh);
                swipeRefresh.setColorSchemeColors(colorAccent, colorAccent, colorAccent);
                swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        holder.refresh();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error attaching SwipeRefreshLayout: " + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
        }
    }

    public static <TElement extends IDiffFace, TElementBinding extends ViewBinding,
            TFragment extends Fragment & IListFragment<TElement, TElementBinding>>
    void setIsRefreshing(TFragment holder, boolean isRefreshing) {
        try {
            SwipeRefreshLayout swipeRefresh = holder.getSwipeRefreshLayout();
            ProgressBar progressBar = holder.getProgressBar();
            if(swipeRefresh != null) swipeRefresh.setRefreshing(isRefreshing);
            if(progressBar != null) progressBar.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "Error setting refresh state: " + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
        }
    }

    public static <TElement extends IDiffFace, TElementBinding extends ViewBinding, TViewModel extends ListBaseViewModel<TElement>>
    void attachViewModel(
            IListFragment<TElement, TElementBinding> holder,
            Class<TViewModel> classModel,
            boolean setUserContext) {

        try {
            Fragment fragment = (Fragment)holder;
            TViewModel model = new ViewModelProvider(fragment).get(classModel);
            IListViewModel<TElement> viewModel = (IListViewModel<TElement>)model;
            if(setUserContext) {
                holder.ensureHasUserContext(false);  // Use the interface method
                IUserContext iCX = viewModel.getAsUserContext();
                iCX.setUserContext(holder.getUserContext());
            }

            holder.setViewModel(viewModel);
        } catch (Exception e) {
            Log.e(TAG, "Error Binding View Model, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
        }
    }

    public static <T extends ListFragment<?, ?, ?>> T newInstance(Class<T> fragmentClass, UserClientAppContext context) {
        try {
            T fragment = fragmentClass.newInstance();
            if (context != null) fragment.setArguments(context.toBundle());
            return fragment;
        } catch (Exception e) {
            Log.e(TAG, "Error Creating Fragment Instance then Setting the Context! Type=" + fragmentClass + " User Context=" + Str.toStringOrNull(context) + " Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return null;
        }
    }
}
