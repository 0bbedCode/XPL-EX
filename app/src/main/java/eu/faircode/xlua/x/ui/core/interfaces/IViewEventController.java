package eu.faircode.xlua.x.ui.core.interfaces;

import android.view.View;

import eu.faircode.xlua.x.ui.core.ViewEventController;

public interface IViewEventController {
    void addViewsToEventController(View... views);
    void wire();
    void unWire();
    ViewEventController getViewEventController();
}
