package eu.faircode.xlua.x.ui.core.util;

import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;

public class UiLog {
    private static final String TAG = "XLua.UiLog";


    public static boolean ensureNotGlobal(UserClientAppContext appContext) { return ensureNotGlobal(null, appContext); }
    public static boolean ensureNotGlobal(View view, UserClientAppContext appContext) {
        if(appContext == null) {
            if(view != null)
                Snackbar.make(view, view.getResources().getString(R.string.msg_error_generic_null), Snackbar.LENGTH_LONG).show();

            Log.e(TAG, "[ensureNotGlobal] App Context is Null! Stack=" + RuntimeUtils.getStackTraceSafeString());
            return true;
        }

        if(appContext.isGlobal()) {
            if(view != null)
                Snackbar.make(view, view.getResources().getString(R.string.msg_error_global_limit), Snackbar.LENGTH_LONG).show();

            return true;
        }

        return false;
    }

    public static boolean ensureHasDataContainer(SettingsContainer container) { return ensureHasDataContainer(null, container); }
    public static boolean ensureHasDataContainer(View view, SettingsContainer container) {
        if(container == null) {
            if(view != null)
                Snackbar.make(view, view.getResources().getString(R.string.msg_error_generic_null), Snackbar.LENGTH_LONG).show();

            Log.e(TAG, "[ensureHasDataContainer] Setting Container is Null! Stack=" + RuntimeUtils.getStackTraceSafeString());
            return true;
        }


        if(container.data.total < 1) {
            if(view != null)
                Snackbar.make(view, view.getResources().getString(R.string.msg_error_no_hooks), Snackbar.LENGTH_LONG).show();

            return true;
        }

        return false;
    }
}
