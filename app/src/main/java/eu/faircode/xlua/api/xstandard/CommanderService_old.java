package eu.faircode.xlua.api.xstandard;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.robv.android.xposed.XC_MethodHook;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.xstandard.command.QueryPacket_old;
import eu.faircode.xlua.api.xstandard.command.TryCallWrapper;
import eu.faircode.xlua.api.xstandard.command.TryQueryWrapper;
import eu.faircode.xlua.api.xstandard.interfaces.IBridgePacketContext;
import eu.faircode.xlua.api.xstandard.interfaces.IInitDatabase;
import eu.faircode.xlua.api.xstandard.interfaces.ISecurityObject;
import eu.faircode.xlua.utilities.DatabasePathUtil;

public class CommanderService_old {
    private static final String TAG = "XLua.CommandHandler";

    private final HashMap<String, CallCommandHandler> calls = new HashMap<>();
    private final HashMap<String, QueryCommandHandler> queries = new HashMap<>();

    private final String commandPrefix;
    private final ExecutorService executor;
    private final IInitDatabase database;

    public CommanderService_old(String commandPrefix, IInitDatabase database, int maxThreads) {
        this.commandPrefix = commandPrefix;
        this.database = database;
        executor = Executors.newFixedThreadPool(maxThreads);//Executors.newSingleThreadExecutor();
    }

    public boolean isCommandPrefix(String methodOrCommandPrefix) { return commandPrefix.equalsIgnoreCase(methodOrCommandPrefix); }
    public XDatabaseOld getDatabase(Context context) { return database.getDatabase(context); }

    public void throwOnPermissionCheck(IBridgePacketContext packet, ISecurityObject command) {
        if(command.requiresCheck()) {
            if(DebugUtil.isDebug())
                Log.w(TAG, "performing a security check on Caller Process, Ensure this function is on the Same thread as Caller / Received");

            command.throwOnPermissionCheck(packet.getContext());
            if(DebugUtil.isDebug())
                Log.i(TAG, "Finished performing security check! your good to go!");
        }
    }

    public Bundle handleCall(CallPacket_old packet) {
        if(packet == null) return null;
        try {
            CallCommandHandler command = calls.get(packet.getMethod());
            if(command == null) {
                Log.e(TAG, "Call Command could not be found! " + packet); return null;
            }

            if(DebugUtil.isDebug())
                Log.i(TAG, "Found Command Handler for Call = " + command + " packet=" + packet);

            throwOnPermissionCheck(packet, command);
            return command.requiresSingleThread ?
                    TryCallWrapper.create(packet, command).call() : executor.submit(TryCallWrapper.create(packet, command)).get();
        }catch (Exception e) {
            DatabasePathUtil.log("Failed to execute command call: " + packet + "\n" + e, true);
            return null;
        }
    }

    public Cursor handleQuery(QueryPacket_old packet) {
        if(packet == null) return null;
        try {
            QueryCommandHandler command = queries.get(packet.getMethod());
            if(command == null) {
                Log.e(TAG, "Query Command could not be found! " + packet); return null;
            }

            if(DebugUtil.isDebug()) Log.i(TAG, "Found Command Handler for Query = " + command + " packet=" + packet);
            throwOnPermissionCheck(packet, command);
            return command.requiresSingleThread ?
                    TryQueryWrapper.create(packet, command).call() : executor.submit(TryQueryWrapper.create(packet, command)).get();
        }catch (Exception e) {
            DatabasePathUtil.log("Failed to execute command query: " + packet + "\n" + e, true);
            return null;
        }
    }

    public CallPacket_old tryCreateCallPacket(XC_MethodHook.MethodHookParam param, String packageName) {
        try {
            String commandPrefix = (String)param.args[0];               //Also known as 'method'
            if(!commandPrefix.equalsIgnoreCase(this.commandPrefix))
                return null;

            Method mGetContext = param.thisObject.getClass().getMethod("getContext");
            Context context = (Context) mGetContext.invoke(param.thisObject);

            String method = (String) param.args[1];                     //sub method being invoked like "getSetting" also known as 'arg'
            Bundle extras = (Bundle) param.args[2];
            return new CallPacket_old(context, commandPrefix, method, extras, database.getDatabase(context), packageName);
        }catch (Exception e) {
            Log.e(TAG, "Failed to generate a Call packet: prefix="  + this.commandPrefix + " pkg=" + packageName + "\n" + e);
            return null;
        }
    }

    public QueryPacket_old tryCreateQueryPacket(XC_MethodHook.MethodHookParam param, String packageName) {
        try {
            String[] projection = (String[]) param.args[1];
            if (!(projection != null && projection.length > 0 && projection[0] != null) || !projection[0].startsWith(this.commandPrefix))
                return null;

            String[] selection = (String[]) param.args[3];

            Method mGetContext = param.thisObject.getClass().getMethod("getContext");
            Context context = (Context) mGetContext.invoke(param.thisObject);

            //coool.string
            String[] split = projection[0].split("\\.");
            String commandPrefix = split[0];   //Also known as 'method'
            String method = split[1];           //sub method being invoked like "getSetting" also known as 'arg'
            return new QueryPacket_old(context, commandPrefix, method, selection, database.getDatabase(context), packageName);
        }catch (Exception e) {
            Log.e(TAG, "Failed to generate a Query packet: prefix="  + this.commandPrefix + " pkg=" + packageName + "\n" + e);
            return null;
        }
    }

    public <TCall extends CallCommandHandler> CommanderService_old registerCall(Class<TCall> callCom) {
        try {
            TCall inst = callCom.newInstance();
            calls.put(inst.getName(), inst);
            return this;
        }catch (Exception e) {
            DatabasePathUtil.log("Failed to register Call Class Command: " + callCom.getName(), true);
            return this;
        }
    }

    public <TQuery extends QueryCommandHandler> CommanderService_old registerQuery(Class<TQuery> queryCom) { return registerQuery(queryCom, false); }
    public <TQuery extends QueryCommandHandler> CommanderService_old registerQuery(Class<TQuery> queryCom, boolean useMarshallAsWell) {
        try {
            TQuery inst = queryCom.newInstance();
            queries.put(inst.getName(), inst);
            if(useMarshallAsWell) {
                TQuery inst2 = queryCom.newInstance();
                inst2.setAsMarshallCommand();
                queries.put(inst2.getName(), inst2);
            }

            return this;
        }catch (Exception e) {
            DatabasePathUtil.log("Failed to register Query Class Command: " + queryCom.getName(), true);
            return this;
        }
    }
}
