package eu.faircode.xlua.api.xmock.database;

import android.content.Context;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.xstandard.database.DatabaseHelp;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.api.useragent.MockUserAgent;
import eu.faircode.xlua.utilities.CollectionUtil;

public class MockUserAgentManager {
    public static final List<String> DEVICES = Arrays.asList(MockUserAgent.GET_UA_LINUX, MockUserAgent.GET_UA_ANDROID, MockUserAgent.GET_UA_WINDOWS, MockUserAgent.GET_UA_IPHONE, MockUserAgent.GET_UA_MACINTOSH, MockUserAgent.GET_UA_ALL);
    public static final int USER_AGENT_COUNT = 4900;

    public static MockUserAgent getRandomUserAgent(Context context, XDatabaseOld db, String device) { return CollectionUtil.getRandomElement(getUserAgentGroup(context, db, device),  MockUserAgent.DEFAULT_UA, true); }
    public static Collection<MockUserAgent> getUserAgentGroup(Context context, XDatabaseOld db, String device) {
        if(!DEVICES.contains(device)) device = MockUserAgent.GET_UA_ANDROID;
        if(!ensureDatabasePrepared(context, db)) return Collections.singletonList(MockUserAgent.DEFAULT_UA);
        if(device.equals(MockUserAgent.GET_UA_ALL)) {
            return SqlQuerySnake
                    .create(db, MockUserAgent.Table.NAME)
                    .queryAs(MockUserAgent.class);
        }else {
            return SqlQuerySnake
                    .create(db, MockUserAgent.Table.NAME)
                    .whereColumn(MockUserAgent.Table.FIELD_DEVICE, device)
                    .queryAs(MockUserAgent.class);
        }
    }

    public static boolean forceDatabaseCheck(Context context, XDatabaseOld db) {
        if(db == null) return false;
        boolean[] results = new boolean[DEVICES.size() - 1];
        int resultsIndex = 0;
        for(int i = 0; i < DEVICES.size(); i++) {
            String dev = DEVICES.get(i);
            //despite it being at the end in case its not for some reason this will ensure we are fine
            if(dev.equals(MockUserAgent.GET_UA_ALL)) continue;
            results[resultsIndex++] = DatabaseHelp.prepareTableIfMissingOrInvalidCount(
                    context,
                    db,
                    MockUserAgent.Table.NAME,
                    MockUserAgent.Table.COLUMNS,
                    dev,
                    true,
                    MockUserAgent.class,
                    DatabaseHelp.DB_FORCE_CHECK);
        } return CollectionUtil.isAllTrue(results);
    }

    public static boolean ensureDatabasePrepared(Context context, XDatabaseOld db) {
        if(db == null) return false;
        if(!db.hasTable(MockUserAgent.Table.NAME) || db.tableEntries(MockUserAgent.Table.NAME) < USER_AGENT_COUNT) forceDatabaseCheck(context, db);
        return db.hasTable(MockUserAgent.Table.NAME) && !db.tableIsEmpty(MockUserAgent.Table.NAME);
    }
}
