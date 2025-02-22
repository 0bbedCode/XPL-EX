package eu.faircode.xlua.x.xlua.commands.packet;

import android.content.Context;
import android.util.Log;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

/**
 *  dodo
 * TODO: Side, make a cool Reflection crawler thing? maybe cache like randomizer ?
 *                  obj -> invoke("", args) -> get("") -> invoke("", args)
 *                  Also look more into the Dynamic T Shit >:) you dont need "Class<T> clazz"
 */

public class QueryPacket extends BridgePacket {
    private static final String TAG = LibUtil.generateTag(QueryPacket.class);

    protected final String[] selection;
    private UserIdentity userIdentity;

    public int getExtras() { return ArrayUtils.getFromStringArray(selection, 3, UserIdentity.DEFAULT_USER); }

    public String getSelectionAt(int index) { return ArrayUtils.getElementAtIndexOrDefault(selection, index, null); }
    public String getSelectionAt(int index, String defaultValue) { return ArrayUtils.getElementAtIndexOrDefault(selection, index, defaultValue); }
    public String[] getSelection() { return selection; }

    public int getIntSelectionAt(int index) { return getIntSelectionAt(index, -1); }
    public int getIntSelectionAt(int index, int defaultValue) { return Str.tryParseInt(getSelectionAt(index, String.valueOf(defaultValue)), defaultValue); }

    public String getCategory() { return getCategory(false); }
    public String getCategory(boolean isCategoryFirst) { return getUserIdentification(isCategoryFirst).getCategory(); }

    public int getUid() { return getUid(false); }
    public int getUid(boolean isCategoryFirst) { return getUserIdentification(isCategoryFirst).getUid(); }

    public int getUserId() { return getUserId(false); }
    public int getUserId(boolean isCategoryFirst) { return getUserIdentification(isCategoryFirst).getUserId(true); }

    public String getLastSelection() { return selection != null ? selection[selection.length - 1] : null; }

    public boolean isDump() { return selection != null && ActionPacket.ACTION_DUMP.equalsIgnoreCase(getLastSelection()); }
    public boolean isAll() { return selection != null && ActionPacket.ACTION_ALL.equalsIgnoreCase(getLastSelection()); }

    public UserIdentity getUserIdentification() { return getUserIdentification(false); }
    public UserIdentity getUserIdentification(boolean isCategoryFirst) {
        if(userIdentity == null) {
            userIdentity = UserIdentity.fromUid(
                    ArrayUtils.getFromStringArray(selection, isCategoryFirst ? 1 : 0, UserIdentity.DEFAULT_USER),
                    getSelectionAt(isCategoryFirst ? 0 : 1, UserIdentity.GLOBAL_NAMESPACE));

        }

        return userIdentity;
    }

    public QueryPacket(Context context, String commandPrefix, String method, String[] selection, SQLDatabase database) { this(context, commandPrefix, method, selection, database, null); }
    public QueryPacket(Context context, String commandPrefix, String method, String[] selection, SQLDatabase database, String packageName) {
        super(commandPrefix, method, context, database, packageName);
        this.selection = selection;
    }

    public <T extends IQueryPacketObject> T readPacketAs(Class<T> clazz, int flags) {
        try {
            T itm = clazz.newInstance();
            itm.readSelectionFromQuery(selection, flags);
            return itm;
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Read Query Packet As Type:%s Flags:%s Error:%s Packet Info:%s", clazz, flags, e, Str.noNL(this)));
            return null;
        }
    }
}
