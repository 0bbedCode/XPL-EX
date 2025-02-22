package eu.faircode.xlua.x.xlua.identity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.utilities.ContentValuesUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.xlua.PacketBase;
import eu.faircode.xlua.x.xlua.hook.GroupPacket;

/*
    ToDO: Ensure the "pro" app is aligned with these "new" changes and or Conform it to be
                We have "user" and "uid", start using "uid" deprecate "user"
                Though if they send through "user" as an actual "user" that 'may' be an issue but ofc if Category needs to be dynamically resolved
                For now treat "user" as "uid"
                Maybe have a 'force legacy flag' here ?
                We can make a 'interceptor' to 'intercept' 'pro' app Data, then 're - arrange' it
                For now we can render 'pro' useless
                Given that 'this' app managed actual DB transactions, we shall not need to worry if 'pro' is still stuck on old layout
                Make 'pro' helper stream shit / legacy structures / legacy flags


Old Original X-LUA Table Layout (Tables that use UID/USER/PKG)
    ==========assignment==========
        [package]
        [uid]
        ...
    ==========group==========
        [package]
        [uid]
        ...
    ==========setting==========
        [user]
        [category]
        ...


Target Wanted X-LUA Table Layout (Tables that use UID/USER/PKG)
   ==========assignment==========
        [user]
        [category]
        ...
    ==========group==========
        [user]
        [category]
        ...
    ==========setting==========
        [user]
        [category]
        ...

 */
public class UserIdentityIO {
    private static final String TAG = "XLua.UserIdentityStream";

    public static final int QUERY_LEGACY_FLAG = 0x1;


    public static final String FIELD_USER = "user";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_PACKAGE_NAME = "packageName";
    public static final String FIELD_PACKAGE = "package";

    public static final String FIELD_STRUCT = "user_identity";


    public static void populateFromCursor_group_assignment(Cursor c, PacketBase base, boolean isLegacy) {
        if(isLegacy) {
            String cat = CursorUtil.getString(c, UserIdentityIO.FIELD_PACKAGE);
            int uid = CursorUtil.getInteger(c, UserIdentityIO.FIELD_UID, -1);
            base.setUserIdentity(UserIdentity.fromUid(uid, cat));
        }
        else {
            base.setUserIdentity(UserIdentity.create(c));
        }
    }

    /*

    LETS KEEP IT SIMPLE (FOR NOW) and Create what is NEEDED, cut down on "fat"

    public static void populateFromQuery(String[] selection) { populateFromQuery(selection, 0); }
    public static void populateFromQuery(String[] selection, int flags) {
        if(!ArrayUtils.isValid(selection)) {
            Log.e(TAG, "Error reading User Identity from Selection Query Args, Args is Null or Empty!... Flags=" + flags);
            return;
        }

        //This comes from executing Intent of [query] nothing to do with DATABASES

        int uid = UserIdentity.DEFAULT_UID;
        String category = UserIdentity.GLOBAL_NAMESPACE;

        if(flags == QUERY_LEGACY_FLAG) {
            uid =
        }
    }

    public static SqlQuerySnake createQuerySnake(UserIdentity identity) { return createQuerySnake(identity, 0); }
    public static SqlQuerySnake createQuerySnake(UserIdentity identity, int flags) {
        if(flags == QUERY_LEGACY_FLAG) {
            return SqlQuerySnake.create()
                    .whereColumn(FIELD_PACKAGE, identity.getCategoryOrPackageName())
                    .whereColumn(FIELD_UID, identity.getUid());
        }

        //This can be relevant to Databases, but it MAY also be relevant to a [query] Intent ? be careful! double check this!
        if(flags == 0) {
            return SqlQuerySnake.create()
                    .whereColumn(FIELD_USER, identity.getUserId())
                    .whereColumn(FIELD_PACKAGE, identity.getCategoryOrPackageName());
        }
    }*/

    public static Bundle ensureBundle(Bundle b) {
        if(b != null && b.containsKey(FIELD_STRUCT)) {
            Bundle sub = b.getBundle(FIELD_STRUCT);
            return sub == null ? b : sub;
        }

        return b;
    }

    public static void toBundleEx(UserIdentity userIdentity, Bundle b) {
        if(userIdentity != null && b != null) {
            b.putString(FIELD_CATEGORY, userIdentity.getCategory());
            if(userIdentity.hasUid()) b.putInt(FIELD_UID, userIdentity.getUid());
            if(userIdentity.hasUserId()) b.putInt(FIELD_USER, userIdentity.getUserId(false));
        }
    }

