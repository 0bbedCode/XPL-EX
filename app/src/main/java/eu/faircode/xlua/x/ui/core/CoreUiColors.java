package eu.faircode.xlua.x.ui.core;

import android.content.Context;

import eu.faircode.xlua.R;
import eu.faircode.xlua.XUtil;

public class CoreUiColors {
    public static int getSwipeRefreshColor(Context context) { return XUtil.resolveColor(context, R.attr.colorAccent); }
}
