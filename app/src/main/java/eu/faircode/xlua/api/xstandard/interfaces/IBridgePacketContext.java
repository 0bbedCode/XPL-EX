package eu.faircode.xlua.api.xstandard.interfaces;

import android.content.Context;

import eu.faircode.xlua.XDatabaseOld;

public interface IBridgePacketContext {
    String getCommandPrefix();
    String getMethod();
    Context getContext();
    XDatabaseOld getDatabase();
    String getPackageName();
}
