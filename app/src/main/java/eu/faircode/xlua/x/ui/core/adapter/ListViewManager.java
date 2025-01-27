package eu.faircode.xlua.x.ui.core.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.viewbinding.ViewBinding;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;
import eu.faircode.xlua.x.ui.core.interfaces.IStateManager;

public abstract class ListViewManager<TElement extends IDiffFace, TBinding extends ViewBinding> {
    private static final String TAG = "XLua.ListViewManager";

    protected final LayoutInflater inflater;
    protected final IStateManager stateManager;
    protected final SharedRegistry stateRegistry;
    protected final LinearLayout containerView;
    protected final Context context;
    protected List<TElement> currentItems;

    public ListViewManager(Context context, LinearLayout containerView, IStateManager stateManager) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.containerView = containerView;
        this.stateManager = stateManager;
        this.stateRegistry = stateManager.getSharedRegistry();
    }

    protected abstract TBinding inflateItemView(ViewGroup parent);
    protected abstract void bindItemView(TBinding binding, TElement item);
    protected abstract void cleanupItemView(TBinding binding);
    protected abstract String getStateTag();

    public void submitList(List<TElement> items) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "[submitList] Item Count=" + ListUtil.size(items));

        this.currentItems = items;
        updateViews();
    }

    public void clear() {
        if(DebugUtil.isDebug())
            Log.d(TAG, "[clear] Clearing Items Count=" + ListUtil.size(this.currentItems));

        cleanupAllViews();
        currentItems = null;
    }

    protected void updateViews() {
        containerView.removeAllViews();
        if (currentItems != null) {
            for (TElement item : currentItems) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "[updateViews] Inflating Item=" + Str.toStringOrNull(item));

                TBinding binding = inflateItemView(containerView);
                bindItemView(binding, item);
                containerView.addView(binding.getRoot());
            }
        }
    }

    protected void cleanupAllViews() {
        for (int i = 0; i < containerView.getChildCount(); i++) {
            View child = containerView.getChildAt(i);
            if (child != null && child.getTag() instanceof ViewBinding) {
                cleanupItemView((TBinding) child.getTag());
            }
        }
        containerView.removeAllViews();
    }
}