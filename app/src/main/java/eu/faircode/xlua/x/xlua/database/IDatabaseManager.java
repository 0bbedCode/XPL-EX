package eu.faircode.xlua.x.xlua.database;

import android.content.Context;

import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;

@SuppressWarnings("all")
public interface IDatabaseManager {
    SQLDatabase getDatabase(Context context);
    boolean initializeDatabase(Context context, boolean checkIsReady);
    public void reset(boolean setDatabaseNull);
}
