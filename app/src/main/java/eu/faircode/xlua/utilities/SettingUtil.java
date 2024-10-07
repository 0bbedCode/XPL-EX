package eu.faircode.xlua.utilities;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import eu.faircode.xlua.AppGeneric;
import eu.faircode.xlua.R;
import eu.faircode.xlua.TextDividerItemDecoration;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.api.xlua.XLuaCall;

public class SettingUtil {

    public static final List<String> EX_NAMES = Arrays.asList("Index", "Vortex");
    public static final List<String> UPPER_PAIRS = Arrays.asList("Stat", "Uuid", "Wifi", "Vpn", "Ex", "Io", "Su", "Arch", "Cpuid", "Db", "Egl", "Ab", "Gms",  "Hw", "Sms", "Icc", "No", "Sys", "Isp", "Cid", "Lac", "Mac", "Net", "Ad", "Drm", "Gsf", "Lcc", "Meid", "Imei", "Bssid", "Ssid", "Esim", "Sim", "Sku", "Lac", "Cid", "Msin", "Mnc", "Mcc", "Adb", "Os", "Utc", "Abi", "Gps", "Dns", "Vm", "Id", "Gsm", "Cpu", "Gpu", "Fp", "Rom", "Nfc", "Soc", "Url", "Dev", "Sdk", "Iso");
    public static final List<String> XP_SETTINGS = Arrays.asList("show", "value.imei", "value.meid", "value.email", "value.android_id", "value.serial", "value.phone_number", "collection", "theme", "restrict_new_apps", "notify_new_apps", "notify");

    public static boolean isBooleanTypeSetting(String settingName) { return settingName.endsWith(".bool") || settingName.contains(".bool.") || settingName.startsWith("bool."); }

    public static String generateDescription(LuaSettingExtended extended) {
        String desc = "N/A";
        if(extended == null)
            return desc;

        //ensure put for these will not change packageName
        if(StringUtil.isValidString(extended.getDescription()))
            desc = extended.getDescription();
        else {
            if(extended.getName() != null) {
                String m66bExtension = "(M66Bs) Built in Value for XPrivacyLua-Pro";
                String name = extended.getName();
                if(name.equalsIgnoreCase("theme"))
                    desc = "Theme Control Setting to Control XPL-EX Theme (pls leave) (Dark, Light)";
                else if(name.equalsIgnoreCase("collection"))
                    desc = "XPL-EX Collections, separating each Collection with a Comma. Collections specifically enabled within the PRO or Main App (if supported)";
                else if(name.equalsIgnoreCase("restrict_new_apps"))
                    desc = "Restrict new Apps when installed (only if LSPosed also did the same :P )";
                else if(name.equalsIgnoreCase("notify_new_apps"))
                    desc = "Notify User when a new Application is Installed";
                else if(name.equalsIgnoreCase("notify"))
                    desc = "Notify when a hook is used/invoked " + m66bExtension;
                else if(name.equalsIgnoreCase("value.android_id"))
                    desc = "Android ID " + m66bExtension;
                else if(name.equalsIgnoreCase("value.email"))
                    desc = "E-mail Address (account name) " + m66bExtension;
                else if(name.equalsIgnoreCase("value.imei"))
                    desc = "IMEI Number for GSM Devices " + m66bExtension;
                else if(name.equalsIgnoreCase("value.meid"))
                    desc = "MEID Number for CDMA Devices " + m66bExtension;
                else if(name.equalsIgnoreCase("value.phone_number"))
                    desc = "Device Phone Number " + m66bExtension;
                else if(name.equalsIgnoreCase("value.serial"))
                    desc = "Android Build Serial " + m66bExtension;
                else if(name.equalsIgnoreCase("lac,cid"))
                    desc = "Cell Location stuff " + m66bExtension + "Setting must match exactly like LAC,CID . for example:26550,187363073";
                else if(name.endsWith(".randomize"))
                    desc = "M66Bs flag to indicate Startup Randomization for: " + name.substring(0, name.length() - ".randomize".length());
            }
        }

        return desc;
    }

    public static TextDividerItemDecoration createSettingsDivider(Context context) {
        //Clean this bullshit up
        TextDividerItemDecoration dividerDecor = new TextDividerItemDecoration(context);
        dividerDecor.setUseIndependentDividers(false);
        dividerDecor.setBarCornerRadius(20);
        dividerDecor.setLeftBarToStartParentPadding(50);
        dividerDecor.setRightBarToEndParentPadding(50);
        dividerDecor.enableLineDivider(true, 85);
        dividerDecor.setDividerPosition(false);
        dividerDecor.setLinkDividersToGroupIDs(true);
        dividerDecor.setTextPaddingLeft(50);
        dividerDecor.initColors(context);
        dividerDecor.setDividerTopPadding(50);
        dividerDecor.setDividerBottomPadding(50);
        dividerDecor.setTextPaddingBottom(0);
        dividerDecor.setTextPaddingTop(0);
        dividerDecor.setTextVerticalAlignment(TextDividerItemDecoration.TextVerticalAlignment.CENTER);
        dividerDecor.setTextSize(65); // Set text size
        dividerDecor.setTextAlignment(Gravity.LEFT);
        return dividerDecor;
    }

