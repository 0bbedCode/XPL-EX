package eu.faircode.xlua.x.ui.core;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;

public class FloatingActionButtonContext {
    public static FloatingActionButtonContext create() { return new FloatingActionButtonContext(); }
    private static final String TAG = "XLua.FloatingActionButtonContext";

    private Animation _fabOpen, _fabClose, _fabFromBottom, _fabToBottom;
    private FloatingActionButton _mainActionButton;
    private boolean _isActionOpen = false;
    private boolean _isMainActionVisible = false;
    private final List<FloatingActionButton> _buttons = new ArrayList<>();

    public FloatingActionButton getMainActionButton() { return _mainActionButton;  }
    public boolean isActionOpen() { return _isActionOpen; }


    public FloatingActionButtonContext initAnimations(Context context) {
        _fabOpen = AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim_one);
        _fabClose = AnimationUtils.loadAnimation(context,R.anim.rotate_close_anim_one);
        _fabFromBottom = AnimationUtils.loadAnimation(context,R.anim.from_bottom_anim_one);
        _fabToBottom = AnimationUtils.loadAnimation(context,R.anim.to_bottom_anim_one);
        return this;
    }

    public FloatingActionButtonContext add(
            View.OnClickListener onClickListener,
            View.OnLongClickListener onLongClickListener,
            FloatingActionButton... buttons) {

        if(ArrayUtils.isValid(buttons)) {
            for(FloatingActionButton b : buttons) {
                try {
                    if(_mainActionButton == null) _mainActionButton = b;
                    else _buttons.add(b);

                    if(onClickListener != null) b.setOnClickListener(onClickListener);
                    if(onLongClickListener != null) b.setOnLongClickListener(onLongClickListener);
                }catch (Exception e) {
                    Log.e(TAG, "Failed to add Floating Action Button, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
                }
            }
        }

        return this;
    }

    public void init(final RecyclerView rv) {
        if(!_mainActionButton.isShown() && !CoreUiUtils.isRVScrollable(rv))
            hideMainActionButton(false);
    }

    public void invokeFloatingActions() {
        _isActionOpen = !_isActionOpen;

        Animation lowAnimation = _isActionOpen ? _fabFromBottom : _fabToBottom;
        Animation baseAnimation = _isActionOpen ? _fabOpen : _fabClose;
        int visibility = _isActionOpen ? View.VISIBLE : View.INVISIBLE;

        if (!_buttons.isEmpty()) {
            for (int i = _buttons.size() - 1; i >= 0; i--) {
                FloatingActionButton fa = _buttons.get(i);
                fa.startAnimation(lowAnimation);
            }

            _mainActionButton.startAnimation(baseAnimation);
            for (FloatingActionButton fa : _buttons) {
                fa.setVisibility(visibility);
                fa.setLongClickable(_isActionOpen);
                fa.setClickable(_isActionOpen);
            }
        } else {
            _mainActionButton.startAnimation(baseAnimation);
        }
    }

    public void linkRecyclerViewToControlVisibility(final RecyclerView rv) {
        if(rv != null && _mainActionButton != null) {
            try {
                rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if(_mainActionButton != null) {
                            // If scrolling up, show the FAB; if scrolling down, hide the FAB
                            if (dy > 0 && _mainActionButton.isShown() && CoreUiUtils.isRVScrollable(rv)) {
                                if(_isActionOpen)
                                    invokeFloatingActions();

                                hideMainActionButton(true);
                            } else if (dy < 0 && !_mainActionButton.isShown()) {
                                hideMainActionButton(false);
                            }
                        }
                    }
                });
            }catch (Exception e) {
                Log.e(TAG, "Failed to link Recycler View to Floating Action Buttons Visibility State when on Scroll. Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            }
        }
    }

    protected void hideMainActionButton(boolean hide) {
        if(_mainActionButton != null) {
            _isMainActionVisible = hide;
            if(hide) _mainActionButton.hide();
            else _mainActionButton.show();
            _mainActionButton.setLongClickable(!hide);
            _mainActionButton.setClickable(!hide);
        }
    }
}
