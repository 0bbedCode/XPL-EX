package eu.faircode.xlua.x.xlua.commands;

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
import eu.faircode.xlua.XSecurity;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;
import eu.faircode.xlua.api.xstandard.CommanderService_old;
import eu.faircode.xlua.api.xstandard.QueryCommandHandler;
import eu.faircode.xlua.api.xstandard.command.CallPacket_old;
import eu.faircode.xlua.api.xstandard.command.QueryPacket_old;
import eu.faircode.xlua.utilities.DatabasePathUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.commands.packet.IBridgePacketCtx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.database.IDatabaseManager;

public class CommandService {
    private static final String TAG = "XLua.CommandService";

    public final String name;
    public final String commandPrefix;
    public final ExecutorService executor;
    public final IDatabaseManager databaseManager;

    public final HashMap<String, QueryCommandHandlerEx> queryCommands = new HashMap<>();
    public final HashMap<String, CallCommandHandlerEx> callCommands = new HashMap<>();

    public CommandService(String commandPrefix, IDatabaseManager databaseManager, int maxThreadCount) { this(commandPrefix, commandPrefix, databaseManager, maxThreadCount); }
    public CommandService(String name, String commandPrefix, IDatabaseManager databaseManager, int maxThreadCount) {
        this.name = name;
        this.commandPrefix = commandPrefix;
        this.executor = Executors.newFixedThreadPool(maxThreadCount);
        this.databaseManager = databaseManager;
    }

    public boolean isCommandPrefix(String prefix) { return commandPrefix.equalsIgnoreCase(prefix); }

    public void throwOnPermissionCheck(IBridgePacketCtx context, IXCommand command) {
        if(command.requiresPermissionCheck()) {
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Performing Security check on Command:%s", command.getCommandName()));

            XSecurity.checkCaller(context.getContext());
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Finished and Passed Security check on Command:%s", command.getCommandName()));
        }
    }

    public Bundle handleCall(CallPacket packet) {
        if(packet == null) return null;
        try {
            CallCommandHandlerEx command = callCommands.get(packet.getMethod());
            if(command == null)
                return null;

            throwOnPermissionCheck(packet, command);
            return command.requiresPermissionCheck ?
                    CallInvoker.create(packet, command).call() :
                    executor.submit(CallInvoker.create(packet, command)).get();
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to handle Command Call, error:%s  packet:%s", e, Str.noNL(packet)));
            return null;
        }
    }

    public Cursor handleQuery(QueryPacket packet) {
        if(packet == null) return null;
        try {
            QueryCommandHandlerEx command = queryCommands.get(packet.getMethod());
            if(command == null)
                return null;

            throwOnPermissionCheck(packet, command);
            return command.requiresSingleThread ?
                    QueryInvoker.create(packet, command).call() :
                    executor.submit(QueryInvoker.create(packet, command)).get();
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to handle Command Query, error:%s  packet:%s", e, Str.noNL(packet)));
            return null;
        }
    }

    public CallPacket tryCreateCallPacket(XC_MethodHook.MethodHookParam param, String packageName) {
        try {
            String commandPrefix = (String)param.args[0];               //Also known as 'method'
            if(!commandPrefix.equalsIgnoreCase(this.commandPrefix))
                return null;

            Method mGetContext = param.thisObject.getClass().getMethod("getContext");
            Context context = (Context) mGetContext.invoke(param.thisObject);

            String method = (String) param.args[1];                     //sub method being invoked like "getSetting" also known as 'arg'
            Bundle extras = (Bundle) param.args[2];
            return new CallPacket(context, commandPrefix, method, extras, databaseManager.getDatabase(context), packageName);
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to create Call Packet! Command Prefix:%s Package:%s Error:%s", commandPrefix, packageName, e));
            return null;
        }
    }

    public QueryPacket tryCreateQueryPacket(XC_MethodHook.MethodHookParam param, String packageName) {
        try {
            String[] projection = (String[]) param.args[1];
            if (!(projection != null && projection.length > 0 && projection[0] != null) || !projection[0].startsWith(this.commandPrefix))
                return null;

            String[] selection = (String[]) param.args[3];

            Method mGetContext = param.thisObject.getClass().getMethod("getContext");
            Context context = (Context) mGetContext.invoke(param.thisObject);

            String[] split = projection[0].split("\\.");
            String commandPrefix = split[0];                    //Also known as 'method'
            String method = split[1];                           //sub method being invoked like "getSetting" also known as 'arg'
            return new QueryPacket(context, commandPrefix, method, selection, databaseManager.getDatabase(context), packageName);
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to create Query Packet! Command Prefix:%s Package:%s Error:%s", commandPrefix, packageName, e));
            return null;
        }
    }


    public <TCall extends CallCommandHandlerEx> CommandService registerCall(Class<TCall> clazz) {
        try {
            TCall inst = clazz.newInstance();
            callCommands.put(inst.getCommandName(), inst);
            return this;
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Register Custom Call Command Handler! Type:%s Command Prefix:%s Error:%s", clazz, commandPrefix, e));
            return this;
        }
    }

    public <TQuery extends QueryCommandHandlerEx> CommandService registerQuery(Class<TQuery> clazz) { return registerQuery(clazz, false); }
    public <TQuery extends QueryCommandHandlerEx> CommandService registerQuery(Class<TQuery> clazz, boolean useMarshallAsWell) {
        try {
            TQuery inst = clazz.newInstance();
            queryCommands.put(inst.getCommandName(), inst);
            if(useMarshallAsWell) {
                TQuery inst2 = clazz.newInstance();
                inst2.setAsMarshallCommand();
                queryCommands.put(inst2.getCommandName(), inst2);
            }

            return this;
        }catch (Exception e) {

            return this;
        }
    }
}
