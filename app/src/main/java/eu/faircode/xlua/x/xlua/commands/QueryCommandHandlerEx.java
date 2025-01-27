package eu.faircode.xlua.x.xlua.commands;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import eu.faircode.xlua.XSecurity;
import eu.faircode.xlua.api.xstandard.command.QueryPacket_old;
import eu.faircode.xlua.api.xstandard.interfaces.ISecurityObject;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;

public abstract class QueryCommandHandlerEx implements IXCommand {
    protected String name;
    protected boolean requiresPermissionCheck = false;
    protected boolean marshall = false;
    protected boolean requiresSingleThread = false;

    public QueryCommandHandlerEx() { }

    public abstract Cursor handle(QueryPacket commandData) throws Throwable;

    @Override
    public boolean requiresPermissionCheck() { return requiresPermissionCheck; }

    @Override
    public boolean requiresSingleThread() { return requiresSingleThread; }

    @Override
    public boolean isMarshal() { return marshall; }

    @Override
    public String getCommandName() { return name; }

    public void setAsMarshallCommand() {
        name += "2";
        marshall = true;
    }

    public void  setName(String name, boolean marshall) {
        this.name = marshall ? name + "2" : name;
        this.marshall = marshall;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof String)) return false;
        String other = (String) obj;
        return this.name.equals(other);
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .appendFieldLine("Name", name)
                .appendFieldLine("Requires UID Check", String.valueOf(requiresPermissionCheck))
                .appendFieldLine("Single Thread", String.valueOf(requiresSingleThread))
                .appendFieldLine("Marshall", String.valueOf(marshall))
                .toString(true);
    }
}
