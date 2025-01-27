package eu.faircode.xlua.x.ui.core;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;

public class RecyclerDynamicSizeAdjuster {
    public static final String TAG = "XLua.RecyclerDynamicSizeAdjuster";
    public static RecyclerDynamicSizeAdjuster create() { return new RecyclerDynamicSizeAdjuster(); }
    private int lastHeight = 0;

    public void startTopViewAdjuster(final View targetTopView, final RecyclerView recyclerView, final SwipeRefreshLayout swipeRefresh) {
        if(targetTopView != null && recyclerView != null) {
            targetTopView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                int height = targetTopView.getHeight();
                if(height != lastHeight) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Last Height is Not Equal to Current Height, Current Height=" + height + " Last Height=" + lastHeight);

                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) targetTopView.getLayoutParams();
                    int totalHeight = height + layoutParams.topMargin + layoutParams.bottomMargin + 15;
                    recyclerView.setPadding(0, totalHeight, 0, 0);
                    int lastHeightCopy = lastHeight;
                    lastHeight = height;

                    if(swipeRefresh != null)
                        CoreUiUtils.setSwipeRefreshLayoutEndOffset(targetTopView.getContext(), swipeRefresh, totalHeight);

                    //UiUtil.setSwipeRefreshLayoutEndOffset(getContext(), swipeRefresh, totalHeight);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    assert layoutManager != null;
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisiblePosition == 0) {
                        if(height > lastHeightCopy)
                            recyclerView.scrollBy(0, -totalHeight);
                    }
                }
            });
        }
    }
}
