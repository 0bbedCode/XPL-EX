package eu.faircode.xlua.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.AppGeneric;
import eu.faircode.xlua.R;

public class ViewFloatingAction extends Fragment {
    protected String TAG_ViewFloatingAction = "XLua.ViewFloatingAction";

    protected Animation fabOpen, fabClose, fromBottom, toBottom;
    protected boolean isActionOpen = false;
    protected boolean isMainHidden = false;

    public AppGeneric application = AppGeneric.DEFAULT;

    protected RecyclerView rvList = null;

    private FloatingActionButton mainActionButton;
    private final List<FloatingActionButton> actionButtons = new ArrayList<>();

    protected ProgressBar progressBar;
    protected SwipeRefreshLayout swipeRefresh;

    public ViewFloatingAction() {  }
    //public ViewFloatingAction(Context context) { initActions(context); }

    protected List<FloatingActionButton> getHigherButtons() { return this.actionButtons; }
    protected FloatingActionButton getMainActionButton() { return this.mainActionButton; }

    protected void setRefreshState(boolean refreshing) {
        if(swipeRefresh != null) swipeRefresh.setRefreshing(refreshing);
        if(progressBar != null) progressBar.setVisibility(refreshing ? View.VISIBLE : View.GONE);
    }

    protected void bindTextViewsToAppId(View main, int tvAppIcon, int tvPackageName, int tvPackageFull, int tvPackageUid) {
        try {
            Log.i(TAG_ViewFloatingAction, "Application Object=" + application);
            //initUserAppId();
            this.application.initView(getContext(), main, tvAppIcon, tvPackageName, tvPackageFull, tvPackageUid);
        }catch (Exception e) {
            Log.e(TAG_ViewFloatingAction, "Failed to bind Text Views to App ID");
        }
    }

    protected void setFloatingActionBars(View view, int... actions) { setFloatingActionBars(null, null, view, actions); }
    protected void setFloatingActionBars(View.OnClickListener onClickListener, View view, int... actions) { setFloatingActionBars(onClickListener, null, view, actions); }
    protected void setFloatingActionBars(View.OnLongClickListener onLongHold, View view, int... actions) { setFloatingActionBars(null, onLongHold, view, actions); }
    protected void setFloatingActionBars(View.OnClickListener onClickListener, View.OnLongClickListener onLongHold, View view, int... actions) {
        if(view == null || actions == null || actions.length < 1) {
            Log.e(TAG_ViewFloatingAction, "[setFloatingActionBars] Context , View or Actions is null...");
            return;
        }

        for(int rId : actions) {
            try {
                FloatingActionButton flAction = view.findViewById(rId);
                if(flAction == null) {
                    Log.w(TAG_ViewFloatingAction, "Floating Action was null... id=" + rId);
                    continue;
                }

                if(onClickListener != null)
                    flAction.setOnClickListener(onClickListener);

                if(onLongHold != null)
                    flAction.setOnLongClickListener(onLongHold);

                if(mainActionButton == null) { mainActionButton = flAction; this.isMainHidden = mainActionButton.isShown(); }
                else actionButtons.add(flAction);
            }catch (Exception e) {
                Log.e(TAG_ViewFloatingAction, "[setFloatingActionBars] Failed to resolve ID... e=" + e);
            }finally {

                Log.i(TAG_ViewFloatingAction, "[setFloatingActionBars] Button Count=" + actionButtons.size() + " isOpen=" + isActionOpen);
            }
        }
    }

    protected void initRecyclerView(View main, int rvView, boolean bindToFloatingAction) {
        try {
             this.rvList = main.findViewById(rvView);
             if(bindToFloatingAction) bindActionButtonToRecyclerView(this.rvList);
        }catch (Exception e) {
            Log.e(TAG_ViewFloatingAction, "[bindActionButtonToRecyclerView]");
        }
    }

    public boolean isOpen() { return mainActionButton.isShown(); }
    public boolean isActionOpen() { return isActionOpen; }
    public boolean isMainHidden() { return isMainHidden; }


    protected void bindActionButtonToRecyclerView() { bindActionButtonToRecyclerView(this.rvList); }
    protected void bindActionButtonToRecyclerView(RecyclerView rvView) {
        if(rvView == null || mainActionButton == null)
            return;

        rvView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // If scrolling up, show the FAB; if scrolling down, hide the FAB
                if (dy > 0 && mainActionButton.isShown() && isRecyclerScrollable()) {
                    if(isActionOpen)
                        invokeFloatingActions();

                    hideMainActionButton(true);
                } else if (dy < 0 && !mainActionButton.isShown()) {
                    hideMainActionButton(false);
                }
            }
        });
    }

    public boolean isRecyclerScrollable() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvList.getLayoutManager();
        RecyclerView.Adapter adapter = rvList.getAdapter();
        if (layoutManager == null || adapter == null) return false;

        return layoutManager.findLastCompletelyVisibleItemPosition() < adapter.getItemCount() - 1;
    }


    protected void hideMainActionButton(boolean hide) {
        if(mainActionButton == null) return;
        this.isMainHidden = hide;
        if(hide) mainActionButton.hide();
        else mainActionButton.show();
        mainActionButton.setLongClickable(!hide);
        mainActionButton.setClickable(!hide);
    }

    public void handleFloatingActions() {
        if(!mainActionButton.isShown() && !isRecyclerScrollable())
            hideMainActionButton(false);

        //if (mainActionButton.isShown() && isRecyclerScrollable()) {
        //    if(isActionOpen)
        //        invokeFloatingActions();
        //    hideMainActionButton(true);
        //} else if (!mainActionButton.isShown()) {
        //    hideMainActionButton(false);
        //}
    }

    protected void invokeFloatingActions() {
        isActionOpen = !isActionOpen;

        Animation lowAnimation = isActionOpen ? fromBottom : toBottom;
        Animation baseAnimation = isActionOpen ? fabOpen : fabClose;
        int visibility = isActionOpen ? View.VISIBLE : View.INVISIBLE;

        Log.i(TAG_ViewFloatingAction, " isOpen=" + isActionOpen + " action button size=" + actionButtons.size());

        if (!actionButtons.isEmpty()) {
            for (int i = actionButtons.size() - 1; i >= 0; i--) {
                FloatingActionButton fa = actionButtons.get(i);
                fa.startAnimation(lowAnimation);
            }

            mainActionButton.startAnimation(baseAnimation);
            for (FloatingActionButton fa : actionButtons) {
                fa.setVisibility(visibility);
                fa.setLongClickable(isActionOpen);
                fa.setClickable(isActionOpen);
            }
        } else {
            mainActionButton.startAnimation(baseAnimation);
        }
    }

    protected void initActions() {
        if(getContext() != null) {
            fabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_open_anim_one);
            fabClose = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_close_anim_one);
            fromBottom = AnimationUtils.loadAnimation(getContext(),R.anim.from_bottom_anim_one);
            toBottom = AnimationUtils.loadAnimation(getContext(),R.anim.to_bottom_anim_one);
        }
    }
}
