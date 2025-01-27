package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;
import android.os.Binder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.app.XLuaApp;
import eu.faircode.xlua.api.xlua.provider.XLuaAppProvider;
import eu.faircode.xlua.api.xstandard.QueryCommandHandler;
import eu.faircode.xlua.api.xstandard.command.QueryPacket_old;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.hook.AppProviderApi;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

@SuppressWarnings("unused")
public class GetAppsCommand extends QueryCommandHandlerEx {

    public GetAppsCommand() { this.name = "getApps"; this.requiresSingleThread = true; this.requiresPermissionCheck = true; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        return CursorUtil.toMatrixCursor_final(
                AppProviderApi.getApps(commandData.getContext(),
                        commandData.getDatabase(),
                        Binder.getCallingUid(),
                        true,
                        true).values(),
                marshall,
                0);
    }

    public static Cursor invoke(Context context, boolean marshall) {
        return XProxyContent.luaQuery(
                context,
                marshall ? "getApps2" : "getApps");
    }


    public static List<AppXpPacket> get(Context context, boolean marshall) {
        return ListUtil.copyToArrayList(
                CursorUtil.readCursorAs_final(
                        XProxyContent.luaQuery(
                                context,
                                marshall ? "getApps2" : "getApps"),
                        marshall, AppXpPacket.class));
    }
}
