/*
    This file is part of XPrivacyLua.

    XPrivacyLua is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    XPrivacyLua is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with XPrivacyLua.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2017-2019 Marcel Bokhorst (M66B)
 */

package eu.faircode.xlua;

import android.app.ActivityManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.system.StructStat;
import android.system.StructTimespec;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputDevice;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

import de.robv.android.xposed.XC_MethodHook;
import eu.faircode.xlua.api.properties.MockPropSetting;
import eu.faircode.xlua.api.xmock.XMockCall;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.ShellIntercept;
import eu.faircode.xlua.interceptors.shell.util.RandomDateHelper;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.random.randomizers.RandomMediaCodec;
import eu.faircode.xlua.random.randomizers.RandomMediaCodecInfo;
import eu.faircode.xlua.rootbox.XReflectUtils;
import eu.faircode.xlua.tools.BytesReplacer;
import eu.faircode.xlua.utilities.Evidence;
import eu.faircode.xlua.utilities.ListFilterUtil;
import eu.faircode.xlua.utilities.CollectionUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.MemoryUtilEx;
import eu.faircode.xlua.utilities.MockFileUtil;
import eu.faircode.xlua.display.MotionRangeUtil;
import eu.faircode.xlua.utilities.FileUtil;
import eu.faircode.xlua.utilities.LuaLongUtil;
import eu.faircode.xlua.utilities.MemoryUtil;
import eu.faircode.xlua.utilities.NetworkUtil;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.utilities.ReflectUtilEx;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.utilities.MockUtils;
import eu.faircode.xlua.utilities.reflect.DynamicField;

public class XParam {
    private static final List<String> ALLOWED_PACKAGES = Arrays.asList("google", "system", "settings", "android", "webview");
    private static final String TAG = "XLua.XParam";

    private final Context context;
    private final Field field;
    private final XC_MethodHook.MethodHookParam param;
    private final Class<?>[] paramTypes;
    private final Class<?> returnType;
    private final Map<String, String> settings;
    private final Map<String, Integer> propSettings;
    private final Map<String, String> propMaps;
    private final String key;
    private final boolean useDefault;
    private final String packageName;

    private static final Map<Object, Map<String, Object>> nv = new WeakHashMap<>();
    private UserContextMaps getUserMaps() { return new UserContextMaps(this.settings, this.propMaps, this.propSettings); }

    // Field param
    public XParam(
            Context context,
            Field field,
            Map<String, String> settings,
            Map<String, Integer> propSettings,
            Map<String, String> propMaps,
            String key,
            boolean useDefault,
            String packageName) {
        this.context = context;
        this.field = field;
        this.param = null;
        this.paramTypes = null;
        this.returnType = field.getType();
        this.settings = settings;
        this.propSettings = propSettings;
        this.propMaps = propMaps;
        this.key = key;
        this.useDefault = useDefault;
        this.packageName = packageName;
    }

    // Method param
    public XParam(
            Context context,
            XC_MethodHook.MethodHookParam param,
            Map<String, String> settings,
            Map<String, Integer> propSettings,
            Map<String, String> propMaps,
            String key,
            boolean useDefault,
            String packageName) {


        this.context = context;
        this.field = null;
        this.param = param;
        if (param.method instanceof Constructor) {
            this.paramTypes = ((Constructor) param.method).getParameterTypes();
            this.returnType = null;
        } else {
            this.paramTypes = ((Method) param.method).getParameterTypes();
            this.returnType = ((Method) param.method).getReturnType();
        }
        this.settings = settings;
        this.propSettings = propSettings;
        this.propMaps = propMaps;
        this.key = key;
        this.useDefault = useDefault;
        this.packageName = packageName;
    }

    //
    //Start of FILTER Functions
    //

    @SuppressWarnings("unused")
    public boolean isDriverFiles(String pathOrFile) { return FileUtil.isDeviceDriver(pathOrFile); }

    @SuppressWarnings("unused")
    public String filterBuildProperty(String property) {
        if(property == null || MockUtils.isPropVxpOrLua(property))
            return MockUtils.NOT_BLACKLISTED;

        if(BuildConfig.DEBUG)
            Log.i(TAG, "Filtering Property=" + property + " prop maps=" + propMaps.size() + " settings size=" + settings.size());

        Integer code = propSettings.get(property);
        if(code != null) {
            if(code == MockPropSetting.PROP_HIDE) return null;//return MockUtils.HIDE_PROPERTY;
            if(code == MockPropSetting.PROP_SKIP) return MockUtils.NOT_BLACKLISTED;
            else {
                if(code != MockPropSetting.PROP_FORCE) {
                    try {
                        Object res = getResult();
                        if(res == null) return MockUtils.NOT_BLACKLISTED;
                    } catch (Throwable ignore) {  }
                }
            }
        }

        boolean cleanEmulator = getSettingBool(Evidence.SETTING_QEMU_EMULATOR, false) || getSettingBool(Evidence.SETTING_EMULATOR, false);
        boolean cleanRoot = getSettingBool(Evidence.SETTING_ROOT, false);
        if(cleanEmulator || cleanRoot) {
            if(cleanEmulator && Evidence.property(property, 1))
                return null;
            if(cleanRoot) {
                if(Evidence.property(property, 2))
                    return null;
                if(property.equalsIgnoreCase(Evidence.PROP_DEBUGGABLE))
                    return Evidence.PROP_DEBUGGABLE_GOOD;
                if(property.equalsIgnoreCase(Evidence.PROP_SECURE))
                    return Evidence.PROP_SECURE_GOOD;
            }
        }

        //add force options now
        if(propMaps != null && !propMaps.isEmpty()) {
            String settingName = propMaps.get(property);
            if(settingName != null) {
                if(settings.containsKey(settingName))
                    return getSetting(settingName, null);
            }
        }

        return MockUtils.NOT_BLACKLISTED;
    }
    //
    //
    //CLEAN LOGGING BEFORE FINAL RELEASE
    //
    //

