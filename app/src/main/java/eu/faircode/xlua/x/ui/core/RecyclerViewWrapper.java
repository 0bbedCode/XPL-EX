package eu.faircode.xlua.x.ui.core;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;
import eu.faircode.xlua.x.ui.core.interfaces.IListAdapter;

public class RecyclerViewWrapper<TElement extends IDiffFace, TElementBinding extends ViewBinding> implements IValidator {
    //public static RecyclerViewWrapper<TElement> create() { return new RecyclerViewWrapper(); }
    //public static RecyclerViewWrapper create(RecyclerView recyclerView) { return new RecyclerViewWrapper(recyclerView); }
    private static final int DEFAULT_PREFETCH_COUNT = 4;

    private RecyclerView recyclerView;
    private FloatingActionButtonContext floatingActionButtonContext;
    private IListAdapter<TElement, TElementBinding> adapter;

    public RecyclerViewWrapper() {  }
    public RecyclerViewWrapper(RecyclerView recyclerView) { this.recyclerView = recyclerView; }

    public RecyclerView getRecyclerView() { return this.recyclerView; }
    public RecyclerViewWrapper<TElement, TElementBinding> setRecyclerView(RecyclerView recyclerView) { if(recyclerView != null) this.recyclerView = recyclerView; return this; }

    public FloatingActionButtonContext getFloatingActionButtonContext() { return this.floatingActionButtonContext; }
    public RecyclerViewWrapper<TElement, TElementBinding> setFloatingActionsContext(FloatingActionButtonContext floatingActionButtonContext) {
        if(floatingActionButtonContext != null) this.floatingActionButtonContext = floatingActionButtonContext;
        return this;
    }


    public RecyclerViewWrapper<TElement, TElementBinding> useLinearLayoutManager(Context context) {
        return useLinearLayoutManager(context, DEFAULT_PREFETCH_COUNT);
    }

    public RecyclerViewWrapper<TElement, TElementBinding> useLinearLayoutManager(Context context, int prefetchCount) {
        if(recyclerView != null) {
            LinearLayoutManager llm = new LinearLayoutManager(context) {
                @Override
                public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull RecyclerView.State state, @NonNull View child, View focused) {
                    return true;
                }
            };

            llm.setAutoMeasureEnabled(true);
            llm.setInitialPrefetchItemCount(prefetchCount);
            recyclerView.setLayoutManager(llm);
        }
        return this;
    }

    public RecyclerViewWrapper<TElement, TElementBinding> disableAnimations() {
        if(recyclerView != null) {
            recyclerView.setItemAnimator(null);
        }
        return this;
    }

    public RecyclerViewWrapper<TElement, TElementBinding> optimizeForPerformance() {
        if(recyclerView != null) {
            disableAnimations();
            setHasFixedSize(true);
            recyclerView.setNestedScrollingEnabled(false); // Disable nested scrolling for better performance
        }
        return this;
    }


    public IListAdapter<TElement, TElementBinding> getAdapter() { return this.adapter; }
    public  RecyclerViewWrapper<TElement, TElementBinding> setAdapter(IListAdapter<TElement, TElementBinding> adapter) {
        if(adapter != null) this.adapter = adapter;
        return this;
    }

    public RecyclerViewWrapper<TElement, TElementBinding> setVisibility(boolean isVisible) {
        if(recyclerView != null) recyclerView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        return this;
    }

    public RecyclerViewWrapper<TElement, TElementBinding> setHasFixedSize(boolean hasFixedSize) {
        if(recyclerView != null) recyclerView.setHasFixedSize(hasFixedSize);
        return this;
    }

    public RecyclerViewWrapper<TElement, TElementBinding> setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if(recyclerView != null) recyclerView.setLayoutManager(layoutManager);
        return this;
    }

    /*public RecyclerViewWrapper<TElement, TElementBinding> useLinearLayoutManager(Context context) {
        if(recyclerView != null) {
            LinearLayoutManager llm = new LinearLayoutManager(context) {
                @Override
                public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull RecyclerView.State state, @NonNull View child, View focused) { return true; }
            };

            llm.setAutoMeasureEnabled(true);
            recyclerView.setLayoutManager(llm);
        }

        return this;
    }*/

    public RecyclerViewWrapper<TElement, TElementBinding> ensureAdapterIsLinked() { return ensureAdapterIsLinked(getAdapter()); }
    public RecyclerViewWrapper<TElement, TElementBinding> ensureAdapterIsLinked(IListAdapter<TElement, TElementBinding> adapter) {
        if(adapter != null) {
            if(this.adapter == null || this.adapter != adapter) this.adapter = adapter;
            //we getAsAdapter ? will this be fine ?
            if(this.recyclerView != null) this.recyclerView.setAdapter(adapter.getAsListAdapterUnsafe());
        }

        return this;
    }


    public RecyclerViewWrapper<TElement, TElementBinding> ensureFloatingActionsAreLinked() { return ensureFloatingActionsAreLinked(getFloatingActionButtonContext()); }
    public RecyclerViewWrapper<TElement, TElementBinding> ensureFloatingActionsAreLinked(FloatingActionButtonContext floatingActionButtonContext) {
        if(floatingActionButtonContext != null) {
            if(this.floatingActionButtonContext == null || this.floatingActionButtonContext != floatingActionButtonContext) this.floatingActionButtonContext = floatingActionButtonContext;
            if(this.recyclerView != null)
                this.floatingActionButtonContext.linkRecyclerViewToControlVisibility(this.recyclerView);
        }

        return this;
    }

    public boolean isScrollable() {
        if(this.recyclerView == null || this.adapter == null) return false;
        LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
        RecyclerView.Adapter ad = recyclerView.getAdapter();
        if(ad == null || layoutManager == null) return false;
        return layoutManager.findLastCompletelyVisibleItemPosition() < ad.getItemCount() - 1;
    }

    public  RecyclerViewWrapper<TElement, TElementBinding> hideActionButtonsWhenScroll() {
        if(floatingActionButtonContext != null && recyclerView != null) floatingActionButtonContext.linkRecyclerViewToControlVisibility(recyclerView);
        return this;
    }

    @Override
    public boolean isValid() { return this.recyclerView != null && this.adapter != null; }
}
