package eu.faircode.xlua.api.xstandard;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import eu.faircode.xlua.XSecurity;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.xstandard.interfaces.ISecurityObject;
import eu.faircode.xlua.x.data.string.StrBuilder;

public abstract class CallCommandHandler implements ISecurityObject {
    protected String name;
    protected boolean requiresPermissionCheck;
    protected boolean requiresSingleThread = false;

    public CallCommandHandler() { }
    public CallCommandHandler(String name, boolean requirePermissionCheck) {
        this.name = name;
        this.requiresPermissionCheck = requirePermissionCheck;
    }

    public abstract Bundle handle(CallPacket_old rawData) throws Throwable;

    @Override
    public void throwOnPermissionCheck(Context context) { XSecurity.checkCaller(context); }

    @Override
    public boolean requiresCheck() { return requiresPermissionCheck; }

    public boolean isRequiresSingleThread() { return this.requiresSingleThread; }

    public String getName() { return name; }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof String))
            return false;
        String other = (String) obj;
        return this.getName().equals(other);
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .appendFieldLine("Name", name)
                .appendFieldLine("Requires UID Check", String.valueOf(requiresPermissionCheck))
                .appendFieldLine("Single Thread", String.valueOf(requiresSingleThread))
                .toString(true);
    }
}