    @SuppressWarnings("unused")
    public String filterBinderProxyAfter(String filterKind) {
        try {
            Object ths = getThis();
            Method mth = XReflectUtils.getMethodFor("android.os.BinderProxy", "getInterfaceDescriptor");
            if (ths == null || mth == null) throw new RuntimeException("No such Member [getInterfaceDescriptor] for Binder proxy and or the current Instance is NULL!");

            String interfaceName = (String) mth.invoke(ths);
            if (!Str.isValidNotWhitespaces(interfaceName)) throw new RuntimeException("Invalid Interface Name for the Binder Proxy! Method has failed to invoke...");

            int code = (int) getArgument(0);
            Parcel data = (Parcel) getArgument(1);
            Parcel reply = (Parcel) getArgument(2);

            if("adid".equalsIgnoreCase(filterKind)) {
                if ("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService".equalsIgnoreCase(interfaceName)) {
                    Log.i(TAG, "Filtering [" + interfaceName + "] Result...");
                    try {
                        //this is way to extra tbh we can MAJOR shorten this even combine it into one function
                        //Kinda of pissing me off how fucking dumb this looks
                        boolean wasSuccessful = (boolean) getResult();
                        if (!wasSuccessful) {
                            Log.e(TAG, "Result of Binder Transaction was not Successful (not a XPL-EX issue) returning [" + interfaceName + "]");
                            return null;
                        }

                        byte[] bytes = reply.marshall();
                        reply.setDataPosition(0);
                        if (bytes == null) {
                            Log.e(TAG, "Raw Data from the Parcel is Null [" + interfaceName + "]");
                            return null;
                        }

                        Log.i(TAG, "Raw Data From the Reply Parcel, Size: " + bytes.length + " Data: " + Str.bytesToHex(bytes));
                        if (bytes.length != 84) {
                            Log.e(TAG, "Raw Data Length of AD ID Data is not Equal to (84), it was " + bytes.length + " Skipping Replacement...");
                            return null;
                        }

                        reply.readException();  //Read Exception as it should always Write Exception on the Reply Parcel
                        String realAdId = reply.readString();
                        String fakeAdId = getSetting("unique.google.advertising.id");
                        if (TextUtils.isEmpty(fakeAdId) || realAdId == null) {
                            Log.e(TAG, "Real AD ID Result or the Fake one From Settings [unique.google.advertising.id] is Null or Empty...");
                            return null;
                        }

                        if (fakeAdId.length() < 5 || realAdId.length() < 5) {
                            Log.e(TAG, "The Size of the AD ID either Real or Fake is not correct: Real Size=" + realAdId.length() + " Fake Size=" + fakeAdId.length());
                            return null;
                        }

                        if(BuildConfig.DEBUG)
                            Log.i(TAG, "Real AD ID:" + realAdId + "\n" + Str.toHex(realAdId) + "\nReplacing With: " + fakeAdId + "\n" + Str.toHex(fakeAdId));


                        byte[] realAdIdBytes = realAdId.getBytes(StandardCharsets.UTF_16);
                        realAdIdBytes = Arrays.copyOfRange(realAdIdBytes, 2, realAdIdBytes.length);

                        //Why are we skipping two bytes ?

                        byte[] fakeAdIdBytes = fakeAdId.getBytes(StandardCharsets.UTF_16);
                        fakeAdIdBytes = Arrays.copyOfRange(fakeAdIdBytes, 2, fakeAdIdBytes.length);

                        if(BuildConfig.DEBUG)
                            Log.i(TAG, "Replacing AD ID Bytes Now:\nFrom=" + Str.bytesToHex(realAdIdBytes) + "\nTo=" + Str.bytesToHex(fakeAdIdBytes));

                        //Ensure its the same size
                        BytesReplacer bytesReplacer = new BytesReplacer(realAdIdBytes, fakeAdIdBytes);
                        byte[] newBytes = bytesReplacer.replace(bytes);

                        if(BuildConfig.DEBUG)
                            Log.i(TAG, "Replaced Bytes new Bytes: " + Str.bytesToHex(newBytes) + "\nOld Bytes:" + Str.bytesToHex(bytes));

                        reply.unmarshall(newBytes, 0, newBytes.length);
                        if(BuildConfig.DEBUG)
                            Log.i(TAG, "Finished Replacing AD ID Bytes...");

                        return realAdId;
                    } catch (Exception e) {
                        Log.e(TAG, "Error Filtering Interface Transact Result: " + interfaceName + " Error: " + e);
                        return null;
                    } finally {
                        reply.setDataPosition(0);
                    }
                }
            } else if("samad".equalsIgnoreCase(filterKind) || "levad".equalsIgnoreCase(filterKind)) {
                if("com.samsung.android.deviceidservice.IDeviceIdService".equalsIgnoreCase(interfaceName) || "com.zui.deviceidservice.IDeviceidInterface".equalsIgnoreCase(interfaceName)) {
                    if(code == 1) {
                        //Fuck the check
                        try {
                            reply.setDataPosition(0);
                            reply.readException();
                            String realId = reply.readString();
                            String fakeId = getSetting("unique.open.anon.advertising.id");

                            if(TextUtils.isEmpty(realId) || TextUtils.isEmpty(fakeId)) {
                                XLog.e(TAG, "Real ID or Fake ID for Samsung/Lenovo Spoof is empty or null..");
                                return null;
                            }

                            reply.setDataPosition(0);
                            byte[] bytes = reply.marshall();

                            byte[] realAdIdBytes = realId.getBytes(StandardCharsets.UTF_16);
                            realAdIdBytes = Arrays.copyOfRange(realAdIdBytes, 2, realAdIdBytes.length);

                            byte[] fakeAdIdBytes = fakeId.getBytes(StandardCharsets.UTF_16);
                            fakeAdIdBytes = Arrays.copyOfRange(fakeAdIdBytes, 2, fakeAdIdBytes.length);

                            BytesReplacer bytesReplacer = new BytesReplacer(realAdIdBytes, fakeAdIdBytes);
                            byte[] newBytes = bytesReplacer.replace(bytes);
                            reply.unmarshall(newBytes, 0, newBytes.length);

                            return realId;
                        } catch (Exception e) {
                            XLog.e(TAG, "Failed Intercepting Samsung/Lenovo ID Service: " + e);
                        } finally {
                            reply.setDataPosition(0);
                        }
                    }
                }
            }
            else if("asusad".equalsIgnoreCase(filterKind)) {
                if("com.asus.msa.SupplementaryDID.IDidAidlInterface".equalsIgnoreCase(interfaceName)) {
                    if(code == 3) {
                        try {
                            reply.setDataPosition(0);
                            reply.readException();
                            String realId = reply.readString();
                            String fakeId = getSetting("unique.open.anon.advertising.id");

                            if(TextUtils.isEmpty(realId) || TextUtils.isEmpty(fakeId)) {
                                XLog.e(TAG, "Real ID or Fake ID for Asus Spoof is empty or null..");
                                return null;
                            }

                            reply.setDataPosition(0);
                            byte[] bytes = reply.marshall();

                            byte[] realAdIdBytes = realId.getBytes(StandardCharsets.UTF_16);
                            realAdIdBytes = Arrays.copyOfRange(realAdIdBytes, 2, realAdIdBytes.length);

                            byte[] fakeAdIdBytes = fakeId.getBytes(StandardCharsets.UTF_16);
                            fakeAdIdBytes = Arrays.copyOfRange(fakeAdIdBytes, 2, fakeAdIdBytes.length);

                            BytesReplacer bytesReplacer = new BytesReplacer(realAdIdBytes, fakeAdIdBytes);
                            byte[] newBytes = bytesReplacer.replace(bytes);
                            reply.unmarshall(newBytes, 0, newBytes.length);

                            return realId;
                        } catch (Exception e) {
                            XLog.e(TAG, "Failed Intercepting Asus ID Service: " + e);
                        } finally {
                            reply.setDataPosition(0);
                        }
                    }
                }
            }
        }catch (Throwable e) {
            Log.e(TAG, "Failed to get Result / Transaction! after Error:"  + e);
        } return null;
    }

