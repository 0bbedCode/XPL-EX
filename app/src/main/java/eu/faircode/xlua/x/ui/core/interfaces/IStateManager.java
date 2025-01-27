package eu.faircode.xlua.x.ui.core.interfaces;

import androidx.fragment.app.FragmentManager;

import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;

public interface IStateManager {
    SharedRegistry getSharedRegistry();
    FragmentManager getFragmentMan();
}
