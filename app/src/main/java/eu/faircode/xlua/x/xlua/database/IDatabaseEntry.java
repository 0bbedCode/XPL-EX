package eu.faircode.xlua.x.xlua.database;

import android.content.ContentValues;
import android.database.Cursor;

import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;

public interface IDatabaseEntry {
    void populateContentValues(ContentValues cv);
    void populateFromContentValues(ContentValues cv);

    ContentValues toContentValues();

    void fromCursor(Cursor c);


    void populateSnake(SQLQueryBuilder snake);
}
