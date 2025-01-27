package eu.faircode.xlua.x.ui.core.adapter;

import androidx.recyclerview.widget.ListAdapter;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.List;

import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;
import eu.faircode.xlua.x.ui.core.interfaces.IGenericElementEvent;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;

public abstract class EnhancedListAdapter<TElement extends IDiffFace, TBinding extends ViewBinding, TViewHolder extends EnhancedListAdapter.BaseViewHolder<TElement, TBinding>>
        extends ListAdapter<TElement, TViewHolder> {

    protected final IGenericElementEvent<TElement, TBinding> events;
    protected final IStateManager stateManager;
    private final RecyclerView.RecycledViewPool sharedPool;

    public EnhancedListAdapter(Context context,
                               IGenericElementEvent<TElement, TBinding> events,
                               IStateManager stateManager,
                               RecyclerView.RecycledViewPool sharedPool) {
        super(new DiffUtil.ItemCallback<TElement>() {
            @Override
            public boolean areItemsTheSame(@NonNull TElement oldItem, @NonNull TElement newItem) {
                return oldItem.areItemsTheSame(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull TElement oldItem, @NonNull TElement newItem) {
                return oldItem.areContentsTheSame(newItem);
            }

            @Override
            public Object getChangePayload(@NonNull TElement oldItem, @NonNull TElement newItem) {
                return oldItem.getChangePayload(newItem);
            }
        });
        this.events = events;
        this.stateManager = stateManager;
        this.sharedPool = sharedPool;
        setHasStableIds(true);
    }

    @Override
    public void onBindViewHolder(@NonNull TViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onBindViewHolder(@NonNull TViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            holder.bind(getItem(position), payloads);
        }
    }

    @Override
    public long getItemId(int position) {
        TElement item = getItem(position);
        return item.hashCode(); // Override in implementation if you have better ID
    }

    public static abstract class BaseViewHolder<TElement extends IDiffFace, TBinding extends ViewBinding>
            extends RecyclerView.ViewHolder {

        protected TBinding binding;
        protected TElement currentItem;
        protected final IGenericElementEvent<TElement, TBinding> events;
        protected final IStateManager manager;
        protected final SharedRegistry sharedRegistry;

        public BaseViewHolder(TBinding binding,
                              IGenericElementEvent<TElement, TBinding> events,
                              IStateManager stateManager) {
            super(binding.getRoot());
            this.binding = binding;
            this.events = events;
            this.manager = stateManager;
            this.sharedRegistry = stateManager.getSharedRegistry();
        }

        public abstract void bind(TElement item);
        public abstract void bind(TElement item, List<Object> payloads);

        protected void onViewAttached() {}
        protected void onViewDetached() {}
    }
}