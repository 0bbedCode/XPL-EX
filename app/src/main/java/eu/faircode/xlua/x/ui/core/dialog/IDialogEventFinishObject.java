package eu.faircode.xlua.x.ui.core.dialog;

import eu.faircode.xlua.x.xlua.database.A_CODE;

public interface IDialogEventFinishObject<T> {
    void onFinish(A_CODE result, T obj);
}
