package eu.faircode.xlua.x.ui.core.interfaces;

import android.view.View;
import android.widget.CompoundButton;

import androidx.viewbinding.ViewBinding;

import eu.faircode.xlua.x.data.interfaces.IValidator;

public interface IGenericElementEvent<TElement extends IValidator, TBinding extends ViewBinding> {
    void onClick(IGenericViewHolder<TElement, TBinding> holder, View view);
    void onLongClick(IGenericViewHolder<TElement, TBinding> holder, View view);
    void onCheckChanged(IGenericViewHolder<TElement, TBinding> holder, CompoundButton compoundButton, boolean isChecked);

    void onBindFinished(IGenericViewHolder<TElement, TBinding> holder);
}
