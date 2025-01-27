package eu.faircode.xlua.x.ui.core.adapter;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import eu.faircode.xlua.x.ui.adapters.ObjectState;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;
import eu.faircode.xlua.x.ui.core.interfaces.IGenericElementEvent;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;
import eu.faircode.xlua.x.ui.core.model.ListAdapterItemViewHolder;

/**
 * We handle most of the View Holder Actions here, the Inherit Class still needs to override onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
 * If you want to handle how the either UnBind or Bind Object Process works, you can override "onBindViewHolder" or "onViewRecycled"
 * Parent Class will Create the base outline for the View Holder, as far as Binding Object, Binding View that all will be handled here
 *
 * We use to Set Field "IBindingInflater<TBinding> bindingInflater" but since either way the Parent Class that inherits this will have overrides I removed it
 * It was used to Inflate the View without knowing the actual view / binding but using Template Type
 * To create that interface all you would do it "TypeView::inflate" it will use the "inflate" function from that Type as a Delegate for the Interface
 * Then in this class construct "ListAdapterItemViewHolder<TElement, TBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType)"
 * Within the Constructor do something like:
 *          TBinding binding = bindingInflater.inflate(LayoutInflater.from(parent.getContext()), parent, false);
 *          return new ListAdapterItemViewHolder<>(binding, null, events, requestOptions);
 *
 *  Main issue as well is creating a Instance of the Actual View Holder, unless assumed Generic
 *
 * @param <TElement> List Element
 * @param <TBinding> List Element View Bind
 * @param <TElementViewHolder>  List Element View Holder, it will extend ListAdapterItemViewHolder, you are to call / setup "onCreateViewHolder"
 */
public abstract class
        ListGenericAdapter<TElement extends IDiffFace, TBinding extends ViewBinding, TElementViewHolder extends ListAdapterItemViewHolder<TElement, TBinding>>
        extends
        ListAdapter<TElement, TElementViewHolder> {

    private final RequestOptions requestOptions;
    private final IGenericElementEvent<TElement, TBinding> events;
    private IStateManager stateManager;

    public IGenericElementEvent<TElement, TBinding> getEvents() { return events; }
    public RequestOptions getRequestOptions() { return requestOptions; }
    public IStateManager getStateManager() { return stateManager; }

    public ListGenericAdapter(Context context, IGenericElementEvent<TElement, TBinding> events, IStateManager stateManager) {
        super(createDiffCallback());
        this.events = events;
        this.requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).override(100);
        this.stateManager = stateManager;
    }

    public ListGenericAdapter(Context context, IGenericElementEvent<TElement, TBinding> events) {
        super(createDiffCallback());
        this.events = events;
        this.requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).override(100);
    }

    @Override
    public void onBindViewHolder(@NonNull TElementViewHolder holder, int position) { holder.bindObjectNonNull(getItem(position)); }

    @Override
    public void onViewRecycled(@NonNull TElementViewHolder holder) {
        super.onViewRecycled(holder);
        //holder.onViewRecycled(); ToDo
    }

    private static <T extends IDiffFace> DiffUtil.ItemCallback<T> createDiffCallback() {
        return new DiffUtil.ItemCallback<T>() {
            @Override
            public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) { return oldItem.areItemsTheSame(newItem); }
            @Override
            public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) { return oldItem.areContentsTheSame(newItem); }
        };
    }
}

