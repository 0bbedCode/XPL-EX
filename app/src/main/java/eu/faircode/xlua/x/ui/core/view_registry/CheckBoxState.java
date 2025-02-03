package eu.faircode.xlua.x.ui.core.view_registry;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class CheckBoxState {
    private static final String TAG = LibUtil.generateTag(CheckBoxState.class);

    public static CheckBoxState DEFAULT = new CheckBoxState(0, 0);

    private final boolean isChecked;
    private final int checked;
    private final int total;
    private int color = -1;

    public boolean isChecked() { return this.isChecked; }
    public int getTotalChecked() { return this.checked; }
    public int getTotal() { return this.total; }

    @NonNull
    @Override
    public String toString() {
        return "Is Checked=" + isChecked + " Checked Count=" + checked + " Total=" + total + " Color=" + color;
    }

    public int getColor(Context context) {
        if(color == -1 && context != null) color = context.getResources().getColor(checked > 0 && checked == total ? R.color.colorAccent : android.R.color.darker_gray, null);
        return color;
    }

    CheckBoxState(int checked, int total) {
        isChecked = checked > 0;
        this.checked = checked;
        this.total = total;
    }

    public static CheckBoxState create(int checked, int total) {
        return new CheckBoxState(checked, total);
    }

    public static <T extends IIdentifiableObject> CheckBoxState from(List<T> object, boolean allChecked) {
        int sz = ListUtil.size(object);
        return new CheckBoxState(allChecked ? sz : 0, sz);
    }

    public static <T extends IIdentifiableObject> CheckBoxState from(List<T> objects, String tag, SharedRegistry registry) {
        if(ObjectUtils.anyNull(objects, tag, registry))
            return DEFAULT;
        int checked = registry.getEnabledCount(objects, tag);
        return new CheckBoxState(checked, objects.size());
    }

    public CheckBoxState updateCheckBox(CheckBox checkBox) { return updateCheckBox(checkBox, null); }
    public CheckBoxState updateCheckBox(CheckBox checkBox, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        if (checkBox != null) {
            CoreUiUtils.logIsNotMainUIThread();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Updating Check Box State, IsChecked=" + isChecked +
                        " Checked Size=" + checked +
                        " Total Size=" + total +
                        " Is Checked Change Listener Null ? " + (onCheckedChangeListener == null ? "true" : "false"));

            if (onCheckedChangeListener != null) checkBox.setOnCheckedChangeListener(null);

            checkBox.setChecked(isChecked);
            if(DebugUtil.isDebug())
                Log.d(TAG, "CheckBox state set to: " + isChecked + " Count=" + checked + " Total Size=" + total);

            int color = getColor(checkBox.getContext());
            if(DebugUtil.isDebug())
                Log.d(TAG, "Button tint updated for checked state. " +
                        "IsChecked=" + isChecked + " " +
                        "Count=" + checked + " " +
                        "Total Size=" + total + " " +
                        "Color=" + color + " " +
                        "Original Color=" + getOriginalCheckboxTint(checkBox, -1337));

            checkBox.setButtonTintList(ColorStateList.valueOf(color));

            if (onCheckedChangeListener != null) checkBox.setOnCheckedChangeListener(onCheckedChangeListener);

            // Ensure UI refresh
            checkBox.invalidate();
            checkBox.requestLayout();
        }

        return this;
    }

    public <T extends IIdentifiableObject> CheckBoxState notifyObjects(List<T> objects, String groupChanged, SharedRegistry registry) {
        if(registry != null) {
            for(T o : objects) {
                registry.notifyGroupChange(o.getSharedId(), groupChanged);
            }
        }

        return this;
    }

    public CheckBoxState updateCheckBoxColor(CheckBox checkBox) {
        if(checkBox != null) {
            int color = getColor(checkBox.getContext());
            if(DebugUtil.isDebug())
                Log.d(TAG, "Updating Check Box Color, IsChecked=" + isChecked +
                        " Checked Size=" + checked +
                        " Total Size=" + total +
                        " Color=" + color +
                        " Original Color=" + getOriginalCheckboxTint(checkBox, -1337));
            checkBox.setButtonTintList(ColorStateList.valueOf(color));
        }

        return this;
    }

    public static int getOriginalCheckboxTint(CheckBox cb, int defaultIfNull) {
        if(cb == null) return defaultIfNull;
        try {
            ColorStateList buttonTintList = cb.getButtonTintList();
            return buttonTintList == null ? defaultIfNull : buttonTintList.getDefaultColor();
        }catch (Exception ignored) { return defaultIfNull; }
    }
}
