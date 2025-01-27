package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;

public interface IAssignListener {
    void setAssigned(Context context, String group, boolean assign);
}
