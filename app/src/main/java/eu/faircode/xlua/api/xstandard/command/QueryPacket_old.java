package eu.faircode.xlua.api.xstandard.command;

import android.content.Context;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XDatabaseOld;
import eu.faircode.xlua.api.xstandard.interfaces.IBridgePacketContext;
import eu.faircode.xlua.api.xstandard.interfaces.IUserPacket;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public class QueryPacket_old extends BridgePacket_old implements IBridgePacketContext {
    private static final String TAG = "XLua.QueryPacket";

    protected final String[] selection;
    public QueryPacket_old(Context context, String commandPrefix, String method, String[] selection, XDatabaseOld db) { this(context, commandPrefix, method, selection, db, null); }
    public QueryPacket_old(Context context, String commandPrefix, String method, String[] selection, XDatabaseOld db, String packageName) {
        super(commandPrefix, method, context, db, packageName);
        this.selection = selection;
    }

    public int getExtras() {
        if(!ArrayUtils.isValid(selection,3)) return 0;
        return Str.tryParseInt(selection[3]);
    }

    public UserIdentity getUserIdentification(boolean isCategoryFirst) {
        if(!ArrayUtils.isValid(selection, 2)) {
            if(DebugUtil.isDebug()) Log.d(TAG, "Error Reading Selection Command for User Id Info, Size=" + ArrayUtils.safeLength(selection));
            return UserIdentity.DEFAULT;
        }

        int uid = Str.tryParseInt(isCategoryFirst ? selection[1] : selection[0]);
        String category = isCategoryFirst ? selection[0] : selection[1];
        return new UserIdentity(uid, category);
    }

    public String[] getSelection() { return this.selection; }
    public <T extends IUserPacket> T readFullPacketFrom(Class<T> clazz, int flags) {
        try {
            T itm = clazz.newInstance();
            itm.readSelectionArgsFromQuery(selection, flags);
            return itm;
        }catch (Exception e) {
            Log.e(TAG, "Failed to read Selections args! flag=" + flags + " class type=" + clazz.getName() + " " + this + "\ne=" + e);
            return null;
        }
    }
}
