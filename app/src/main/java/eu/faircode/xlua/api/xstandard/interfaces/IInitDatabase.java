package eu.faircode.xlua.api.xstandard.interfaces;

import android.content.Context;

import eu.faircode.xlua.XDatabaseOld;

public interface IInitDatabase {
    XDatabaseOld getDatabase(Context context);
    boolean initDatabase(Context context, boolean checkIsReady);
    void reset(boolean setDatabaseNull);
}