    @SuppressWarnings("unused")
    public void spoofLastModified() {
        try {
            //Look I am getting tired of this fucking xplex old base xD
            setResult(RandomDateHelper.generateLastModified());
        }catch (Throwable ignored) { }
    }

    @SuppressWarnings("unused")
    public StructStat cleanStructStat() {
        try {
            StructStat stat = (StructStat) getResult();
            if(stat == null) return null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                String[] fieldsOne = new String[] { "st_mtim", "st_atim", "st_ctim" };
                for(String f : fieldsOne) {
                    DynamicField field = new DynamicField(StructStat.class, f)
                            .setAccessible(true)
                            .bindInstance(stat);

                    if(field.isValid()) {
                        try {
                            StructTimespec inst = field.tryGetValueInstance();
                            if(inst != null) {
                                field.trySetValueInstance(new StructTimespec(
                                        inst.tv_sec + ThreadLocalRandom.current().nextInt(100, 80000),
                                        inst.tv_nsec + ThreadLocalRandom.current().nextInt(100, 80000)));
                            }
                        }catch (Exception ignored) { }
                    }
                }
            }

            String[] fields = new String[] { "st_atime", "st_ctime", "st_dev", "st_ino", "st_rdev" };
            for(String f : fields) {
                DynamicField field = new DynamicField(StructStat.class, f)
                        .setAccessible(true)
                        .bindInstance(stat);

                if(field.isValid()) {
                    try {
                        long val = field.tryGetValueInstance();
                        field.trySetValueInstance(val +  ThreadLocalRandom.current().nextInt(100, 80000));
                    }catch (Exception ignored) { }
                }
            }

            return stat;
        }catch (Throwable ignored) { }
        return null;
    }


    //public boolean filterCall(String )

    //public boolean filterContentProviderClientCall() {
    //}

    @SuppressWarnings("unused")
    public boolean filterSettingsCall(String setting, String newValue) {
        try {
            //This is called always, URI calls to this one
            // public final @Nullable Bundle call(@NonNull String authority, @NonNull String method,
            //            @Nullable String arg, @Nullable Bundle extras)

            //Bundle callResult = context.getContentResolver().call(
            // Uri.parse("content://settings/secure"), "GET_secure", "android_id", new Bundle()
            //);
            //String androidIdValue = callResult.getString("value");
            //content://settings/secure
            Object authObj = getArgument(0);
            String auth = authObj instanceof Uri ? ((Uri) authObj).getAuthority() : (String)authObj;
            if(auth == null) return false;
            auth = auth.toLowerCase();
            if(auth.contains("settings")) {
                //&& (auth.contains("secure") || auth.contains("global")
                String method = (String)getArgument(1);
                if("GET_secure".equalsIgnoreCase(method)) {
                    String arg = (String)getArgument(2);
                    if(arg == null) return false;
                    //Log.w(TAG, "Hello this is ObbedCode its weird this app is using Such Methods to get Secure Setting, please report App to ObbedCode! Setting:" + arg);
                    Object res = getResult();
                    if(res == null) return false;
                    Bundle resBundle = (Bundle) res;
                    if(!resBundle.containsKey("value")) return false;
                    String value = resBundle.getString("value");
                    if(value == null || value.isEmpty()) return false;
                    if(setting.contains("|")) {
                        String[] parts = setting.split("\\|");
                        for(String p : parts) {
                            if(p.trim().equalsIgnoreCase(arg)) {
                                Bundle fakeResult = new Bundle();
                                fakeResult.putString("value", newValue);
                                setResult(fakeResult);
                                return true;
                            }
                        }
                    }
                    else if(setting.equals("*") || setting.equalsIgnoreCase(arg)) {
                        Bundle fakeResult = new Bundle();
                        fakeResult.putString("value", newValue);
                        setResult(fakeResult);
                        return true;
                    }
                }
            }
        }catch (Throwable e) {
            Log.e(TAG, "Filter SettingsSecure IPC Error: " + e.getMessage());
        }
        return false;
    }

    @SuppressWarnings("unused")
    public boolean filterSettingsSecure(String setting, String newValue) {
        try {
            Object arg = getArgument(1);
            if(setting != null && newValue != null && arg instanceof String) {
                String set = ((String)arg);
                if(setting.contains("|")) {
                    String[] parts = setting.split("\\|");
                    for(String p : parts) {
                        if(p.trim().equalsIgnoreCase(set)) {
                            setResult(newValue);
                            return true;
                        }
                    }
                }
                else if(setting.equalsIgnoreCase(set)) {
                    setResult(newValue);
                    return true;
                }
            }
        }catch (Throwable e) {
            Log.e(TAG, "Filter SettingsSecure Error: " + e.getMessage());
        }
        return false;
    }

    //
    //End of FILTER Functions
    //

    @SuppressWarnings("unused")
    public boolean isDriverFile(String path) { return FileUtil.isDeviceDriver(path); }

    @SuppressWarnings("unused")
    public File[] filterFilesArray(File[] files) { return FileUtil.filterFileArray(files); }

    @SuppressWarnings("unused")
    public List<File> filterFilesList(List<File> files) { return FileUtil.filterFileList(files); }

    @SuppressWarnings("unused")
    public String[] filterFileStringArray(String[] files) { return FileUtil.filterArray(files); }

    @SuppressWarnings("unused")
    public List<String> filterFileStringList(List<String> files) { return FileUtil.filterList(files); }

    @SuppressWarnings("unused")
    public boolean isPackageAllowed(String str) {
        if(str == null || str.isEmpty() || str.equalsIgnoreCase(this.packageName)) return true;
        str = str.toLowerCase().trim();
        String blackSetting = getSetting("applications.blacklist.mode.bool");
        if(blackSetting != null) {
            boolean isBlacklist = StringUtil.toBoolean(blackSetting, false);
            if(isBlacklist) {
                String block = getSetting("applications.block.list");
                if(Str.isValidNotWhitespaces(block)) {
                    block = block.trim();
                    if(block.contains(",")) {
                        String[] blocked = block.split(",");
                        for(String b : blocked)
                            if(b.trim().equalsIgnoreCase(str))
                                return false;
                    }else {
                        if(block.equalsIgnoreCase("*"))
                            return false;
                        else if(block.equalsIgnoreCase(str))
                            return false;
                    }
                }
            } else {
                String allow = getSetting("applications.allow.list");
                if(Str.isValidNotWhitespaces(allow)) {
                    allow = allow.trim();
                    if(allow.contains(",")) {
                        String[] allowed = allow.split(",");
                        for(String a : allowed)
                            if(a.trim().equalsIgnoreCase(str))
                                return true;
                    }else {
                        if(allow.equalsIgnoreCase("*"))
                            return true;
                        else if(allow.equalsIgnoreCase(str))
                            return true;
                    }
                }
                if(Evidence.packageName(str, 3))
                    return false;

                for(String p : ALLOWED_PACKAGES)
                    if(str.contains(p))
                        return true;

                return false;
            }
        } return !Evidence.packageName(str, 3);
    }

    //
    //Shell Intercept
    //

    @SuppressWarnings("unused")
    public ShellInterception createShellContext(boolean isProcessBuilder) { return new ShellInterception(this, isProcessBuilder, getUserMaps()); }

    @SuppressWarnings("unused")
    public String ensureCommandIsSafe(ShellInterception shellData) {
        if(shellData == null) return null;
        ShellInterception res = ShellIntercept.intercept(shellData);
        if(!res.isMalicious() || res.getNewValue() == null) return null;
        if(!returnType.equals(Process.class)) return null;
        try {
            setResult(res.getEchoProcess());
            return res.getNewValue();
        }catch (Throwable e) {
            Log.e(TAG, "Error Setting the new Intercepted Process Command Value " + e);
            return null;
        }
    }

    /*@SuppressWarnings("unused")
    public String interceptCommand(String command) {
        //We would accept args and even do some of this in LUAJ but LUAJ LOVES and I mean LOVES to Gas light us
        //Its one thing for me as the programmer to make a mistake another thing when LUAJ just lies to me and wont work
        if(command != null) {
            try {
                ShellInterception res = ShellIntercept.intercept(ShellInterception.create(command, getUserMaps()).setProcess(getResult()));
                if(res != null && res.isMalicious()) {
                    if(res.getNewValue() != null) {
                        if(BuildConfig.DEBUG) {
                            Log.w(TAG, "Command Intercepted: " + command);
                            Log.w(TAG, "Replacing Command with: " + res.getNewValue());
                        }

                        if(returnType.equals(Process.class))
                            setResult(res.getEchoProcess());

                        return res.getNewValue();
                    }
                }
            }catch (Throwable e) {
                Log.e(TAG, "Failed to intercept e=" + e);
            }
        } return null;
    }

    @SuppressWarnings("unused")
    public String interceptCommandArray(String[] commands) {
        if(commands != null) {
            try {
                ShellInterception res = ShellIntercept.intercept(ShellInterception.create(commands, getUserMaps()).setProcess(getResult()));
                if(res != null && res.isMalicious()) {
                    if(res.getNewValue() != null) {
                        if(BuildConfig.DEBUG) {
                            Log.w(TAG, "Command Intercepted: " + joinArray(commands));
                            Log.w(TAG, "Replacing Command with: " + res.getNewValue());
                        }

                        if(returnType.equals(Process.class))
                            setResult(res.getEchoProcess());

                        return res.getNewValue();
                    }else {
                        Log.e(TAG, "[getNewValue] is NULL!");
                    }
                }
            }catch (Throwable e) {
                Log.e(TAG, "Failed to intercept e=" + e);
            }
        } return null;
    }

    @SuppressWarnings("unused")
    public String interceptCommandList(List<String> commands) {
        if(commands != null) {
            try {
                ShellInterception res = ShellIntercept.intercept(ShellInterception.create(commands, getUserMaps()).setProcess(getResult()));
                if(res != null && res.isMalicious()) {
                    if(res.getNewValue() != null) {
                        if(BuildConfig.DEBUG) {
                            Log.w(TAG, "Command Intercepted: " + joinList(commands));
                            Log.w(TAG, "Replacing Command with: " + res.getNewValue());
                        }

                        if(returnType.equals(Process.class))
                            setResult(res.getEchoProcess());

                        return res.getNewValue();
                    }else {
                        Log.e(TAG, "[getNewValue] is NULL!");
                    }
                }
            }catch (Throwable e) {
                Log.e(TAG, "Failed to intercept e=" + e);
            }
        } return null;
    }*/

    //
    //End of Shell Intercept
    //

    //
    //Start of Memory/CPU Functions
    //

    @SuppressWarnings("unused")
    public int getFileDescriptorId(FileDescriptor fs) { return FileUtil.getDescriptorNumber(fs);  }

    @SuppressWarnings("unused")
    public File createFakeMeminfoFile(int totalGigabytes, int availableGigabytes) { return FileUtil.generateTempFakeFile(MemoryUtil.generateFakeMeminfoContents(totalGigabytes, availableGigabytes)); }

    @SuppressWarnings("unused")
    public FileDescriptor createFakeMeminfoFileDescriptor(int totalGigabytes, int availableGigabytes) { return FileUtil.generateFakeFileDescriptor(MemoryUtil.generateFakeMeminfoContents(totalGigabytes, availableGigabytes)); }

    @SuppressWarnings("unused")
    public void populateMemoryInfo(ActivityManager.MemoryInfo memoryInfo, int totalMemoryInGB, int availableMemoryInGB) { MemoryUtilEx.populateMemoryInfo(memoryInfo, totalMemoryInGB, availableMemoryInGB); }

    @SuppressWarnings("unused")
    public ActivityManager.MemoryInfo getFakeMemoryInfo(int totalMemoryInGB, int availableMemoryInGB) { return MemoryUtilEx.getMemory(totalMemoryInGB, availableMemoryInGB); }

    @SuppressWarnings("unused")
    public FileDescriptor createFakeCpuinfoFileDescriptor() { return MockFileUtil.generateFakeFileDescriptor(XMockCall.getSelectedMockCpu(getApplicationContext())); }

    @SuppressWarnings("unused")
    public File createFakeCpuinfoFile() { return MockFileUtil.generateFakeFile(XMockCall.getSelectedMockCpu(getApplicationContext())); }


    @SuppressWarnings("unused")
    public FileDescriptor createFakeUUIDFileDescriptor() { return MockFileUtil.generateFakeBootUUIDDescriptor(getSetting("unique.boot.id")); }

    @SuppressWarnings("unused")
    public File createFakeUUIDFile() { return MockFileUtil.generateFakeBootUUIDFile(getSetting("unique.boot.id")); }


    //
    //End of Memory/CPU Functions
    //

    //
    //Start of Bluetooth Functions
    //

    @SuppressWarnings("unused")
    public Set<BluetoothDevice> filterSavedBluetoothDevices(Set<BluetoothDevice> devices, List<String> allowList) { return ListFilterUtil.filterSavedBluetoothDevices(devices, allowList); }

    @SuppressWarnings("unused")
    public List<ScanResult> filterWifiScanResults(List<ScanResult> results, List<String> allowList) { return ListFilterUtil.filterWifiScanResults(results, allowList); }

    @SuppressWarnings("unused")
    public List<WifiConfiguration> filterSavedWifiNetworks(List<WifiConfiguration> results, List<String> allowList) { return ListFilterUtil.filterSavedWifiNetworks(results, allowList); }

    //
    //End of Bluetooth Functions
    //

    //
    //Start of Display Functions
    //

    @SuppressWarnings("unused")
    public InputDevice.MotionRange createXAxis(int height) { return MotionRangeUtil.createXAxis(height); }

    @SuppressWarnings("unused")
    public InputDevice.MotionRange createYAxis(int width) { return MotionRangeUtil.createYAxis(width); }

    //
    //End of Display Functions
    //

    //
    //Start of ETC Util Functions
    //

    @SuppressWarnings("unused")
    public int generateRandomInt(int origin, int bound) { return ThreadLocalRandom.current().nextInt(origin, bound); }

    @SuppressWarnings("unused")
    public String[] generateMediaCodecSupportedTypeList() { return RandomMediaCodecInfo.generateSupportedTypes(); }

    @SuppressWarnings("unused")
    public String generateMediaCodecName() { return RandomMediaCodec.generateName(); }

    @SuppressWarnings("unused")
    public int[] generateIntArray() {
        int sz = ThreadLocalRandom.current().nextInt(2, 8);
        int[] elements = new int[sz];
        for(int i = 0; i < sz; i++) {
            elements[i] = ThreadLocalRandom.current().nextInt(5000, 9999999);
        }

        return elements;
    }

    @SuppressWarnings("unused")
    public String generateRandomString(int min, int max) { return generateRandomString(ThreadLocalRandom.current().nextInt(min, max + 1)); }

    @SuppressWarnings("unused")
    public String generateRandomString(int length) { return RandomStringGenerator.generateRandomAlphanumericString(length); }

    @SuppressWarnings("unused")
    public byte[] getIpAddressBytes(String ipAddress) { return NetworkUtil.stringIpAddressToBytes(ipAddress); }

    @SuppressWarnings("unused")
    public byte[] getFakeIpAddressBytes() { return NetworkUtil.stringIpAddressToBytes(getSetting("net.host_address")); }

    @SuppressWarnings("unused")
    public int getFakeIpAddressInt() { return NetworkUtil.stringIpAddressToInt(getSetting("net.host_address")); }

    @SuppressWarnings("unused")
    public byte[] getFakeMacAddressBytes() { return NetworkUtil.macStringToBytes(getSetting("net.mac")); }

    @SuppressWarnings("unused")
    public String gigabytesToBytesString(int gigabytes) { return Long.toString((long) gigabytes * 1073741824L); }

    //
    //End of ETC Util Functions
    //

    //
    //Start of Query / Call Functions
    //

    @SuppressWarnings("unused")
    public File[] fileArrayHasEvidence(File[] files, int code) { return Evidence.fileArray(files, code); }
    @SuppressWarnings("unused")
    public List<File> fileListHasEvidence(List<File> files, int code) { return Evidence.fileList(files, code); }
    @SuppressWarnings("unused")
    public String[] stringArrayHasEvidence(String[] file, int code) { return Evidence.stringArray(file, code); }
    @SuppressWarnings("unused")
    public List<String> stringListHasEvidence(List<String> files, int code) { return Evidence.stringList(files, code); }
    @SuppressWarnings("unused")
    public boolean packageNameHasEvidence(String packageName, int code) { return Evidence.packageName(packageName, code); }
    @SuppressWarnings("unused")
    public boolean fileIsEvidence(String file, int code) { return Evidence.file(new File(file), code); }
    @SuppressWarnings("unused")
    public boolean fileIsEvidence(File file,  int code) { return Evidence.file(file.getAbsolutePath(), file.getName(), code); }
    @SuppressWarnings("unused")
    public boolean fileIsEvidence(String fileFull, String fileName, int code) { return Evidence.file(fileFull, fileName, code); }
    @SuppressWarnings("unused")
    public StackTraceElement[] stackHasEvidence(StackTraceElement[] elements) { return Evidence.stack(elements); }

    @SuppressWarnings("unused")
    public String createFilledString(String s, String fillChar) { return Str.createFilledCopy(s, fillChar); }

    @SuppressWarnings("unused")
    public String[] extractSelectionArgs() {
        String[] sel = null;
        try {
            if(paramTypes[2].getName().equals(Bundle.class.getName())){
                //ContentResolver.query26
                Bundle bundle = (Bundle) getArgument(2);
                if(bundle != null) sel = bundle.getStringArray("android:query-arg-sql-selection-args");
            }
            else if(paramTypes[3].getName().equals(String[].class.getName())) sel = (String[]) getArgument(3);
        }catch (Exception ignore) { }
        return sel;
    }

    @SuppressWarnings("unused")
    public boolean isAuthority(String authority) {
        try {
            Object o = getArgument(0);
            if(o == null) return false;
            Uri uri = (Uri)o;
            String a = uri.getAuthority();
            if(a == null) return false;
            return a.toLowerCase().contains(authority.toLowerCase());
        }catch (Exception e) {
            Log.e(TAG, "Failed to Compare Authority... " + authority + " Error: " + e);
            return false;
        }
    }

    @SuppressWarnings("unused")
    public boolean queryFilterAfter(String serviceName, String columnName, String newValue) { return queryFilterAfter(serviceName, columnName, newValue, (Uri)getArgument(0)); }

    @SuppressWarnings("unused")
    public boolean queryFilterAfter(String serviceName, String columnName, String newValue, Uri uri) {
        if(newValue != null && serviceName != null && columnName != null) {
            try {
                String authority = uri.getAuthority();
                Cursor ret = (Cursor) getResult();
                if(ret != null && authority != null) {
                    //content://com.vivo.vms.IdProvider/IdentifierId/OAID
                    String sLow = serviceName.toLowerCase();
                    String cLow = columnName.toLowerCase().trim();
                    String aLow = authority.toLowerCase();
                    if (serviceName.equals("*") || aLow.contains(sLow)) {
                        if (cLow.equals("*")) {
                            //Replace alllll
                            //to do
                        } else {
                            String[] cs = cLow.contains("|") ? cLow.split("\\|") : new String[]{cLow};
                            String[] args = extractSelectionArgs();
                            //Final Check
                            boolean isTarget = false;
                            for (String c : cs) {
                                if (args != null) {
                                    for (String a : args) {
                                        if (a.equalsIgnoreCase(c)) {
                                            isTarget = true;
                                            break;
                                        }
                                    }
                                }

                                if (isTarget) break;
                                if (aLow.contains(c)) {
                                    isTarget = true;
                                    break;
                                }
                            }

                            if (isTarget) {
                                if(BuildConfig.DEBUG)
                                    Log.i(TAG, "Found Query Service [" + authority + "] and Column [" + columnName + "] new Value [" + newValue + "]");

                                setResult(CursorUtil.replaceValue(ret, newValue, cs));
                                return true;
                            }
                        }
                    }
                }
            }catch (Throwable e) {
                Log.e(TAG, "LUA PARAM [queryFilterAfter] Error: " + e.getMessage());
            }
        }
        return false;
    }

    //
    //End of Query / Call Functions
    //

    @SuppressWarnings("unused")
    public Context getApplicationContext() { return this.context; }

    @SuppressWarnings("unused")
    public String getPackageName() { return this.context.getPackageName(); }

    @SuppressWarnings("unused")
    public int getUid() { return this.context.getApplicationInfo().uid; }

    @SuppressWarnings("unused")
    public Object getScope() { return this.param; }

    @SuppressWarnings("unused")
    public int getSDKCode() { return Build.VERSION.SDK_INT; }

    @SuppressWarnings("unused")
    public void printFileContents(String filePath) { FileUtil.printContents(filePath); }

    @SuppressWarnings("unused")
    public StackTraceElement[] getStackTrace() { return Thread.currentThread().getStackTrace(); }

    @SuppressWarnings("unused")
    public String getStackTraceString() { return Log.getStackTraceString(new Throwable()); }

    @SuppressWarnings("unused")
    public void printStack() { Log.w(TAG, Log.getStackTraceString(new Throwable())); }

    //
    //Start of REFLECT Functions
    //

    @SuppressWarnings("unused")
    public Class<Byte> getByteType() { return Byte.TYPE; }

    @SuppressWarnings("unused")
    public Class<Integer> getIntType() { return Integer.TYPE; }

    @SuppressWarnings("unused")
    public Class<Character> getCharType() { return Character.TYPE; }

    @SuppressWarnings("unused")
    public byte[] createByteArray(int size) { return new byte[size]; }

    @SuppressWarnings("unused")
    public int[] createIntArray(int size) { return new int[size]; }

    @SuppressWarnings("unused")
    public Character[] createCharArray(int size) { return new Character[size]; }

    @SuppressWarnings("unused")
    public static boolean isNumericString(String s) { return StringUtil.isNumeric(s); }

    @SuppressWarnings("unused")
    public static int getContainerSize(Object o) { return CollectionUtil.getSize(o); }

    @SuppressWarnings("unused")
    public String joinArray(String[] array) { return array == null ? "" : StringUtil.joinDelimiter(" ", array); }

    @SuppressWarnings("unused")
    public String joinArray(String[] array, String delimiter) {  return array == null ? "" : StringUtil.joinDelimiter(delimiter, array); }

    @SuppressWarnings("unused")
    public String joinList(List<String> list) { return  list == null ? "" : StringUtil.joinDelimiter(" ", list); }

    @SuppressWarnings("unused")
    public String joinList(List<String> list, String delimiter) { return  list == null ? "" : StringUtil.joinDelimiter(delimiter, list);  }

    @SuppressWarnings("unused")
    public List<String> stringToList(String s, String del) { return StringUtil.stringToList(s, del); }

    @SuppressWarnings("unused")
    public boolean listHasString(List<String> lst, String s) { return StringUtil.listHasString(lst, s);  }

    @SuppressWarnings("unused")
    public int stringLength(String s) { return s == null ? -1 : s.length(); }

    @SuppressWarnings("unused")
    public byte[] stringToUTF8Bytes(String s) { return StringUtil.getUTF8Bytes(s); }

    @SuppressWarnings("unused")
    public String bytesToUTF8String(byte[] bs) { return StringUtil.getUTF8String(bs); }

    @SuppressWarnings("unused")
    public byte[] stringToRawBytes(String s) { return StringUtil.stringToRawBytes(s); }

    @SuppressWarnings("unused")
    public String rawBytesToHexString(byte[] bs) { return StringUtil.rawBytesToHex(bs); }

    @SuppressWarnings("unused")
    public String bytesToSHA256Hash(byte[] bs) { return StringUtil.getBytesSHA256Hash(bs); }

    //
    //End of REFLECT Functions
    //

    @SuppressWarnings("unused")
    public boolean javaMethodExists(String className, String methodName) { return ReflectUtilEx.javaMethodExists(className, methodName); }

    @SuppressWarnings("unused")
    public Class<?> getClassType(String className) { return ReflectUtilEx.getClassType(className); }

    @SuppressWarnings("unused")
    public Object createReflectArray(String className, int size) { return ReflectUtilEx.createArray(className, size); }

    @SuppressWarnings("unused")
    public Object createReflectArray(Class<?> classType, int size) { return ReflectUtilEx.createArray(classType, size); }

    @SuppressWarnings("unused")
    public boolean hasFunction(String classPath, String function) { return XReflectUtils.methodExists(classPath, function); }

    @SuppressWarnings("unused")
    public boolean hasFunction(String function) { return XReflectUtils.methodExists(getThis().getClass().getName(), function); }

    @SuppressWarnings("unused")
    public boolean hasField(String classPath, String field) { return XReflectUtils.fieldExists(classPath, field); }

    @SuppressWarnings("unused")
    public boolean hasField(String field) { return XReflectUtils.fieldExists(getThis().getClass().getName(), field); }

    //
    //START OF LONG HELPER FUNCTIONS
    //
    @SuppressWarnings("unused")
    public String getFieldLong(Object instance, String fieldName) { return Long.toString(LuaLongUtil.getFieldValue(instance, fieldName)); }

    @SuppressWarnings("unused")
    public void setFieldLong(Object instance, String fieldName, String longValue) { LuaLongUtil.setLongFieldValue(instance, fieldName, longValue); }

    @SuppressWarnings("unused")
    public void parcelWriteLong(Parcel parcel, String longValue) { LuaLongUtil.parcelWriteLong(parcel, longValue); }

    @SuppressWarnings("unused")
    public String parcelReadLong(Parcel parcel) { return LuaLongUtil.parcelReadLong(parcel); }

    @SuppressWarnings("unused")
    public void bundlePutLong(Bundle bundle, String key, String longValue) { LuaLongUtil.bundlePutLong(bundle, key, longValue); }

    @SuppressWarnings("unused")
    public String bundleGetLong(Bundle bundle, String key) { return LuaLongUtil.bundleGetLong(bundle, key); }

    @SuppressWarnings("unused")
    public void setResultToLong(String long_value) throws Throwable { try { setResult(Long.parseLong(long_value)); }catch (Exception e) { XLog.e("Failed to set result to Long Value. Make sure its a valid Number.", e); } }

    @SuppressWarnings("unused")
    public void setResultToLongInt(String long_int) throws Throwable { try { setResult(Integer.parseInt(long_int)); }catch (Exception e) {  XLog.e("Failed to set result to Long Int Value. Make sure its a valid Number.", e); } }

    @SuppressWarnings("unused")
    public String getResultLong() throws Throwable { try { return Long.toString((long)getResult()); }catch (Exception e) { XLog.e("Failed Get Result as Long String", e); return "0"; } }

    //
    //END OF LONG HELPER FUNCTIONS
    //

    @SuppressWarnings("unused")
    public Object getThis() {
        if (this.field == null) return this.param.thisObject;
        else return null;
    }

    @SuppressWarnings("unused")
    public Object getArgument(int index) {
        if (index < 0 || index >= this.paramTypes.length) throw new ArrayIndexOutOfBoundsException("Argument #" + index);
        return this.param.args[index];
    }

    @SuppressWarnings("unused")
    public void setArgumentString(int index, Object value) { setArgument(index, (String)String.valueOf(value)); }

    @SuppressWarnings("unused")
    public void setArgumentString(int index, String value) { setArgument(index, value); }

    @SuppressWarnings("unused")
    public void setArgument(int index, Object value) {
        if (index < 0 || index >= this.paramTypes.length) throw new ArrayIndexOutOfBoundsException("Argument #" + index);
        if (value != null) {
            value = coerceValue(this.paramTypes[index], value);
            if (!boxType(this.paramTypes[index]).isInstance(value))
                throw new IllegalArgumentException("Expected argument #" + index + " " + this.paramTypes[index] + " got " + value.getClass());
        } this.param.args[index] = value;
    }


    @SuppressWarnings("unused")
    public Throwable getException() {
        Throwable ex = (this.field == null ? this.param.getThrowable() : null);
        if(DebugUtil.isDebug()) Log.i(TAG, "Get " + this.getPackageName() + ":" + this.getUid() + " result=" + ex.getMessage());
        return ex;
    }

    @SuppressWarnings("unused")
    public Object getResult() throws Throwable {
        Object result = (this.field == null ? this.param.getResult() : this.field.get(null));
        if (DebugUtil.isDebug()) Log.i(TAG, "Get " + this.getPackageName() + ":" + this.getUid() + " result=" + result);
        return result;
    }

    @SuppressWarnings("unused")
    public void setResultString(Object result) throws Throwable { setResult(String.valueOf(result)); }

    @SuppressWarnings("unused")
    public void setResultString(String result) throws Throwable { setResult(result); }

    @SuppressWarnings("unused")
    public void setResultByteArray(Byte[] result) throws Throwable { setResult(result); }

    @SuppressWarnings("unused")
    public void setResultBytes(byte[] result) throws Throwable {
        if(result == null) {
            Log.e(TAG, "Set Bytes is NULL ??? fix");
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Setting as Bytes...");
            Log.i(TAG, "Bytes Set " + this.getPackageName() + ":" + this.getUid() +
                    " result size=" + result.length + " return=" + this.returnType);
        }

        try {
            Object res2 = coerceValue(this.returnType, result);
            Log.w(TAG, "Res2 = " + res2 + " class=" + res2.getClass().getName());
        }catch (Exception e) { Log.e(TAG, "Failed to read ccv e=" + e); }

        try {
            boolean boxTyp = !boxType(this.returnType).isInstance(result);
            Log.w(TAG, "Is bytes shit box type ? " + boxTyp);
        }catch (Exception e) { Log.e(TAG, "Failed to read is box type e=" + e); }

        if (this.field == null) this.param.setResult(result);
        else this.field.set(null, result);
    }

    @SuppressWarnings("unused")
    public void setResult(Object result) throws Throwable {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "Set " + this.getPackageName() + ":" + this.getUid() +
                    " result=" + result + " return=" + this.returnType);

        if (result != null && !(result instanceof Throwable) && this.returnType != null) {
            result = coerceValue(this.returnType, result);
            if (!boxType(this.returnType).isInstance(result))
                throw new IllegalArgumentException(
                        "Expected return " + this.returnType + " got " + result.getClass());
        }

        if (this.field == null)
            if (result instanceof Throwable) this.param.setThrowable((Throwable) result);
            else this.param.setResult(result);
        else this.field.set(null, result);
    }

    @SuppressWarnings("unused")
    public Integer getSettingInt(String name, int defaultValue) {
        String setting = getSetting(name);
        if(setting == null) return useDefault ? defaultValue : null;
        try {
            return Integer.parseInt(setting);
        }catch (Exception e) {
            return useDefault ? defaultValue : null;
        }
    }

    @SuppressWarnings("unused")
    public String getSettingReMap(String name, String oldName) { return getSettingReMap(name, oldName, null); }

    @SuppressWarnings("unused")
    public String getSettingReMap(String name, String oldName, String defaultValue) {
        if(name == null && oldName == null) return useDefault ? defaultValue : null;
        String setting = getSetting(name);
        if (setting != null) return setting;
        if(oldName != null) {
            setting = getSetting(oldName);
            if (setting != null) return setting;
            return useDefault ? defaultValue : null;
        }
        return useDefault ? defaultValue : null;
    }

    @SuppressWarnings("unused")
    public String getSetting(String name, String defaultValue) {
        String setting = getSetting(name);
        return setting == null && useDefault ? defaultValue : setting;
    }

    @SuppressWarnings("unused")
    public boolean getSettingBool(String name, boolean defaultValue) {
        String setting = getSetting(name);
        if(setting == null) return defaultValue;
        return Str.toBoolean(setting, defaultValue);
    }

    @SuppressWarnings("unused")
    public String getSetting(String name) {
        synchronized (this.settings) { return (this.settings.containsKey(name) ? this.settings.get(name) : null); }
    }

    @SuppressWarnings("unused")
    public void putValue(String name, Object value, Object scope) {
        Log.i(TAG, "Put value " + this.getPackageName() + ":" + this.getUid() + " " + name + "=" + value + " @" + scope);
        synchronized (nv) {
            if (!nv.containsKey(scope))
                nv.put(scope, new HashMap<String, Object>());
            nv.get(scope).put(name, value);
        }
    }

    @SuppressWarnings("unused")
    public Object getValue(String name, Object scope) {
        Object value = getValueInternal(name, scope);
        //Log.i(TAG, "Get value " + this.getPackageName() + ":" + this.getUid() + " " + name + "=" + value + " @" + scope);
        return value;
    }

    private static Object getValueInternal(String name, Object scope) {
        synchronized (nv) {
            if (!nv.containsKey(scope))
                return null;
            if (!nv.get(scope).containsKey(name))
                return null;
            return nv.get(scope).get(name);
        }
    }

    private static Class<?> boxType(Class<?> type) {
        if (type == boolean.class)
            return Boolean.class;
        else if (type == byte.class)
            return Byte.class;
        else if (type == char.class)
            return Character.class;
        else if (type == short.class)
            return Short.class;
        else if (type == int.class)
            return Integer.class;
        else if (type == long.class)
            return Long.class;
        else if (type == float.class)
            return Float.class;
        else if (type == double.class)
            return Double.class;
        return type;
    }

    private static Object coerceValue(Class<?> returnType, Object value) {
        // TODO: check for null primitives
        Class<?> valueType = value.getClass();
        if(valueType == Double.class || valueType == Float.class || valueType == Long.class || valueType == Integer.class || valueType == String.class) {
            Class<?> boxReturnType = boxType(returnType);
            if(boxReturnType == Double.class || boxReturnType == Float.class || boxReturnType == Long.class || boxReturnType == Integer.class || boxReturnType == String.class) {
                switch (boxReturnType.getName()) {
                    case "java.lang.Integer":
                        return
                                valueType == Double.class ? ((Double) value).intValue() :
                                valueType == Float.class ? ((Float) value).intValue() :
                                valueType == Long.class ? ((Long) value).intValue() :
                                valueType == String.class ? Str.tryParseInt(String.valueOf(value)) : value;
                    case "java.lang.Double":
                        return
                                valueType == Integer.class ? Double.valueOf((Integer) value) :
                                valueType == Float.class ? Double.valueOf((Float) value) :
                                valueType == Long.class ? Double.valueOf((Long) value) :
                                valueType == String.class ? Str.tryParseDouble(String.valueOf(value)) : value;
                    case "java.lang.Float":
                         return
                                valueType == Integer.class ? Float.valueOf((Integer) value) :
                                valueType == Double.class ? ((Double) value).floatValue() :
                                valueType == Long.class ? ((Long) value).floatValue() :
                                valueType == String.class ? Str.tryParseFloat(String.valueOf(value)) : value;
                    case "java.lang.Long":
                        return
                                valueType == Integer.class ? Long.valueOf((Integer) value) :
                                valueType == Double.class ? ((Double) value).longValue() :
                                valueType == Float.class ? ((Float) value).longValue() :
                                valueType == String.class ? Str.tryParseLong(String.valueOf(value)) : value;
                    case "java.lang.String":
                        return
                                valueType == Integer.class ? Integer.toString((int) value) :
                                valueType == Double.class ? Double.toString((double) value) :
                                valueType == Float.class ? Float.toString((float) value) :
                                valueType == Long.class ? Long.toString((long) value) : value;
                }
            }
        }


        // Lua 5.2 auto converts numbers into floating or integer values
    if (Integer.class.equals(value.getClass())) {
            if (long.class.equals(returnType)) return (long) (int) value;
            else if (float.class.equals(returnType)) return (float) (int) value;
        else if (double.class.equals(returnType))
            return (double) (int) value;
    } else if (Double.class.equals(value.getClass())) {
        if (float.class.equals(returnType))
            return (float) (double) value;
    } else if (value instanceof String && int.class.equals(returnType)) {
            return Integer.parseInt((String) value);
        }
        else if (value instanceof String && long.class.equals(returnType)) {
            return Long.parseLong((String) value);
        }
        else if (value instanceof String && float.class.equals(returnType))
            return Float.parseFloat((String) value);
        else if (value instanceof String && double.class.equals(returnType))
            return Double.parseDouble((String) value);

        return value;
    }
}
