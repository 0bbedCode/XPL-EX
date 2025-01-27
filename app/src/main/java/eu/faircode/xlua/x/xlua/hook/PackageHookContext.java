package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import eu.faircode.xlua.api.properties.MockPropConversions;
import eu.faircode.xlua.api.xlua.XLuaQuery;
import eu.faircode.xlua.api.xmock.XMockQuery;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetSettingsExCommand;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public class PackageHookContext {
    public static PackageHookContext create(final XC_LoadPackage.LoadPackageParam lpparam, int uid, final Context context) { return new PackageHookContext(lpparam.packageName, uid, context); }

    public final String packageName;
    public final int uid;
    public final String temporaryKey;
    public final boolean useDefault;

    public final HashMap<String, String> settings = new HashMap<>();
    public final HashMap<String, Integer> buildPropSettings = new HashMap<>();
    public final HashMap<String, String> buildPropMaps = new HashMap<>();

    public PackageHookContext(String packageName, int uid, final Context context) {
        this.packageName = packageName;
        this.uid = uid;
        this.temporaryKey = UUID.randomUUID().toString();
        this.useDefault = GetSettingExCommand.getBool(context, GetSettingExCommand.SETTING_USE_DEFAULT, uid, packageName);

        settings.putAll(GetSettingsExCommand.getAsMap(
                context,
                true,
                UserIdentity.DEFAULT_USER,
                UserIdentity.GLOBAL_NAMESPACE,
                GetSettingsExCommand.FLAG_ONE));

        settings.putAll(GetSettingsExCommand.getAsMap(
                context,
                true,
                uid,
                packageName,
                GetSettingsExCommand.FLAG_ONE));

        initRandomizers();
        //final Map<String, Integer> propSettings = MockPropConversions.toMap(XMockQuery.getModifiedProperties(context, uid, pName));
        //final Map<String, String> propMaps = XMockQuery.getMockPropMapsMap(context, true, settings, false);
    }

    private void initRandomizers() {
        //Init Randomizers
        //Randomize %random% Values
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("PackageName", this.packageName)
                .appendFieldLine("UID", this.uid)
                .appendFieldLine("Temp Key", this.temporaryKey)
                .appendFieldLine("Use Default", this.useDefault)
                .appendFieldLine("Settings Count", this.settings.size())
                .appendFieldLine("Build Prop Settings Count", this.buildPropSettings.size())
                .appendFieldLine("Build Prop Settings Map Count", this.buildPropMaps.size())
                .toString(true);
    }
}
