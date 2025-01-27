package eu.faircode.xlua.x.ui.core.dialog;

import java.util.List;

public interface IDialogOptionsEvent {
    void onPositive(List<String> checked, List<String> disabled);
}
