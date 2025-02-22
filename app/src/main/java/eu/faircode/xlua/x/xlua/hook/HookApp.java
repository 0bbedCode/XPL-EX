package eu.faircode.xlua.x.xlua.hook;

import androidx.annotation.NonNull;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;

public class HookApp {
    public static HookApp create(UserClientAppContext app) { return new HookApp(app.appUid, app.appPackageName); }
    public static HookApp create(int uid, String packageName) { return new HookApp(uid, packageName); }

    public final int uid;
    public final String packageName;
    public int getUserId() { return UserIdentityUtils.getUserId(uid); }
    public boolean isGlobal() { return UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName); }

    public HookApp(int uid, String packageName) {
        this.uid = uid;
        this.packageName = packageName;
    }

    public static boolean isGlobalApp(HookApp app) { return app == null || app.isGlobal(); }

    @NonNull
    @Override
    public String toString() {
        return "Uid=" + uid + " Pkg=" + packageName;
    }
}
