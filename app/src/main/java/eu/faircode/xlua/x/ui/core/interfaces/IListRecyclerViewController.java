package eu.faircode.xlua.x.ui.core.interfaces;

import android.widget.ProgressBar;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import eu.faircode.xlua.x.ui.core.adapter.ListGenericAdapter;

public interface IListRecyclerViewController<TElement extends IDiffFace, TElementBinding extends ViewBinding> {
    void setIsRefreshing(boolean isRefreshing);
    ProgressBar getProgressBar();
    void setProgressBar(ProgressBar progressBar);
    SwipeRefreshLayout getSwipeRefreshLayout();
    void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout);
    void initSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout, int refreshCircleColor);
    void initFloatingActions(FloatingActionButton... floatingActionButtons);
    void initRecyclerView(RecyclerView recyclerView);


    IListAdapter<TElement, ?> getAdapter();
    void setAdapter(IListAdapter<TElement, TElementBinding> adapter);

    boolean isScrollable();
}
