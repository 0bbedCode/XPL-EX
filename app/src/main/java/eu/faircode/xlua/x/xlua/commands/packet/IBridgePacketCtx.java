package eu.faircode.xlua.x.xlua.commands.packet;

import android.content.Context;

import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;

public interface IBridgePacketCtx {
    String getMethod();
    Context getContext();
    SQLDatabase getDatabase();
    String getPackageName();
    String getCommandPrefix();
}
