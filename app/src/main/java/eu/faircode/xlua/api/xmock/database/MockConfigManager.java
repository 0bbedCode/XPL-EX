package eu.faircode.xlua.api.xmock.database;

import android.content.Context;

import java.util.Collection;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.configs.MockConfig;
import eu.faircode.xlua.api.configs.MockConfigPacket;
import eu.faircode.xlua.api.properties.MockPropMap;
import eu.faircode.xlua.api.xstandard.database.DatabaseHelp;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.utilities.StringUtil;

public class MockConfigManager {
    private static final String JSON = "configs.json";
    private static final int COUNT = 3;

    public static XResult putMockConfig(Context context, XDatabaseOld db, MockConfigPacket packet) {
        XResult res = XResult.create().setMethodName("putMockConfig").setExtra(packet.toString());
        if(!StringUtil.isValidString(packet.getName())) return res.setFailed("Mock Config Name is Null!");
        boolean result =
                !packet.isDelete() ?
                        DatabaseHelp.insertItem(
                                db,
                                MockConfig.Table.name,
                                packet,
                                true) :
                        DatabaseHelp.deleteItem(SqlQuerySnake
                                .create(db, MockConfig.Table.name)
                                .whereColumn(MockConfig.Table.FIELD_NAME, packet.getName()));

        if(packet.isDelete() && !result)
            result = SqlQuerySnake.create(db, MockPropMap.Table.name)
                    .whereColumn(MockConfig.Table.FIELD_NAME, packet.getName())
                    .exists();

        return res.setResult(result);
    }

    public static Collection<MockConfig> getMockConfigs(Context context, XDatabaseOld db) {
        return DatabaseHelp.getOrInitTable(
                context,
                db,
                MockConfig.Table.name,
                MockConfig.Table.columns,
                JSON,
                true,
                MockConfig.class,
                COUNT);
    }

    public static boolean prepareDatabaseTable(Context context, XDatabaseOld db) {
        return DatabaseHelp.prepareTableIfMissingOrInvalidCount(
                context,
                db,
                MockConfig.Table.name,
                MockConfig.Table.columns,
                JSON,
                true,
                MockConfig.class,
                COUNT);
    }

    public static boolean forceDatabaseCheck(Context context, XDatabaseOld db) {
        prepareDatabaseTable(context, db);
        return DatabaseHelp.prepareTableIfMissingOrInvalidCount(
                context,
                db,
                MockConfig.Table.name,
                MockConfig.Table.columns,
                JSON,
                true,
                MockConfig.class,
                DatabaseHelp.DB_FORCE_CHECK);
    }
}
