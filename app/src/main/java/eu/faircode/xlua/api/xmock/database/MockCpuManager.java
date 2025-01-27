package eu.faircode.xlua.api.xmock.database;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.cpu.MockCpu;
import eu.faircode.xlua.api.xstandard.database.DatabaseHelp;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;

public class MockCpuManager {
    private static final String TAG = "XLua.XMockCpuDatabase";
    public static final int COUNT = 43;
    public static final String JSON = "cpumaps.json";

    public static boolean insertCpuMap(XDatabaseOld db, MockCpu map) {
        return DatabaseHelp.insertItem(
                db,
                MockCpu.Table.name,
                map);
    }

    public static boolean updateCpuMap(XDatabaseOld db, String name, boolean selected) {
        MockCpu map = new MockCpu(name, null, null, null, selected);
        SqlQuerySnake snake = SqlQuerySnake.create(db, MockCpu.Table.name)
                .whereColumn("name", name)
                .whereColumn("selected", Boolean.toString(!selected));

        return DatabaseHelp.updateItem(snake, map);
    }

    public static boolean putCpuMaps(XDatabaseOld db, Collection<MockCpu> maps) {
       return DatabaseHelp.insertItems(db, MockCpu.Table.name, maps);
    }

    public static MockCpu getSelectedMap(XDatabaseOld db, boolean getContents) {
        SqlQuerySnake snake = SqlQuerySnake.create(db, MockCpu.Table.name)
                .whereColumn("selected", "true");

        if(!getContents)
            avoidContents(snake);

        return snake.queryGetFirstAs(MockCpu.class, true);
    }

    public static MockCpu getMap(XDatabaseOld db, String name, boolean getContents) {
        SqlQuerySnake snake = SqlQuerySnake
                .create(db, MockCpu.Table.name)
                .whereColumn("name", name);

        if(!getContents)
            snake.onlyReturnColumns("name", "model", "manufacturer", "selected");

        return snake.queryGetFirstAs(MockCpu.class, true);
    }

    public static void enforceOneSelected(XDatabaseOld db, String keepMapName, boolean keepFirstSelected) {
        SqlQuerySnake selectedSnake = SqlQuerySnake.create(db, MockCpu.Table.name)
                .whereColumn("selected", "true")
                .onlyReturnColumns("name", "selected");

        List<MockCpu> selected = new ArrayList(selectedSnake.queryAs(MockCpu.class, true));
        if(selected.size() > 1) {
            if(keepMapName != null) {
                for(int i = 0; i < selected.size(); i++) {
                    MockCpu map = selected.get(i);
                    if(map.getName().equals(keepMapName)) {
                        selected.remove(map);
                        break;
                    }
                }
            }
            else if(keepFirstSelected)
                selected.remove(selected.get(0));

            for (MockCpu m : selected)
                m.setSelected(false);

            DatabaseHelp.updateItems(db, MockCpu.Table.name, selected, selectedSnake);
        }
    }

    public static Collection<MockCpu> getSelectedMaps(XDatabaseOld db) {
        SqlQuerySnake selectedSnake = SqlQuerySnake.create(db, MockCpu.Table.name)
                .whereColumn("selected", "true")
                .onlyReturnColumns("name", "selected");

        return selectedSnake.queryAs(MockCpu.class, true);
    }

    public static Collection<String> getSelectedMapNames(XDatabaseOld db) {
        SqlQuerySnake selectedSnake = SqlQuerySnake.create(db, MockCpu.Table.name)
                .whereColumn("selected", "true")
                .onlyReturnColumns("name", "selected");

        return selectedSnake.queryAsStringList("name", true);
    }

    public static Collection<MockCpu> getCpuMaps(Context context, XDatabaseOld db) {
        return DatabaseHelp.getOrInitTable(
                context,
                db,
                MockCpu.Table.name,
                MockCpu.Table.columns,
                JSON,
                true,
                MockCpu.class,
                COUNT);
    }

    public static boolean forceDatabaseCheck(Context context, XDatabaseOld db) {
        return DatabaseHelp.prepareTableIfMissingOrInvalidCount(
                context,
                db,
                MockCpu.Table.name,
                MockCpu.Table.columns,
                JSON,
                true,
                MockCpu.class,
                DatabaseHelp.DB_FORCE_CHECK);
    }

    private static void avoidContents(SqlQuerySnake snake) {
        snake.onlyReturnColumns("name", "model", "manufacturer", "selected");
    }
}
