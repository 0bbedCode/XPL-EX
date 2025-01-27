package eu.faircode.xlua.x.xlua.commands.packet;

import android.content.Context;

import androidx.annotation.NonNull;

import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;

public class BridgePacket implements IBridgePacketCtx {
    protected String commandPrefix;
    protected String method;
    protected Context context;
    protected SQLDatabase db;
    protected String packageName;

    @Override
    public String getMethod() { return this.method; }
    @Override
    public Context getContext() { return this.context; }
    @Override
    public SQLDatabase getDatabase() { return this.db; }
    @Override
    public String getPackageName() { return this.packageName; }
    @Override
    public String getCommandPrefix() { return this.commandPrefix; }

    public BridgePacket() { }
    public BridgePacket(String commandPrefix, String method, Context context, SQLDatabase db, String packageName) {
        this.commandPrefix = commandPrefix;
        this.method = method;
        this.context = context;
        this.db = db;
        this.packageName = packageName;
        //Is VXP if package is null ?
        //Also secret key ?
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Command Prefix", this.commandPrefix)
                .appendFieldLine("Method", this.method)
                .appendFieldLine("Package", this.packageName)
                .appendFieldLine("Database", this.db)
                .toString(true);
    }
}
