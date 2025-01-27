package eu.faircode.xlua.x.ui.core.model;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.core.interfaces.IListViewModel;
import eu.faircode.xlua.x.ui.core.interfaces.IUserContext;
import kotlin.Pair;
import kotlin.Triple;

//Can this extend over ListFragment ??

public abstract class ListBaseViewModel<TElement> extends AndroidViewModel implements IUserContext, IListViewModel<TElement> {

    private static final String TAG = "XLua.ListBaseViewModel";

    //App
    //User
    //Repo ?
    //Above this uses "preferences"
    //Above this can also be one for "repos"

    protected String tag = "";
    protected final MutableLiveData<Triple<Pair<String, List<String>>, Pair<String, Boolean>, Long>> updateParams;
    protected final LiveData<List<TElement>> liveData;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());
    protected final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

    private UserClientAppContext userContext;

    @Override
    public LiveData<List<TElement>> getRawLiveData() {
        return this.liveData;
    }

    @Override
    public IUserContext getAsUserContext() { return this; }

    protected abstract boolean isReversed();
    protected abstract String getOrder();
    protected abstract List<String> getFilters();
    protected abstract List<TElement> filterData(Triple<Pair<String, List<String>>, Pair<String, Boolean>, Long> params, Application application);

    public ListBaseViewModel(Application application, String tag) {
        super(application);
        this.updateParams = new MutableLiveData<>(new Triple<>(new Pair<>(Str.EMPTY, new ArrayList<>()), new Pair<>(Str.EMPTY, false), 0L));
        this.liveData = setupLiveData(application);
        this.tag = tag;
        refresh();
    }

    @Override
    public void refresh() {
        updateParams.setValue(new Triple<>(
                new Pair<>(getOrder(), getFilters()),
                new Pair<>(Str.EMPTY, isReversed()),
                System.currentTimeMillis()));
    }

    @Override
    public void updateList(FilterRequest request) { updateList(new Pair<>(request.order, request.filterTags), request.query, request.isReversed); }

    @Override
    public void updateList(Pair<String, List<String>> filter, String query, boolean isReversed) {
        updateParams.setValue(
                new Triple<>(filter, new Pair<>(query, isReversed),
                System.currentTimeMillis()));
    }

    private LiveData<List<TElement>> setupLiveData(final Application application) {
        return Transformations.switchMap(updateParams, input -> Transformations.map(
                Transformations.distinctUntilChanged(
                        Transformations.switchMap(updateParams, params -> {
                            MutableLiveData<List<TElement>> result = new MutableLiveData<>();
                            backgroundExecutor.execute(() -> {
                                List<TElement> filteredData = filterData(params, application);
                                mainHandler.post(() -> result.setValue(filteredData));
                            });
                            return result;
                        })
                ),
                rawLiveData -> rawLiveData // You can add any additional transformations here if needed
        ));
    }

    @Override
    public AndroidViewModel getAsViewModel() { return this; }

    @Override
    public boolean isGlobal() { return userContext != null && userContext.isGlobal(); }

    @Override
    public int getIcon() { return userContext == null ? 0 : userContext.icon; }

    @Override
    public int getUserId() {
        //return userContext == null ? 0 : userContext.getProfileUserId();
        //Users dont fucking need this its server service side
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
    public void ensureHasUserContext(boolean useGlobalIfNull) { }

    @Override
    public void setUserContext(UserClientAppContext context) {
        if(context != null) {
            this.userContext = context;
        }
    }

    @Override
    public boolean hasContext() { return userContext != null; }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Tag", this.tag)
                .appendFieldLine("User Context", Str.toStringOrNull(this.userContext))
                .appendFieldLine("Live Data", Str.toStringOrNull(this.liveData))
                .toString(true);
    }
}
