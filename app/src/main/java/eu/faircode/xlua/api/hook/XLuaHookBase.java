package eu.faircode.xlua.api.hook;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import eu.faircode.xlua.hooks.XHookUtil;

public class XLuaHookBase {
    public static ArrayList<XLuaHook> readHooks(Context context, String apk) throws IOException, JSONException {
        return XHookUtil.readHooks(apk);
    }
    private final static String TAG = "XLua.XHook";
    //we can just do a key exchange system

    protected Boolean builtin = false;
    protected String collection;
    protected String group;
    protected String name;
    protected String author;
    protected Integer version = 0;
    protected String description;

    protected String className;
    protected String resolvedClassName = null;
    protected String methodName;
    protected String[] parameterTypes;
    protected String returnType;

    protected Integer minSdk;
    protected Integer maxSdk;
    protected Integer minApk;
    protected Integer maxApk;
    protected String[] excludePackages;
    protected Boolean enabled;
    protected Boolean optional;
    protected Boolean usage;
    protected Boolean notify;

    protected String luaScript;

    protected String[] settings;

    public final static int FLAG_WITH_DB = 5;
    public final static int FLAG_WITH_LUA = 2; // =PARCELABLE_ELIDE_DUPLICATES

    public String getId() { return this.collection + "." + this.name; }

    public XLuaHookBase setCollection(String collection) { this.collection = collection; return this; }
    public XLuaHookBase setName(String name) { this.name = name; return this; }

    public boolean isBuiltin() {
        return this.builtin;
    }

    public String getCollection() { return collection; }

    public String getGroup() { return group; }
    public String getName() { return name; }
    public String getAuthor() { return author; }
    public Integer getVersion() { return version; }
    public String getDescription() { return description; }
    public String getClassName() { return className; }
    public String getResolvedClassName() { return (this.resolvedClassName == null ? this.className : this.resolvedClassName); }
    public String getMethodName() { return methodName; }
    public String[] getParameterTypes() { return parameterTypes; }
    public String getReturnType() { return returnType; }
    public Integer getMinSdk() { return minSdk; }
    public Integer getMaxSdk() { return maxSdk; }
    public Integer getMinApk() { return minApk; }
    public Integer getMaxApk() { return maxApk; }
    public String[] getExcludePackages() { return excludePackages; }
    public Boolean isEnabled() { return enabled; }
    public String[] getSettings() { return settings; }

    public void setIsEnabled(boolean isEnabled) {
        enabled = isEnabled;
    }

    public boolean isOptional() {
        return this.optional;
    }
    public boolean doUsage() {
        return this.usage;
    }
    public boolean doNotify() {
        return this.notify;
    }
    public String getLuaScript() {
        return this.luaScript;
    }

    public boolean containsQuery(String query, boolean checkSettings, boolean checkMethod, boolean checkClass, boolean falseIfGroupContains) {
        if(falseIfGroupContains && this.group != null && this.group.toLowerCase().contains(query))
            return false;
        if(this.name != null && this.name.toLowerCase().contains(query))
            return true;
        if(this.getId() != null && this.getId().toLowerCase().contains(query))
            return true;
        if(checkMethod && this.methodName != null && this.methodName.toLowerCase().contains(query))
            return true;
        if(checkClass && this.className != null && this.className.toLowerCase().contains(query))
            return true;
        if(checkSettings && this.settings != null && this.settings.length > 0) {
            for(String s : settings) {
                if(s.toLowerCase().contains(query))
                    return true;
            }
        }

        return false;
    }

    public boolean isAvailable(int versionCode) { return (versionCode >= this.minApk && versionCode <= maxApk); }
    public boolean isAvailable(String packageName, List<String> collection) {
        if (!collection.contains(this.collection))
            return false;

        if (!this.enabled)
            return false;

        if (Build.VERSION.SDK_INT < this.minSdk || Build.VERSION.SDK_INT > this.maxSdk)
            return false;

        if (packageName == null)
            return true;

        if (this.excludePackages == null)
            return true;

        boolean included = true;
        for (String excluded : this.excludePackages)
            if (Pattern.matches(excluded, packageName)) {
                included = false;
                break;
            }
        return included;
    }

    public void resolveClassName(Context context) {
        String rName = XHookUtil.resolveClassName(context, this.className);
        if(rName == null)
            return;
        this.resolvedClassName = rName;
    }

    public void validate() {
        if (TextUtils.isEmpty(this.collection))
            throw new IllegalArgumentException("collection missing");
        if (TextUtils.isEmpty(this.group))
            throw new IllegalArgumentException("group missing");
        if (TextUtils.isEmpty(this.name))
            throw new IllegalArgumentException("name missing");
        if (TextUtils.isEmpty(this.author))
            throw new IllegalArgumentException("author missing");
        if (TextUtils.isEmpty(this.className))
            throw new IllegalArgumentException("class name missing");
        if (parameterTypes == null)
            throw new IllegalArgumentException("parameter types missing");
        if (TextUtils.isEmpty(this.luaScript))
            throw new IllegalArgumentException("Lua script missing " + this.methodName + " " + this.className + " " + getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof XLuaHookBase))
            return false;
        XLuaHookBase other = (XLuaHookBase) obj;
        return this.getId().equals(other.getId());
    }

    @NonNull
    @Override
    public String toString() {
         return this.getId() + "@" + this.className + ":" + this.methodName;
    }
}
