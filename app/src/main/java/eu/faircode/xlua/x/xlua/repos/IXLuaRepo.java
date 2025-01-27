package eu.faircode.xlua.x.xlua.repos;

import android.content.Context;

import java.util.List;

import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;

public interface IXLuaRepo<TElement> {
    List<TElement> get();
    List<TElement> get(Context context, UserClientAppContext userContext);
    List<TElement> filterAndSort(List<TElement> items, FilterRequest request);
}
