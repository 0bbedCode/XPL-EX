package eu.faircode.xlua.api.xstandard.command;

import android.database.Cursor;
import android.util.Log;

import java.util.concurrent.Callable;

import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.XPolicy;
import eu.faircode.xlua.api.xstandard.QueryCommandHandler;

public class TryQueryWrapper implements Callable<Cursor> {
    private static final String TAG = "XLua.TryQueryWrapper";

    private QueryPacket_old packet;
    private String packageName;
    private QueryCommandHandler handle;

    private boolean isRunning = false;
    private Throwable exception;

    public static TryQueryWrapper create(QueryPacket_old packet, QueryCommandHandler handler) { return new TryQueryWrapper(packet, handler); }

    public TryQueryWrapper(QueryPacket_old packet, QueryCommandHandler handle) {
        this.packet = packet;
        this.handle = handle;
    }

    public TryQueryWrapper(QueryPacket_old packet, String packageName, QueryCommandHandler handle) {
        this.packet = packet;
        this.packageName = packageName;
        this.handle = handle;
    }

    @Override
    public Cursor call() throws Exception {
        XPolicy policy = XPolicy.policyAllowRW();
        try {
            isRunning = true;
            return handle.handle(packet);
        }catch (Throwable e) {
            exception = e;
            Log.e(TAG, "Query Error: packet=" + packet + " command=" + handle.getName() + " \n" + e + "\n" + Log.getStackTraceString(e));
            XposedBridge.log("Query Error");
            return null;
        }finally {
            policy.revert();
            isRunning = false;
        }
    }

    public boolean isRunning() { return isRunning; }
    public boolean hasException() { return exception != null; }
    public Throwable getException() { return this.exception; }
}
