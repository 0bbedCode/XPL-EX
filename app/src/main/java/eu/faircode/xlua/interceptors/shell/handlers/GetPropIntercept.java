package eu.faircode.xlua.interceptors.shell.handlers;

import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.api.properties.MockPropSetting;
import eu.faircode.xlua.api.xstandard.interfaces.ICommandIntercept;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.shell.CommandInterceptor;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.Evidence;
import eu.faircode.xlua.utilities.ShellUtils;
import eu.faircode.xlua.utilities.StringUtil;

public class GetPropIntercept extends CommandInterceptor implements ICommandIntercept {
    private static final String TAG = "XLua.GetPropIntercept";

    public static final String GETPROP_INTERCEPT_SETTING = "intercept.shell.getprop.bool";
    public static final List<Character> ALLOWED_CHARS = Arrays.asList('.', '_');
    public static final List<String> PRIVILEGED_PROPS = Arrays.asList("persist.netd.stable_secret", "vendor.bluetooth.soc");

    public static boolean show(Integer code, String propertyName) {
        if(code == null || code != MockPropSetting.PROP_FORCE) return !PRIVILEGED_PROPS.contains(propertyName);
        return true;
    }

    @SuppressWarnings("unused")
    public GetPropIntercept() { this.command = "getprop"; this.setting = GETPROP_INTERCEPT_SETTING; }

    @Override
    public boolean interceptCommand(ShellInterception result) {
        if(result != null && result.isValid) {
            UserContextMaps maps = result.getUserMaps();
            if(maps != null) {
                if(!keepGoing(maps, GETPROP_INTERCEPT_SETTING)) return true;
                String low = result.getCommandLine();
                if(!StringUtil.isValidString(low)) {
                    Log.e(TAG, "Some how the String low is null or empty...");
                    return false;
                }

                if(low.equals(this.command)) {
                    createMap(result, maps);
                    return true;
                }else {
                    String after = StringUtil.startAtString(this.command, low);
                    boolean hasGrep = after.contains("grep");
                    List<String> parts = StringUtil.breakStringExtreme(after, true, true, ALLOWED_CHARS);
                    List<String> allowed = CollectionUtil.getVerifiedStrings(parts, false, ShellUtils.USELESS_COMMANDS);
                    if(BuildConfig.DEBUG)
                        Log.i(TAG, "Command Data after cleaning=" + allowed + "  command=" + this.command);

                    if(maps.isSettingsValid() && maps.isPropMapsValid()) {
                        if(!allowed.get(0).equals(this.command)) return false; //weird
                        if(allowed.size() == 1) {
                            createMap(result, maps);    //assume its the getprop command
                            return true;
                        }

                        List<String> withoutCommand = allowed.subList(1, allowed.size() - 1);
                        if(hasGrep) {
                            HashMap<String, String> toWrite = new HashMap<>();
                            for(String s : withoutCommand) {
                                for(Map.Entry<String, String> propMap : maps.getPropMaps().entrySet()) {
                                    String pName = propMap.getKey();
                                    if(pName.contains(s)) {
                                        Integer code = maps.getPropertySetting(pName);
                                        if(code != null) {
                                            if(code == MockPropSetting.PROP_HIDE || !show(code, s))
                                                continue;
                                        }

                                        toWrite.put(pName, maps.getSetting(propMap.getValue(), ""));
                                    }
                                }
                            }

                            if(!toWrite.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                for(Map.Entry<String, String> e : toWrite.entrySet())
                                    sb.append("[").append(e.getKey()).append("]: ").append("[").append(e.getValue()).append("]\r\n");

                                result.setNewValue(sb.toString());
                                result.setIsMalicious(true);
                                return true;
                            }
                        }else {
                            boolean cleanEmulator = maps.getSettingBool(Evidence.SETTING_QEMU_EMULATOR) || maps.getSettingBool(Evidence.SETTING_EMULATOR);
                            boolean cleanRoot = Str.toBoolean(maps.getSetting(Evidence.SETTING_ROOT, Str.FALSE));
                            for(String s : allowed) {
                                if(s == null || TextUtils.isEmpty(s)) continue;
                                if(maps.hasProperty(s)) {
                                    Log.w(TAG, "Command Data contains a Targeted Property! =" + s);
                                    if(cleanEmulator || cleanRoot) {
                                        if((cleanEmulator && Evidence.property(s, 1)) || (cleanRoot && Evidence.property(s, 2))) {
                                            result.setNewValue(Str.EMPTY);
                                            result.setIsMalicious(true);
                                            return true;
                                        }
                                        if(cleanRoot) {
                                            if(s.equalsIgnoreCase(Evidence.PROP_DEBUGGABLE)) {
                                                result.setNewValue(Evidence.PROP_DEBUGGABLE_GOOD);
                                                result.setIsMalicious(true);
                                                return true;
                                            }
                                            if(s.equalsIgnoreCase(Evidence.PROP_SECURE)) {
                                                result.setNewValue(Evidence.PROP_SECURE_GOOD);
                                                result.setIsMalicious(true);
                                                return true;
                                            }
                                        }
                                    }

                                    Integer code = maps.getPropertySetting(s);
                                    if(code != null) {
                                        if(code == MockPropSetting.PROP_SKIP) continue;
                                        if(code == MockPropSetting.PROP_HIDE || !show(code, s)) {
                                            result.setNewValue(Str.EMPTY);
                                            result.setIsMalicious(true);
                                            return true;
                                        }
                                    }

                                    result.setNewValue(maps.getSetting(s, Str.EMPTY));
                                    result.setIsMalicious(true);
                                    return true;
                                }
                            }
                        }

                        createMap(result, maps);
                        return true;
                    }
                }
            }
        } return false;
    }

    public void createMap(ShellInterception result, UserContextMaps maps) {
        if(BuildConfig.DEBUG)
            Log.w(TAG, "Mocking command [" + this.command + "] as full single input output");

        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> mappedProperty : maps.getPropMaps().entrySet()) {
            String propName = mappedProperty.getKey();
            if(propName == null || TextUtils.isEmpty(propName))
                continue;

            //if(sb.length() > 0)
            //    sb.append("\r\n");

            String settName = mappedProperty.getValue();
            Integer code = maps.getPropertySetting(propName);
            if(code != null) if(code == MockPropSetting.PROP_SKIP || code == MockPropSetting.PROP_HIDE) continue;
            if(!show(code, propName)) continue;
            sb.append("[").append(propName).append("]: [").append(maps.getSetting(settName, "")).append("]\r\n");
            //Ends with 0D 0A
        }

        if(BuildConfig.DEBUG)
            Log.d(TAG, "Fake " + this.command + " output:\n" + sb);

        result.setNewValue(sb.toString());
        result.setIsMalicious(true);
    }
}
