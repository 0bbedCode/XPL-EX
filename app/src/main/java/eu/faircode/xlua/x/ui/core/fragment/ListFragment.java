package eu.faircode.xlua.x.ui.core.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.core.DataEventKind;
import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.FloatingActionButtonContext;
import eu.faircode.xlua.x.ui.core.RecyclerViewWrapper;
import eu.faircode.xlua.x.ui.core.ViewEventController;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;
import eu.faircode.xlua.x.ui.core.interfaces.IFragmentController;
import eu.faircode.xlua.x.ui.core.interfaces.IListAdapter;
import eu.faircode.xlua.x.ui.core.interfaces.IListFragment;
import eu.faircode.xlua.x.ui.core.interfaces.IListViewModel;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.core.model.ListBaseViewModel;
import eu.faircode.xlua.x.ui.core.CoreUiLog;
import eu.faircode.xlua.x.ui.core.util.ListFragmentUtils;


/**
 * ToDo: Maybe make a Interface for TBinding binding
 *
 *     //protected IFragmentController controller;
 *     //From the Fragment we invoke some "search(request)" then it will invoke the ViewModel "filterData"
 *
 * @param <TElement>
 * @param <TBinding>
 */
public abstract class ListFragment<TElement extends IDiffFace, TBinding extends ViewBinding, TElementBinding extends ViewBinding>
        extends Fragment
        implements
            View.OnClickListener,
            View.OnLongClickListener,
            IListFragment<TElement, TElementBinding>, IStateManager {

    private static final String TAG = "XLua.ListFragment";
    private static final int TYPE_VIEW_BINDING_ARG_INDEX = 1;   //Set this to where the arg for the ViewBinding Type is in for Templates / Generics ListFragment<TElement(0), TBinding(1)..

    protected TBinding binding;

    private UserClientAppContext userContext;
    private boolean isObserving = false;
    private IListViewModel<TElement> viewModel;

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    protected final RecyclerViewWrapper<TElement, TElementBinding> recyclerViewWrapper = new RecyclerViewWrapper<>();
    private final ViewEventController viewEvents = ViewEventController.create(this, this);

    //protected SharedRegistry sharedRegistry = new SharedRegistry();

    //@Override
    //public SharedRegistry getSharedRegistry() { return sharedRegistry; }


    public ListFragment() { }

    protected abstract void dataEvent(DataEventKind kind, List<TElement> elements);


    @Nullable
    @Override
    @SuppressWarnings({ "unchecked", "ConstantConditions" } )
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ensureHasUserContext();
        //This will get the Templated Type from <T> as a Class<?>, index needs to be correct if more than one Generic
        Type superclass = getClass().getGenericSuperclass();
        Class<?> aClass = (Class<?>) ((ParameterizedType) superclass).getActualTypeArguments()[TYPE_VIEW_BINDING_ARG_INDEX];
        try {
            Method method = aClass.getDeclaredMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
            binding = (TBinding) method.invoke(null, inflater, container, false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inflate ViewBinding", e);
        }
        return binding.getRoot();
    }

    @Override
    public void onDataChanged(List<TElement> tElements) {  }

    public void initApplicationView(ImageView ivAppIcon, TextView tvAppName, TextView tvAppPackageName, TextView tvAppUid) { if(this.userContext != null) this.userContext.setImageViewTextViewTexts(requireContext(), ivAppIcon, tvAppName, tvAppPackageName, tvAppUid); }

    public TBinding setBinding(TBinding binding, boolean ensureUserContext) {
        if(binding != null) { if(ensureUserContext) this.ensureHasUserContext(); this.binding = binding; }
        return this.binding;
    }

    /*IDataObserver*/
    @Override
    public LiveData<List<TElement>> getLiveData() { CoreUiLog.logGettingLiveData(TAG); return viewModel.getRawLiveData(); }
    @Override
    public void startObserver() { ListFragmentUtils.setRecyclerViewDataObservingState(this, true); }
    @Override
    public void stopObserver() { ListFragmentUtils.setRecyclerViewDataObservingState(this, true); }
    @Override
    public void setIsObserving(boolean isObserving) { this.isObserving = isObserving; }
    @Override
    public boolean isObserving() { return this.isObserving; }

    @Override
    public void clear() {  }
    @Override
    public void refresh() {
        ensureUpdatedShared();
        viewModel.refresh();
    }
    @Override
    public void updatedSortedList(FilterRequest request) { this.viewModel.updateList(request); }

    /*IViewEventController*/
    @Override
    public void addViewsToEventController(View... views) { viewEvents.addViews(views); }
    @Override
    public void wire() { viewEvents.wire(); }
    @Override
    public void unWire() { viewEvents.unWire(); }
    @Override
    public ViewEventController getViewEventController() { return this.viewEvents; }

    public void ensureHasUserContext() { ensureHasUserContext(false); }
    public void ensureHasUserContext(boolean forceGlobalIfNullArgs) {
        if(this.userContext == null) {
            Bundle args = getArguments();
            if(forceGlobalIfNullArgs || args != null) {
                this.userContext = UserClientAppContext.fromBundle(requireContext(), getArguments());
                if(this.userContext != null)
                    ensureUpdatedShared();
            }
        }
    }

    /*IFragmentController*/
    @Override
    public Fragment getFragment() { return this; }
    @Override
    public FragmentManager getFragmentMan() { return ListFragmentUtils.getManager(this); }

    @Override
    public IFragmentController getController() { return this; }
    @Override
    public void setController(IFragmentController controller) {  }

    /*IUserContext*/
    @Override
    public boolean isGlobal() { return userContext != null && userContext.isGlobal(); }
    @Override
    public int getIcon() { return userContext == null ? 0 : userContext.icon; }
    @Override
    public int getUserId() { /*return userContext == null ? 0 : userContext.getProfileUserId();
        we should not be using this from user context
        Its NOT NEEDED the Server / Service Side resolves this
        Stop being fucking dumb thanks!
    */
        return 0;
    }

    @Override
    public String getAppName() { return userContext == null ? UserIdentityPacket.GLOBAL_NAMESPACE : userContext.appName; }
    @Override
    public String getAppPackageName() { return userContext == null ? UserIdentityPacket.GLOBAL_NAMESPACE : userContext.appPackageName; }
    @Override
    public int getAppUid() { return userContext == null ? UserIdentityPacket.GLOBAL_USER : userContext.appUid; }
    @Override
    public UserClientAppContext getUserContext() { return userContext; }
    @Override
    public void setUserContext(UserClientAppContext context) { this.userContext = context; }
    @Override
    public boolean hasContext() { return userContext != null && userContext.isValid(); }

    /*IListRecyclerViewController*/
    @Override
    public IListAdapter<TElement, ?> getAdapter() { return this.recyclerViewWrapper.getAdapter(); }
    @Override
    public void setAdapter(IListAdapter<TElement, TElementBinding> adapter) { if(adapter != null) this.recyclerViewWrapper.ensureAdapterIsLinked(adapter); }
    @Override
    public void setIsRefreshing(boolean isRefreshing) { ListFragmentUtils.setIsRefreshing(this, isRefreshing); }
    @Override
    public ProgressBar getProgressBar() { return this.progressBar; }
    @Override
    public void setProgressBar(ProgressBar progressBar) { if(progressBar != null) this.progressBar = progressBar; }
    @Override
    public SwipeRefreshLayout getSwipeRefreshLayout() { return this.swipeRefreshLayout; }
    @Override
    public void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) { if(swipeRefreshLayout != null) this.swipeRefreshLayout = swipeRefreshLayout; }
    @Override
    public void initSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout, int refreshCircleColor) { ListFragmentUtils.attachSwipeRefreshLayout(this, swipeRefreshLayout, refreshCircleColor); }
    @Override
    public void initFloatingActions(FloatingActionButton... floatingActionButtons) {
        if(ArrayUtils.isValid(floatingActionButtons))
            recyclerViewWrapper.ensureFloatingActionsAreLinked(
                    FloatingActionButtonContext.create().initAnimations(getContext())
                            .add(this, this, floatingActionButtons));
    }
    @Override
    public boolean isScrollable() { return recyclerViewWrapper.isScrollable(); }
    @Override
    public void initRecyclerView(RecyclerView recyclerView) {
        //recyclerViewWrapper.
        if(recyclerView != null) {
            recyclerViewWrapper
                    .setRecyclerView(recyclerView)
                    .ensureFloatingActionsAreLinked()
                    .setVisibility(true)
                    .useLinearLayoutManager(getActivity(), 4)  // Added prefetch count
                    .optimizeForPerformance()                  // New performance optimizations
                    .ensureAdapterIsLinked();
        }
    }

    /*IListViewModelController*/
    @Override
    public boolean hasViewModel() { return this.viewModel != null; }
    @Override
    public IListViewModel<TElement> getViewModel() { return this.viewModel; }
    @Override
    public void setViewModel(IListViewModel<TElement> viewModel) { if(viewModel != null) this.viewModel = viewModel; }
    @Override
    public <TViewModel extends ListBaseViewModel<TElement>> void createViewModel(Class<TViewModel> classModel, boolean setUserContext) { ListFragmentUtils.attachViewModel(this, classModel, setUserContext); }

    @Override
    public void onResume() {
        super.onResume();
        dataEvent(DataEventKind.ON_RESUME, ListUtil.ensureIsValidOrEmptyList(getLiveData().getValue()));
    }

    /*
        ToDo: To make even cooler, when "refreshed" we invoke a onEvent Event, then init all items ?
                Bind / Update stuff from Shared ? instead of handling it else where
     */
    private void ensureUpdatedShared() {
        SharedRegistry sharedRegistry = getSharedRegistry();
        if(sharedRegistry instanceof SettingSharedRegistry) {
            SettingSharedRegistry setShared = (SettingSharedRegistry) sharedRegistry;
            setShared.refresh(requireContext(), this.userContext.appUid, this.userContext.appPackageName);
        }
    }
}
