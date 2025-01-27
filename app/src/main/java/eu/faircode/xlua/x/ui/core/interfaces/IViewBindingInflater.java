package eu.faircode.xlua.x.ui.core.interfaces;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

public interface IViewBindingInflater<TBinding extends ViewBinding> {
    TBinding inflate(IBindingInflater<TBinding> inflate, LayoutInflater inflater, ViewGroup container, boolean ensureUserContext);
    TBinding setBinding(TBinding binding, boolean ensureUserContext);
}
