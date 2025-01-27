package eu.faircode.xlua.x.xlua.database;

import android.content.Context;

import eu.faircode.xlua.XDatabaseOld;

public interface IDatabaseInsurance {
    String getInsuranceId();
    void ensureIsInitialized(Context context, XDatabaseOld database);
}