    public static void sortSettings(List<LuaSettingExtended> settings) {
        if(settings != null && !settings.isEmpty()) {
            Collections.sort(settings, new Comparator<LuaSettingExtended>() {
                @Override
                public int compare(LuaSettingExtended o1, LuaSettingExtended o2) {
                    if(o1 == null || o2 == null || o1.getName() == null || o2.getName() == null)
                        return 0;

                    if (o1.isBuiltIntSetting() && !o2.isBuiltIntSetting())
                        return -1; // o1 comes before o2
                    else if (!o1.isBuiltIntSetting() && o2.isBuiltIntSetting())
                        return 1; // o2 comes before o1

                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
        }
    }

    public static boolean isBuiltInSetting(String settingName) {
        return XP_SETTINGS.contains(settingName) ||
                settingName.endsWith(".randomize") || settingName.equalsIgnoreCase("lac,cid"); }

    public static String handleLowerHalf(String name) {
        for (String s : UPPER_PAIRS)
            if(s.equals(name))
                return name.toUpperCase();

        if(name.equalsIgnoreCase("inputdevice"))
            return "InputDevice";

        if(name.equalsIgnoreCase("timezone"))
            return "TimeZone";

        if((!EX_NAMES.contains(name)) &&  name.endsWith("ex") && name.length() > 3)
            return name.substring(0, name.length() -2) + " EX";

        return name;
    }

    public static void initSettingTextColor(Context context, TextView tvSettingName, LuaSettingExtended setting) {
        if(tvSettingName == null || setting == null)
            return;

        tvSettingName.setTextColor(XUtil.resolveColor(context, !setting.isValueNull() && !setting.isModified() ? R.attr.colorAccent : R.attr.colorTextOne));
    }

    public static void initCardViewColor(Context context, TextView tvSettingName, CardView cv, LuaSettingExtended setting) {
        if(cv == null || setting == null)
            return;

        initSettingTextColor(context, tvSettingName, setting);
        cv.setCardBackgroundColor(XUtil.resolveColor(context, setting.isModified() ? R.attr.colorSystem : R.attr.cardBackgroundColor));
    }

    public static String replaceSettingType(String settingName, String settingType) {
        String s = "." + settingType + ".";
        for(int i = 0; i < 3; i ++) {
            if(i == 0 && settingName.contains(s))
                return settingName.replace(s, "");
            if(i == 1) {
                s = "." + settingType;
                if(settingName.contains(s)) return settingName.replace(s, "");
            }

            if(i == 2) {
                s = settingType + ".";
                if(settingName.contains(s)) return settingName.replace(s, "");
            }
        }

        return settingName;
    }

    public static String cleanSettingName(String settingName) {
        if (!StringUtil.isValidString(settingName))
            return "NULL";

        if(settingName.equalsIgnoreCase("lac,cid"))
            return "LAC CID";

        String lowered = settingName.toLowerCase().trim();
        if(isBooleanTypeSetting(lowered)) lowered = replaceSettingType(lowered, "bool");
        StringBuilder name = new StringBuilder();
        boolean started = true;
        StringBuilder low = new StringBuilder();

        for (int i = 0; i < lowered.length(); i++) {
            char c = lowered.charAt(i);
            if (c == ' ' || c == '_' || c == '.' || c == '\n' || c == '\0' || c == '\t' || c == ',') {
                if (low.length() > 0) {
                    if (name.length() > 0) name.append(" ");
                    name.append(handleLowerHalf(low.toString()));
                    low.setLength(0);  // Clear the StringBuilder
                    started = true;
                }
            } else {
                if (started) {
                    //we can use size of low but for speed we will use a boolean
                    started = false;
                    low.append(Character.toUpperCase(c));
                } else {
                    low.append(c);
                }
            }
        }

        if (low.length() > 0) {
            if (name.length() > 0) name.append(" ");
            name.append(handleLowerHalf(low.toString()));
        }

        return name.toString();
    }

    public XResult sendSetting(final Context context, final LuaSettingExtended setting, AppGeneric application, boolean deleteSetting, boolean forceKill) {
        final LuaSettingPacket packet = LuaSettingPacket.create(setting, LuaSettingPacket.getCodeInsertOrDelete(deleteSetting), forceKill)
                .copyIdentification(application);

        if(!deleteSetting) packet.setValueForce(setting.getModifiedValue());
        return XLuaCall.sendSetting(context, packet);

        /*executor.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    final XResult ret = XLuaCall.sendSetting(context, packet);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void run() {
                            if(ret.succeeded()) {
                                setting.updateValue();
                                SettingUtil.initCardViewColor(context, tvSettingName, cvSetting, setting);
                            }

                            //Toast.makeText(context, ret.getResultMessage(), Toast.LENGTH_SHORT).show();
                            //notifyDataSetChanged();
                        }
                    });
                }
            }
        });*/
    }


    public static void updateSetting(LuaSettingExtended setting, String newValue, HashMap<LuaSettingExtended, String> modified) {
        String originalValue = modified.get(setting);
        if(originalValue == null) {
            if((setting.getValue() != null && newValue != null) && !newValue.equalsIgnoreCase(setting.getValue())) {
                modified.put(setting, setting.getValue());
                setting.setValueForce(newValue);
            }
            else if(setting.getValue() == null && newValue != null) {
                modified.put(setting, "<nil>");
                setting.setValueForce(newValue);
            }
        }else if(originalValue.equalsIgnoreCase("<nil>") && newValue == null) {
            modified.remove(setting);
            setting.setValueForce(null);
        } else if(newValue == null) {
            setting.setValueForce(null);
        }
        else if(!originalValue.equalsIgnoreCase(newValue)) {
            setting.setValueForce(newValue);
        }else {
            modified.remove(setting);
            setting.setValueForce(originalValue);
        }
    }
}
