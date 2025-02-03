package eu.faircode.xlua.x.ui.core.interfaces;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.settings.test.SharedViewControl;

public interface IStateManager {
    SharedViewControl getSharedViewControl();
    SharedRegistry getSharedRegistry();
    FragmentManager getFragmentMan();
    Fragment getAsFragment();
}
