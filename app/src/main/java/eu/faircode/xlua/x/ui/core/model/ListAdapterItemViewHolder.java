package eu.faircode.xlua.x.ui.core.model;

import android.view.View;
import android.widget.CompoundButton;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.request.RequestOptions;

import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.ui.core.interfaces.IGenericElementEvent;
import eu.faircode.xlua.x.ui.core.interfaces.IGenericViewHolder;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;


public class ListAdapterItemViewHolder<TElement extends IValidator, TBinding extends ViewBinding> extends RecyclerView.ViewHolder
        implements
        View.OnClickListener,
        View.OnLongClickListener,
        CompoundButton.OnCheckedChangeListener,
        IGenericViewHolder<TElement, TBinding> {

    protected IGenericElementEvent<TElement, TBinding> events;
    protected TBinding binding;
    protected TElement object;
    protected RequestOptions options;
    protected IStateManager stateManager;

    public boolean hasObject() { return object != null; }
    public boolean hasBinding() { return binding != null; }

    public static <TElement extends IValidator, TBinding extends  ViewBinding> ListAdapterItemViewHolder<TElement, TBinding> create(TBinding binding, TElement object, IGenericElementEvent<TElement, TBinding> onEvents, RequestOptions requestOptions) { return new ListAdapterItemViewHolder<>(binding, object, onEvents, requestOptions); }
    public static <TElement extends IValidator, TBinding extends  ViewBinding> ListAdapterItemViewHolder<TElement, TBinding> create(TBinding binding) { return new ListAdapterItemViewHolder<>(binding); }

    public ListAdapterItemViewHolder(TBinding binding) { super(binding.getRoot()); }

    public ListAdapterItemViewHolder(TBinding binding, IGenericElementEvent<TElement, TBinding> onEvents, RequestOptions requestOptions, IStateManager stateManager) {
        super(binding.getRoot());
        this.bindStateManager(stateManager);
        this.bindEvents(onEvents);
        this.bindRequestOptions(requestOptions);
        this.bindBinding(binding);
    }

    public ListAdapterItemViewHolder(TBinding binding, IGenericElementEvent<TElement, TBinding> onEvents, RequestOptions requestOptions) {
        super(binding.getRoot());
        this.bindEvents(onEvents);
        this.bindRequestOptions(requestOptions);
        this.bindBinding(binding);
    }

    public ListAdapterItemViewHolder(TBinding binding, TElement object, IGenericElementEvent<TElement, TBinding> onEvents, RequestOptions requestOptions) {
        super(binding.getRoot());
        this.bindRequestOptions(requestOptions);
        this.bindEvents(onEvents);
        this.bindBinding(binding);
        this.bindObject(object);
    }

    public void bindStateManager(IStateManager stateManager) { this.stateManager = stateManager; }
    public void bindStateManagerNonNull(IStateManager stateManager) { if(stateManager != null) this.stateManager = stateManager; }
    public void unBindStateManager() { this.stateManager = null; }

    public void bindObject(TElement object) { this.object = object; }
    public void bindObjectNonNull(TElement object) { if(object != null) bindObject(object); }
    public void unBindObject() { this.object = null; }

    public void bindBinding(TBinding binding) { this.binding = binding; }
    public void bindBindingNonNull(TBinding binding) {  if(binding != null) bindBinding(binding); }
    public void unBindBinding() { this.binding = null; }

    public void bindEvents(IGenericElementEvent<TElement, TBinding> event) { this.events = event; }
    public void bindEventsNonNull(IGenericElementEvent<TElement, TBinding> event) { if(event != null) bindEvents(event); }
    public void unBindEvents() { this.events = null; }

    public void bindRequestOptions(RequestOptions requestOptions) { this.options = requestOptions; }
    public void bindRequestOptionsNonNull(RequestOptions requestOptions) { if(requestOptions != null) bindRequestOptions(options); }

    @Override
    public void show() {  }
    @Override
    public void hide() {  }
    @Override
    public void wire() {  }
    @Override
    public void unWire() { }
    @Override
    public IGenericElementEvent<TElement, TBinding> getEvents() { return events; }
    @Override
    public TElement getObject() { return object; }
    @Override
    public TBinding getBinding() { return binding; }
    @Override
    public void onClick(View view) { if(events != null) events.onClick(this, view); }
    @Override
    public boolean onLongClick(View view) { if(events != null) events.onLongClick(this, view); return false; }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) { if(events != null) events.onCheckChanged(this, compoundButton, isChecked); }
}