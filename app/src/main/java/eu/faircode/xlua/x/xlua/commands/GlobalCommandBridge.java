package eu.faircode.xlua.x.xlua.commands;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import de.robv.android.xposed.XC_MethodHook;
import eu.faircode.xlua.XLua;

import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.xlua.commands.call.AssignHooksCommand;
import eu.faircode.xlua.x.xlua.commands.call.ClearAppDataCommand;
import eu.faircode.xlua.x.xlua.commands.call.DropTableCommand;
import eu.faircode.xlua.x.xlua.commands.call.ForceStopAppCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetAppDirectoriesCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetAppInfoCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetBridgeVersionCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetDatabasePathCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetDatabaseStatusCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetGroupsCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetHookCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetProfileCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetProfileListCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetVersionExCommand;
import eu.faircode.xlua.x.xlua.commands.call.InitAssignments;
import eu.faircode.xlua.x.xlua.commands.call.PutAppProfileCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutAssignmentCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutConfigCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutHookExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.ReportCommand;
import eu.faircode.xlua.x.xlua.commands.call.SetAppProfileCommand;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.commands.query.GetAppsCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignedHooksExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignedHooksLegacyCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignmentsCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetConfigsCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetSettingsExCommand;
import eu.faircode.xlua.x.xlua.database.wrappers.XLuaDatabaseManager;
import eu.faircode.xlua.x.xlua.database.wrappers.XMocDatabaseManager;

public class GlobalCommandBridge {
    public static final CommandService luaCommandService = new CommandService("xlua", XLuaDatabaseManager.create(), 100);
    public static final CommandService mocCommandService = new CommandService("mock", XMocDatabaseManager.create(), 100);

    static {
        luaCommandService
                .registerCall(PutSettingExCommand.class)
                .registerCall(GetSettingExCommand.class)
                .registerCall(GetGroupsCommand.class)
                .registerCall(GetVersionExCommand.class)
                .registerCall(AssignHooksCommand.class)
                .registerCall(GetAppInfoCommand.class)

                .registerCall(GetDatabaseStatusCommand.class)
                .registerCall(GetBridgeVersionCommand.class)

                .registerCall(ReportCommand.class)

                .registerCall(DropTableCommand.class)
                .registerCall(PutHookExCommand.class)
                .registerCall(PutAssignmentCommand.class)

                .registerCall(GetHookCommand.class)
                .registerCall(InitAssignments.class)

                .registerCall(PutConfigCommand.class)

                .registerCall(ForceStopAppCommand.class)
                .registerCall(ClearAppDataCommand.class)

                .registerCall(GetDatabasePathCommand.class)
                .registerCall(PutAppProfileCommand.class)
                .registerCall(SetAppProfileCommand.class)
                .registerCall(GetProfileCommand.class)
                .registerCall(GetProfileListCommand.class)

                .registerCall(GetAppDirectoriesCommand.class)

                .registerQuery(GetAppsCommand.class, true)
                .registerQuery(GetHooksCommand.class, true)

                .registerQuery(GetAssignmentsCommand.class, true)

                .registerQuery(GetConfigsCommand.class, true)

                .registerQuery(GetAssignedHooksExCommand.class, true)
                .registerQuery(GetAssignedHooksLegacyCommand.class, true)
                .registerQuery(GetSettingsExCommand.class, true);

        //No Mock ones rn
    }

    public static Bundle vxpCall(Context context, String arg, Bundle extras, String method)  {
        CommandService cService = null;
        if(luaCommandService.isCommandPrefix(method)) cService = luaCommandService;
        else if(mocCommandService.isCommandPrefix(method)) cService = mocCommandService;
        else return null;

        CallPacket packet = new CallPacket(context, method, arg, extras, cService.databaseManager.getDatabase(context));
        return cService.handleCall(packet);
    }

    public static Cursor vxpQuery(Context context, String arg, String[] selection, String method)  {
        CommandService cService = null;
        if(luaCommandService.isCommandPrefix(method)) cService = luaCommandService;
        else if(mocCommandService.isCommandPrefix(method)) cService = mocCommandService;
        else return null;

        QueryPacket packet = new QueryPacket(context, method, arg, selection, cService.databaseManager.getDatabase(context));
        return cService.handleQuery(packet);
    }

    public static void handleQuery(XC_MethodHook.MethodHookParam param, String packageName) {
        QueryPacket packet = luaCommandService.tryCreateQueryPacket(param, packageName);
        if (packet != null) param.setResult(luaCommandService.handleQuery(packet));
        else {
            packet = mocCommandService.tryCreateQueryPacket(param, packageName);
            if(packet != null) param.setResult(mocCommandService.handleQuery(packet));
        }
    }

    public static void handeCall(XC_MethodHook.MethodHookParam param, String packageName)  {
        CallPacket packet = luaCommandService.tryCreateCallPacket(param, packageName);
        if(packet != null) {
            if(packet.getMethod().equalsIgnoreCase("getVersion")) param.setResult(BundleUtil.createSingleInt("version", XLua.version));
            else param.setResult(luaCommandService.handleCall(packet));
        }
        else {
            packet = mocCommandService.tryCreateCallPacket(param, packageName);
            if(packet != null) param.setResult(mocCommandService.handleCall(packet));
        }
    }
}
