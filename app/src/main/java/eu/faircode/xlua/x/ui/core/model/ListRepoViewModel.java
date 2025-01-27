package eu.faircode.xlua.x.ui.core.model;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.interfaces.IRepositoryContainer;
import eu.faircode.xlua.x.xlua.repos.IXLuaRepo;
import kotlin.Pair;
import kotlin.Triple;

public class ListRepoViewModel<TElement> extends ListPreferenceViewModel<TElement> implements IRepositoryContainer<TElement> {

    protected IXLuaRepo<TElement> repository;

    private static final String TAG = "XLua.ListRepoViewModel";

    public ListRepoViewModel(Application application, String tag) {
        super(application, tag);
    }

    public ListRepoViewModel(Application application, String tag, IXLuaRepo<TElement> repository) {
        super(application, tag);
        this.repository = repository;
    }

    @Override
    protected List<TElement> filterData(Triple<Pair<String, List<String>>, Pair<String, Boolean>, Long> params, Application application) {
        if(!hasContext() || repository == null) {
            Log.e(TAG, "Repo View Model Does not have Context or Repository!");
            return new ArrayList<>();
        }

        Pair<String, List<String>> filter = params.getFirst();
        Pair<String, Boolean> searchParams = params.getSecond();
        return repository.filterAndSort(
                repository.get(application, getUserContext()),
                FilterRequest
                    .create()
                    .setQuery(searchParams.getFirst())          //Query Item to Search For
                    .setIsReversed(searchParams.getSecond())    //Is Reversed Flags
                    .setOrder(filter.getFirst())                //Order Items by ...
                    .setFilterTags(filter.getSecond()));        //List of the Tags for a more of a Filter, like Chips
    }

    @Override
    public void setRepository(IXLuaRepo<TElement> repository) {
        this.repository = repository;
    }

    @Override
    public IXLuaRepo<TElement> getRepository() {
        return this.repository;
    }

    @Override
    public PrefManager getPreferences() {
        return this.getPreferencesManger();
    }
}
