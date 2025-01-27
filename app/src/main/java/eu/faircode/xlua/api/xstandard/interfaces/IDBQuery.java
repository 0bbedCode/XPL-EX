package eu.faircode.xlua.api.xstandard.interfaces;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;

public interface IDBQuery {
    SqlQuerySnake createQuery(XDatabaseOld db);
}
