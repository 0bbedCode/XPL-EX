package eu.faircode.xlua.x.ui.core.acitivty;

import android.util.Log;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import eu.faircode.xlua.ActivityBase;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.interfaces.IFragmentController;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.test.EventTrigger;
import eu.faircode.xlua.x.xlua.settings.test.SharedViewControl;
import eu.faircode.xlua.x.xlua.settings.test.interfaces.IUIViewControl;

public abstract class ListBaseActivity extends ActivityBase implements IFragmentController, IUIViewControl {
    private static final String TAG = LibUtil.generateTag(ListBaseActivity.class);

    private IFragmentController controller;
    private MenuItem menuSearch;
    private SharedViewControl sharedViewControl;

    protected abstract String getSharedTagId();

    public <T extends IFragmentController> void startFragmentTransaction(Class<T> loaderClass, int replaceResource, boolean passArgs) {
        setLoaderFragment(loaderClass, passArgs);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(replaceResource, controller.getFragment());
        fragmentTransaction.commit();
    }

    public <T extends IFragmentController> void setLoaderFragment(Class<T> loaderClass, boolean passArgs) {
        try {
            controller = loaderClass.newInstance();
            if(passArgs) {
                Fragment frag = controller.getFragment();
                frag.setArguments(getIntent().getExtras());
                if(frag instanceof IUIViewControl) {
                    try {
                        IUIViewControl cast = (IUIViewControl) frag;
                        SharedViewControl svc = getSharedViewControl();
                        svc.setFragment(frag);
                        cast.setSharedViewControl(svc);
                    }catch (Exception e) {
                        Log.e(TAG, "Failed to Bind Shared View Controller! Error=" + e);
                    }
                }
                //? this is it ?
            }

            if(menuSearch != null)
                CoreUiUtils.bindMenuSearch(menuSearch, controller);

        }catch (Exception e) {
            Log.e(TAG, "Failed to Create Instance of Fragment Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
        }
    }

    public void bindMenuSearch(MenuItem menuSearch) {
        if(this.menuSearch == null) {
            this.menuSearch = menuSearch;
            if(controller != null) {
                CoreUiUtils.bindMenuSearch(menuSearch, controller);
                this.menuSearch = null;
            }
        }
    }

    @Override
    public void clear() { controller.clear(); }

    @Override
    public void refresh() {
        //controller.refresh();
        //Ohhh ?
        controller.refresh();
    }

    @Override
    public void updatedSortedList(FilterRequest request) {
        controller.updatedSortedList(request);
    }

    @Override
    public Fragment getFragment() { return controller.getFragment(); }

    @Override
    public FragmentManager getFragmentMan() { return controller.getFragmentMan(); }

    @Override
    public IFragmentController getController() {
        return controller;
    }

    @Override
    public void setController(IFragmentController controller) { this.controller = controller; }


    @Override
    public SharedViewControl getSharedViewControl() {
        if(this.sharedViewControl == null) this.sharedViewControl = new SharedViewControl().setTag(getSharedTagId()).setActivity(this);
        return this.sharedViewControl;
    }

    @Override
    public void setSharedViewControl(SharedViewControl sharedViewControl) { }

    @Override
    public void onEvent(EventTrigger event) {
        IUIViewControl.super.onEvent(event);
    }
}
