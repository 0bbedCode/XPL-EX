package eu.faircode.xlua.api.xstandard.command;

import android.content.Context;

import androidx.annotation.NonNull;

import eu.faircode.xlua.XDatabaseOld;

public class BridgePacket_old {
    protected String commandPrefix;
    protected String method;
    protected Context context;
    protected XDatabaseOld db;
    protected String packageName;

    public BridgePacket_old() { }
    public BridgePacket_old(String commandPrefix, String method, Context context, XDatabaseOld db, String packageName) {
        this.commandPrefix = commandPrefix;
        this.method = method;
        this.context = context;
        this.db = db;
        this.packageName = packageName;
        //Is VXP if package is null ?
        //Also secret key ?
    }

    public String getCommandPrefix() { return this.commandPrefix; }
    public String getMethod() { return this.method; }
    public Context getContext() { return this.context; }
    public XDatabaseOld getDatabase() { return this.db; }
    public String getPackageName() { return this.packageName; }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder()
                .append(" command prefix=")
                .append(commandPrefix)
                .append(" method=")
                .append(method)
                .append(" package=")
                .append(packageName)
                .append(" db=")
                .append(db).toString();
    }
}
