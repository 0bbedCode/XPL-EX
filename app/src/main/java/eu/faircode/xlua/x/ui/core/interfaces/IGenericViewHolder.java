package eu.faircode.xlua.x.ui.core.interfaces;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import eu.faircode.xlua.x.data.interfaces.IValidator;

public interface IGenericViewHolder<TElement extends IValidator, TBinding extends ViewBinding> {
    void wire();
    void unWire();

    void hide();
    void show();

    IGenericElementEvent<TElement, TBinding> getEvents();

    TElement getObject();
    TBinding getBinding();
}
