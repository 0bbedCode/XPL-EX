package eu.faircode.xlua.x.ui.core.model;

import android.app.Application;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.ui.core.CoreUiGlobals;

/**
 * ToDo: Work more off of Filters / Criteria. I want to make a system that resolved the chips as these would mostly be bind to chips
 * @param <TElement>
 */
public abstract class ListPreferenceViewModel<TElement> extends ListBaseViewModel<TElement> {
    private static final String TAG = "XLua.ListPreferenceViewModel";

    private PrefManager preferences;

    public PrefManager getPreferencesManger() { return preferences; }

    //Do something with filters
    public ListPreferenceViewModel(Application application, String tag) {
        super(application, tag);
        preferences = PrefManager.create(application, tag);
    }

    @Override
    protected boolean isReversed() {
        return PrefManager.getBoolean(preferences, CoreUiGlobals.PREFERENCE_IS_REVERSED, CoreUiGlobals.PREFERENCE_IS_REVERSED_DEFAULT); }

    @Override
    protected String getOrder() {
        return PrefManager.getString(preferences, CoreUiGlobals.PREFERENCE_ORDER, CoreUiGlobals.PREFERENCE_ORDER_DEFAULT, true); }

    @Override
    protected List<String> getFilters() {
        return Collections.emptyList(); }
}
