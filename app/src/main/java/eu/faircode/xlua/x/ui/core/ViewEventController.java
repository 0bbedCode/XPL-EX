package eu.faircode.xlua.x.ui.core;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;

@SuppressWarnings("all")
public class ViewEventController implements IValidator {
    private final List<View> views = new ArrayList<>();
    private boolean mIsWired = false;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;

    //Have ability to Set Visibilities
    public boolean isWired() { return mIsWired; }

    public static ViewEventController create() { return new ViewEventController(); }
    public static ViewEventController create(View.OnClickListener onClick) { return new ViewEventController(onClick); }
    public static ViewEventController create(View.OnLongClickListener onLongClick) { return new ViewEventController(onLongClick); }
    public static ViewEventController create(View.OnClickListener onClick, View.OnLongClickListener onLongClick) { return new ViewEventController(onClick, onLongClick); }

    public ViewEventController() { }
    public ViewEventController(View.OnClickListener onClick) { linkOnClick(onClick); }
    public ViewEventController(View.OnLongClickListener onLongClick) { linkOnLongClick(onLongClick); }
    public ViewEventController(View.OnClickListener onClick, View.OnLongClickListener onLongClick) { linkOnClick(onClick); linkOnLongClick(onLongClick); }

    public ViewEventController addView(View view) {
        if(view != null && !views.contains(view)) views.add(view);
        return this;
    }

    public ViewEventController addViews(View... views) {
        if(ArrayUtils.isValid(views))
            for(View v : views)
                addView(v);

        return this;
    }

    public ViewEventController wire() { return setViewsClickEvents(true, true); }
    public ViewEventController unWire() { return setViewsClickEvents(false, false); }

    public ViewEventController setViewsClickEvents(boolean onClick, boolean onLongClick) {
        if(ListUtil.isValid(views)) {
            View.OnClickListener clickListener = onClick ? onClickListener : null;
            View.OnLongClickListener longClickListener = onLongClick ? onLongClickListener : null;
            for(View v : views) {
                v.setOnClickListener(clickListener);
                v.setOnLongClickListener(longClickListener);
            }
        }

        return this;
    }

    public ViewEventController linkOnLongClick(View.OnLongClickListener onLongClick) {
        if(onLongClick != null) this.onLongClickListener = onLongClick;
        return this;
    }

    public ViewEventController linkOnClick(View.OnClickListener onClick) {
        if(onClick != null) this.onClickListener = onClick;
        return this;
    }

    @Override
    public boolean isValid() { return ListUtil.isValid(views) && (onClickListener != null || onLongClickListener != null); }
}
