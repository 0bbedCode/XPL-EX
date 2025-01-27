package eu.faircode.xlua.x.ui.core.interfaces;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import eu.faircode.xlua.x.data.utils.ObjectUtils;

@SuppressWarnings("unchecked")
public interface IListAdapter<
        TElement extends IDiffFace,
        TBinding extends ViewBinding>
{

    default ListAdapter<TElement, ?> getAsListAdapterUnsafe() { return (ListAdapter<TElement, ?>) (Object)this; }

    @Nullable
    default ListAdapter<TElement, ?> getAsListAdapterOrNull() { return ObjectUtils.tryCast(this); }
}