    public static void fromBundleEx(Bundle b, UserIdentity userIdentity, boolean forceAsUid) {
        if(b != null && userIdentity != null) {
            Bundle extras = ensureBundle(b);
            String category = extras.getString(FIELD_CATEGORY, extras.getString(FIELD_PACKAGE, extras.getString(FIELD_PACKAGE_NAME, UserIdentity.GLOBAL_NAMESPACE)));
            userIdentity.setCategory(category);

            if(!forceAsUid) {
                int uid = extras.getInt(FIELD_UID, -1);
                if(uid > -1) userIdentity.setUid(uid);

                int userId = extras.getInt(FIELD_USER, -1);
                if(userId > -1) userIdentity.setUserId(userId);
            } else {
                int v = extras.getInt(FIELD_UID, extras.getInt(FIELD_USER, UserIdentity.DEFAULT_USER));
                userIdentity.setUid(v);
            }
        }
    }

    public static void fromBundleEx_old(Bundle b, UserIdentity userIdentity, boolean isUidNotUserId) {
        Bundle extras = ensureBundle(b);
        if(extras != null && userIdentity != null) {
            int v = extras.getInt(FIELD_UID, extras.getInt(FIELD_USER, UserIdentity.DEFAULT_USER));
            String category = extras.getString(FIELD_CATEGORY, extras.getString(FIELD_PACKAGE, extras.getString(FIELD_PACKAGE_NAME, UserIdentity.GLOBAL_NAMESPACE)));
            if(isUidNotUserId) userIdentity.setUid(v);
            else userIdentity.setUserId(v);
            userIdentity.setCategory(category);
        }
    }


    public static void populateFromContentValues(ContentValues cv, UserIdentity userIdentity) {
        if(cv != null && userIdentity != null) {
            userIdentity.setUserId(ContentValuesUtil.getInteger(cv, FIELD_USER));
            userIdentity.setCategory(ContentValuesUtil.getString(cv, FIELD_CATEGORY));
        }
    }

    public static void populateContentValues(UserIdentity userIdentity, ContentValues cv) {
        if(userIdentity != null && cv != null) {
            cv.put(FIELD_USER, userIdentity.getUserId(true));
            cv.put(FIELD_CATEGORY, userIdentity.getCategory());
        }
    }

    public static void populateFromBundle(Bundle b, UserIdentity userIdentity) {
        if(b.containsKey(FIELD_STRUCT))
            internalPopulateFromBundle(b.getBundle(FIELD_STRUCT), userIdentity);
        else
            internalPopulateFromBundle(b, userIdentity);
    }
    public static UserIdentity fromBundle(Bundle b) {
        UserIdentity userIdentity = new UserIdentity();
        if(b.containsKey(FIELD_STRUCT)) internalPopulateFromBundle(b.getBundle(FIELD_STRUCT), userIdentity);
        else internalPopulateFromBundle(b, userIdentity);
        return userIdentity;
    }

    public static void populateBundle(UserIdentity userIdentity, Bundle b) {
        if(userIdentity != null && b != null) {
            Bundle inst = new Bundle();
            internalPopulateToBundle(userIdentity, inst);
            b.putBundle(FIELD_STRUCT, inst);
        }
    }
    public static Bundle toBundle(UserIdentity userIdentity) {
        Bundle b = new Bundle();
        internalPopulateToBundle(userIdentity, b);
        return b;
    }

    private static void internalPopulateFromBundle(Bundle b, UserIdentity userIdentity) {
        if(b != null && userIdentity != null) {
            userIdentity.setUid(UserIdentity.DEFAULT_USER);
            userIdentity.setCategory(UserIdentity.GLOBAL_NAMESPACE);

            /* Consider User as UID for now, they are suppose to be sending it as UID not User */
            /* Pro app should not use (getSetting) so this should not be an issues (should not) */
            if(b.containsKey(FIELD_USER)) userIdentity.setUid(b.getInt(FIELD_USER));
            if(b.containsKey(FIELD_UID)) userIdentity.setUid(b.getInt(FIELD_UID));
            if(b.containsKey(FIELD_PACKAGE_NAME)) userIdentity.setCategory(b.getString(FIELD_PACKAGE_NAME));        //PackageName or Package
            if(b.containsKey(FIELD_CATEGORY)) userIdentity.setCategory(b.getString(FIELD_CATEGORY));

            if(DebugUtil.isDebug())
                Log.d(TAG, "Read User Identity Information from Bundle, Result=" + userIdentity);

        }
    }

    private static void internalPopulateToBundle(UserIdentity userIdentity, Bundle b) {
        if(userIdentity != null && b != null) {
            int uid = userIdentity.getUid();
            String category = userIdentity.getCategory();
            b.putInt(FIELD_UID, uid < 0 ? UserIdentity.DEFAULT_USER : uid);
            b.putString(FIELD_CATEGORY, category == null ? UserIdentity.GLOBAL_NAMESPACE : category);
        }
    }
}
