package eu.faircode.xlua.x.ui.core.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.FutureTask;

import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.NumericUtils;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.ui.core.FilterRequest;
import eu.faircode.xlua.x.ui.core.interfaces.IFragmentController;
import eu.faircode.xlua.x.ui.core.view_registry.ChangedStatesPacket;
import eu.faircode.xlua.x.ui.core.view_registry.IStateChanged;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.ui.core.view_registry.SettingSharedRegistry;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.hook.data.AssignmentData;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.interfaces.NameInformationTypeBase;

public class CoreUiUtils {
    public static final int CIRCLE_DIAMETER = 64;
    public static final DynamicField FIELD_SPINNER_END_OFFSET = DynamicField.create(SwipeRefreshLayout.class, "mSpinnerOffsetEnd")
            .setAccessible(true);

    public static final String TAG = "XLua.CoreUiUtils";


    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static String getInputTextText(EditText editText) { return getInputTextText(editText, null, false); }
    public static String getInputTextText(EditText editText, String defaultText) { return getInputTextText(editText, defaultText, false); }
    public static String getInputTextText(EditText editText, String defaultText, boolean checkWindowTokenAttached) {
        if (editText == null) return defaultText;
        try {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                // Use a FutureTask to retrieve the result synchronously
                FutureTask<String> futureTask = new FutureTask<>(() -> {
                    if (!checkWindowTokenAttached || editText.getWindowToken() != null) { // Check if view is still attached
                        Editable e = editText.getText();
                        return e == null ? defaultText : e.toString();
                    } else {
                        throw new Exception("Window Token did not check out");
                    }
                });
                new Handler(Looper.getMainLooper()).post(futureTask);
                return futureTask.get(); // Wait for the result
            } else {
                if (!checkWindowTokenAttached || editText.getWindowToken() != null) { // Check if view is still attached
                    Editable e = editText.getText();
                    return e == null ? defaultText : e.toString();
                } else {
                    throw new Exception("Window Token did not check out");
                }
            }
        } catch (Exception ignored) {
            return defaultText;
        }
    }



    public static String getInputTextText(TextInputEditText inputEditText) { return getInputTextText(inputEditText, null, false); }
    public static String getInputTextText(TextInputEditText inputEditText, String defaultText) { return getInputTextText(inputEditText, defaultText, false); }
    public static String getInputTextText(TextInputEditText inputEditText, String defaultText, boolean checkWindowTokenAttached) {
        if (inputEditText == null) return defaultText;
        try {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                // Use a FutureTask to retrieve the result synchronously
                FutureTask<String> futureTask = new FutureTask<>(() -> {
                    if (!checkWindowTokenAttached || inputEditText.getWindowToken() != null) { // Check if view is still attached
                        Editable e = inputEditText.getText();
                        return e == null ? defaultText : e.toString();
                    } else {
                        throw new Exception("Window Token did not check out");
                    }
                });
                new Handler(Looper.getMainLooper()).post(futureTask);
                return futureTask.get(); // Wait for the result
            } else {
                if (!checkWindowTokenAttached || inputEditText.getWindowToken() != null) { // Check if view is still attached
                    Editable e = inputEditText.getText();
                    return e == null ? defaultText : e.toString();
                } else {
                    throw new Exception("Window Token did not check out");
                }
            }
        } catch (Exception ignored) {
            return defaultText;
        }
    }


    public static void setTextColor(TextView textView, int color, boolean checkWindowTokenAttached) {
        if(textView != null) {
            try {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!checkWindowTokenAttached || textView.getWindowToken() != null) {  // Check if view is still attached
                            textView.setTextColor(color);
                        }
                    });
                } else {
                    if (!checkWindowTokenAttached || textView.getWindowToken() != null) {  // Check if view is still attached
                        textView.setTextColor(color);
                    } else
                        throw new Exception("Window Token did not check out");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error Setting Text Color for Text View! Error=" + e);
            }
        }
    }

    public static void setEditTextText(EditText inputEditText, TextWatcher textWatcher, String text, boolean checkWindowTokenAttached) {
        if(inputEditText != null) {
            try {
                final String t = text == null ? "" : text;
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!checkWindowTokenAttached || inputEditText.getWindowToken() != null) {  // Check if view is still attached
                            if(textWatcher != null) inputEditText.removeTextChangedListener(textWatcher);
                            inputEditText.setText(t);
                            if(textWatcher != null) inputEditText.addTextChangedListener(textWatcher);
                        }
                    });
                } else {
                    if (!checkWindowTokenAttached || inputEditText.getWindowToken() != null) {  // Check if view is still attached
                        if(textWatcher != null) inputEditText.removeTextChangedListener(textWatcher);
                        inputEditText.setText(t);
                        if(textWatcher != null) inputEditText.addTextChangedListener(textWatcher);
                    } else
                        throw new Exception("Window Token did not check out");
                }
            }catch (Exception e) {
                Log.e(TAG, "Error Setting Text for Text Input! Error=" + e);
            }
        }
    }

    public static void setInputTextText(TextInputEditText inputEditText, TextWatcher textWatcher, String text, boolean checkWindowTokenAttached) {
        if(inputEditText != null) {
            try {
                final String t = text == null ? "" : text;
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!checkWindowTokenAttached || inputEditText.getWindowToken() != null) {  // Check if view is still attached
                            if(textWatcher != null) inputEditText.removeTextChangedListener(textWatcher);
                            inputEditText.setText(t);
                            if(textWatcher != null) inputEditText.addTextChangedListener(textWatcher);
                        }
                    });
                } else {
                    if (!checkWindowTokenAttached || inputEditText.getWindowToken() != null) {  // Check if view is still attached
                        if(textWatcher != null) inputEditText.removeTextChangedListener(textWatcher);
                        inputEditText.setText(t);
                        if(textWatcher != null) inputEditText.addTextChangedListener(textWatcher);
                    } else
                        throw new Exception("Window Token did not check out");
                }
            }catch (Exception e) {
                Log.e(TAG, "Error Setting Text for Text Input! Error=" + e);
            }
        }
    }

    public static void setText(TextView textView, String text) { setText(textView, text, false); }
    public static void setText(TextView textView, String text, boolean checkWindowTokenAttached) {
        if(textView != null) {
            try {
                final String t = text == null ? "" : text;
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!checkWindowTokenAttached || textView.getWindowToken() != null) {  // Check if view is still attached
                            textView.setText(t);
                        }
                    });
                } else {
                    if (!checkWindowTokenAttached || textView.getWindowToken() != null) {  // Check if view is still attached
                        textView.setText(t);
                    } else
                        throw new Exception("Window Token did not check out");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error Setting Text for Text View! Error=" + e);
            }
        }
    }

    /*
        ToDo: To make this "messy" system even better
                Use the Stack to Append "app" a App Object that is a single one not these 3 different app objects
                Use the Stack to Append "container" ...
                Since the Registry can be used Globally many use cases we can use the "stack"
                We can also have a stack or holder for actual "context" var or like last context ?
                And the big reason why is so we can only pass SharedRegistry not rest args can be put to stack
                Do some system for Registry has some builder / sub registry system
                Hmm have some "fast" "cache" system or perhaps, some "cache" and then "put" "container" as key and Value is "data" ?
                better the event system in Registry
                Perhaps event links to dialog closing etc ?
                hmm
                I want some System like "Map<UI_ELEMENT,String>" ? if text changes it jumps straight to the UI Element to update ???
                Add a "hook properties" button or something ??
     */
    /*public static String getAssignmentsNumberString(
            Context context,
            int uid,
            String packageName,
            SharedRegistry sharedRegistry,
            SettingsContainer container,
            boolean refresh) {

        if(!refresh && container.data != null)
            return container.data.toString();

        if(!(sharedRegistry instanceof SettingSharedRegistry))
            return "---";

        SettingSharedRegistry setShared = (SettingSharedRegistry) sharedRegistry;
        if(refresh && context != null &&
                packageName != null &&
                !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName) &&
                uid > -1) {
            setShared.refresh(context, uid, packageName);
        } else {
            return container.data == null ? "---" : container.data.toString();
        }

        List<String> setting_names = ListUtil.toStringList(container.getSettings(), NameInformationTypeBase::getName);
        AssignmentData data = setShared.getAssignmentDataForSettings(setting_names, packageName);
        container.data = data;
        return data.toString();
    }*/


    public static AssignmentData ensureAssignmentDataInit(
            Context context,
            int uid,
            String packageName,
            SharedRegistry sharedRegistry,
            SettingsContainer container,
            boolean refresh) {

        if(container == null)
            return AssignmentData.DEFAULT;

        if(!(sharedRegistry instanceof SettingSharedRegistry) || (!refresh && container.data.hasInit))
            return container.data;

        SettingSharedRegistry setShared = (SettingSharedRegistry) sharedRegistry;
        if(refresh && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName) && uid > -1  && packageName != null) {
            container.data.refresh();
            setShared.refresh(context, uid, packageName);
        }

        List<String> setting_names = ListUtil.toStringList(container.getSettings(), NameInformationTypeBase::getName);
        setShared.initAssignmentDataForSettings(setting_names, packageName, container.data);
        return container.data;
    }

    public static void initOnGroupChanged(
            String changedGroup,
            String groupNeedOfChange,
            SharedRegistry stateRegistry,
            CheckBox checkBox,
            IIdentifiableObject item,
            CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        stateRegistry.putGroupChangeListener(new IStateChanged() {
            @Override
            public void onGroupChange(ChangedStatesPacket packet) {
                if(packet.isFrom(changedGroup)) {
                    boolean isChecked = stateRegistry.isChecked(groupNeedOfChange, item.getId());
                    checkBox.setOnCheckedChangeListener(null);
                    checkBox.setChecked(isChecked);
                    checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
                }
            }
        }, item.getId());
    }

    public static boolean logIsNotMainUIThread() {
        if(Looper.myLooper() != Looper.getMainLooper()) {
            Log.e(TAG, "This is not a UI Thread ! Stop executing UI related actions on a Non UI thread! Stack=" + RuntimeUtils.getStackTraceSafeString());
            return true;
        }

        return false;
    }

    public static boolean isRVScrollable(RecyclerView rv) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
        RecyclerView.Adapter adapter = rv.getAdapter();
        if (layoutManager == null || adapter == null) return false;
        return layoutManager.findLastCompletelyVisibleItemPosition() < adapter.getItemCount() - 1;
    }

    public static void setViewStates(boolean enable, View... views) {
        if(ArrayUtils.isValid(views)) {
            for(View v : views) {
                try {
                    v.setEnabled(enable);
                }catch (Exception ignored) { }
            }
        }
    }

    public static void bindMenuSearch(MenuItem menuSearch, IFragmentController controller) {
        final SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (controller != null) {
                    controller.updatedSortedList(FilterRequest.create(query));
                    searchView.clearFocus();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (controller != null)
                    controller.updatedSortedList(FilterRequest.create(newText));

                return true;
            }
        });

        menuSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) { return true; }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) { return true; }
        });
    }

    public static void setSwipeRefreshLayoutEndOffset(Context context, SwipeRefreshLayout swipeRefreshLayout, int startOffset) {
        if(ObjectUtils.anyNull(swipeRefreshLayout, context)) return;
        swipeRefreshLayout.setProgressViewOffset(false, startOffset, startOffset + NumericUtils.ensureIntSuccessOrOperation(
                getSwipeRefreshEndOffset(context),
                () -> FIELD_SPINNER_END_OFFSET.tryGetValueInstanceEx(swipeRefreshLayout, 130),
                130
        ));
    }

    public static int getSwipeRefreshEndOffset(Context context) {
        try {
            final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return (int) (CIRCLE_DIAMETER * metrics.density);
        }catch (Exception e) {
            Log.e(TAG, "Failed to calculate offset for Circle Refresh Swipe thingy: " + e);
            return -1;
        }
    }

    public static void setViewsVisibility(ImageView img, boolean expanded, View... views) {
        int vv = expanded ? View.VISIBLE : View.GONE;
        if (img != null) img.setImageLevel(expanded ? 1 : 0);
        for (View v : views) {
            if (v != null) v.setVisibility(vv);
        }
    }

    public static void linkEventsToView(
            boolean doLink,
            View.OnClickListener onClick,
            View.OnLongClickListener onLongClick,
            View... views) {
        View.OnClickListener oClick = doLink ? onClick : null;
        View.OnLongClickListener oLongClick = doLink ? onLongClick : null;
        //CompoundButton.OnCheckedChangeListener onCheckedChanged = doLink ? onChecked : null;
        for(View v : views) {
            if(v instanceof AdapterView) {
                //((AdapterView)v).setOnItemClickListener();
            } else {
                v.setOnClickListener(oClick);
                v.setOnLongClickListener(oLongClick);
            }

            //if(onCheckedChanged == null) {
                //Set OnChecked ToDo
            //}
        }
    }
}
