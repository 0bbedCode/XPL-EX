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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.InputDevice;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import de.robv.android.xposed.XC_MethodHook;
import eu.faircode.xlua.api.hook.XLuaHookBase;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.api.properties.MockPropSetting;
import eu.faircode.xlua.api.xmock.XMockCall;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.interceptors.UserContextMaps;
import eu.faircode.xlua.interceptors.ShellIntercept;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.kinds.FileFilterContainer;
import eu.faircode.xlua.x.hook.interceptors.devices.InputDeviceInterceptor;
import eu.faircode.xlua.x.hook.interceptors.file.FileInterceptor;
import eu.faircode.xlua.x.hook.interceptors.file.StatInterceptor;
import eu.faircode.xlua.x.hook.interceptors.ipc.BinderInterceptor;
import eu.faircode.xlua.x.hook.interceptors.ipc.holders.IntentQueryData;
import eu.faircode.xlua.x.hook.interceptors.ipc.holders.SettingsIntentCallData;
import eu.faircode.xlua.x.hook.interceptors.network.DhcpInfoInterceptor;
import eu.faircode.xlua.x.hook.interceptors.network.LinkPropertiesInterceptor;
import eu.faircode.xlua.x.hook.interceptors.network.NetworkInterfaceInterceptor;
import eu.faircode.xlua.x.hook.interceptors.network.WifiInfoInterceptor;
import eu.faircode.xlua.random.randomizers.RandomMediaCodec;
import eu.faircode.xlua.random.randomizers.RandomMediaCodecInfo;
import eu.faircode.xlua.rootbox.XReflectUtils;
import eu.faircode.xlua.utilities.Evidence;
import eu.faircode.xlua.utilities.ListFilterUtil;
import eu.faircode.xlua.utilities.CollectionUtil;
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
import eu.faircode.xlua.x.hook.interceptors.pkg.PackageInfoInterceptor;
import eu.faircode.xlua.x.process.ProcessUtils;
//import eu.faircode.xlua.x.hook.handlers.settings.CallData;

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

    private String oldResult = "";
    private String newResult = "";
    private String settingResult = "";

    public void setOldResult(String oldResult) { this.oldResult = oldResult; }
    @SuppressWarnings("unused")
    public String getOldResult() { return this.oldResult; }

    public void setNewResult(String newResult) { this.newResult = newResult; }
    @SuppressWarnings("unused")
    public String getNewResult() { return this.newResult; }

    public void setSettingResult(String settingResult) { this.settingResult = settingResult; }

    public String getSettingResult() { return "Setting:" + this.settingResult; }

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

    @SuppressWarnings("unused")
    public GroupedMap getGroupedMap(String category) {
        //category, applicationContext
        Context ctx = getApplicationContext();
        Object v = getValue(category, ctx);
        if(!(v instanceof GroupedMap)) {
            GroupedMap map = new GroupedMap();
            putValue(category, map, ctx);
            return map;
        }

        return (GroupedMap) v;
    }

    @SuppressWarnings("unused")
    public boolean isDriverFiles(String pathOrFile) { return FileUtil.isDeviceDriver(pathOrFile); }

    /*
        ToDO: Rename "setSettingResult" & "setOldResult" & "setNewResult"
                Update to ensure working with Index Settings, and Control Prop
     */
    @SuppressWarnings("unused")
    public boolean ensurePropertyIsSafe() {
        try {
            String propName = Str.trimOriginal(tryGetArgument(0, Str.EMPTY));
            if(Str.isEmpty(propName) ||  MockUtils.isPropVxpOrLua(propName))
                return false;

            if(DebugUtil.isDebug())
                Log.d(TAG, "Property Get=" + propName);

            String retValue = tryGetResult(Str.EMPTY);
            String mappedSetting = getSetting(FilterContainerElement.createPropertySetting(propName));
            if(Str.isEmpty(mappedSetting))
                return false;

            String newValue = getSetting(mappedSetting);
            if(newValue == null)
                return false;

            //Handle pair based [1,2]
            //So MAYBE ? in the Rule Filter we parse both ".1" and ".2" ? I don't know ... lets find out
            setSettingResult(propName);
            setOldResult(retValue);
            setNewResult(newValue);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Intercepted Property [%s] with the Mapped Setting [%s] value [%s] replacing [%s] value",
                        propName,
                        mappedSetting,
                        Str.ensureNoDoubleNewLines(newValue),
                        Str.ensureNoDoubleNewLines(retValue)));

            setResult(newValue);
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Error ensuring Build.Prop Property is Safe! Error=" + e);
            return false;
        }
    }


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

    @SuppressWarnings("unused")
    public boolean interceptRemoveExternalDeviceIds() { return InputDeviceInterceptor.removeExternalDevices(this); }

    @SuppressWarnings("unused")
    public boolean interceptDevice(boolean isResult) { return InputDeviceInterceptor.interceptDevice(this, isResult); }

    @SuppressWarnings("unused")
    public boolean interceptOpen() { return FileInterceptor.interceptOpen(this); }

    @SuppressWarnings("unused")
    public boolean interceptFileList() { return FileInterceptor.interceptList(this); }

    @SuppressWarnings("unused")
    public boolean interceptFileBool() { return FileInterceptor.interceptExistsOrIsFileOrDirectory(this); }

    @SuppressWarnings("unused")
    public boolean packageInfoInstallTimeSpoof(boolean isReturn) { return PackageInfoInterceptor.interceptTimeStamps(this, isReturn); }

    @SuppressWarnings("unused")
    public String randomUUIDString() { return UUID.randomUUID().toString(); }

    @SuppressWarnings("unused")
    public UUID randomUUID() { return UUID.randomUUID();  }

    @SuppressWarnings("unused")
    public boolean spoofLastModified() { return StatInterceptor.interceptFileLastModified(this); }

    @SuppressWarnings("unused")
    public boolean cleanStructStat() { return StatInterceptor.interceptOsStat(this); }

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
        String blackSetting = getSetting("apps.blacklist.mode.bool");
        if(blackSetting != null) {
            boolean isBlacklist = StringUtil.toBoolean(blackSetting, false);
            if(isBlacklist) {
                String block = getSetting("apps.block.list");
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

                //Revert this
                if(getSettingBool("apps.blacklist.allow.vital.apps.bool", true)) {
                    for(String p : ALLOWED_PACKAGES)
                        if(str.contains(p))
                            return true;
                }

                return false;
            }
        } return !Evidence.packageName(str, 3);
    }

    @SuppressWarnings("unused")
    public boolean ensureIpcIsSafe(boolean getResult) { return BinderInterceptor.intercept(this, getResult); }


    @SuppressWarnings("unused")
    public void isNullError(Object h) {
        StrBuilder report = StrBuilder.create()
                .ensureOneNewLinePer(true);
        String possibleLuaScript = null;
        report.appendLine("[(0x8835)ERROR NIL LUA SCRIPT CODE] *Interesting*...");
        try {
            if(h instanceof XLuaHookBase) {
                XLuaHookBase hookBase = (XLuaHookBase) h;
                try { possibleLuaScript = hookBase.getLuaScript(); } catch (Exception ignored) { }
                report.appendLine("[[HOOK INFO]]")
                        .appendFieldLine("Class", hookBase.getClassName())
                        .appendFieldLine("Method", hookBase.getMethodName())
                        .appendFieldLine("Group", hookBase.getGroup())
                        .appendFieldLine("ID", hookBase.getObjectId())
                        .appendFieldLine("Collection", hookBase.getCollection())
                        .appendFieldLine("Author", hookBase.getAuthor());
            } else {
                report.appendLine("[Hook Info] ERROR Printing")
                        .appendFieldLine("Is Null", String.valueOf(h == null))
                        .appendFieldLine("ClasName if not Null", h == null ? "null" : h.getClass().getName());
            }
        }catch (Exception e) {
            report.appendLine("[!] ERROR WRITING HOOK INFO!").appendFieldLine("Error", e.getMessage());
        }
        try {
            Object r = tryGetResult(null);
            if(r == null) {
                report.appendLine("[[RESULT]]:").appendLine("[>] Is null...");
            } else {
                report.appendLine("[[RESULT]]:")
                                .appendFieldLine("Type Result", r.getClass().getName())
                                .appendFieldLine("Result[valueOf]", String.valueOf(r))
                                .appendFieldLine("Result[toString]", r.toString());
            }
        }catch (Exception e) {
            report.appendLine("[!] ERROR WRITING RESULT!").appendFieldLine("Error", e.getMessage());
        }
        try {
            report.appendLine("[[XPARAM METHOD DUMP]]");
            List<Method> methods = new ArrayList<>();
            methods.addAll(Arrays.asList(XParam.class.getMethods()));
            methods.addAll(Arrays.asList(XParam.class.getDeclaredMethods()));
            for(Method m : methods) report.appendLine(m.getName());
        }catch (Exception e) {
            report.appendLine("[!] ERROR WRITING XPARAM METHOD DUMP!").appendFieldLine("Error", e.getMessage());
        }
        //Dump "this" methods ???
        //For logging generic errors sure but for this not needed
        report.appendLine("[[STACK TRACE]]");
        report.appendLine(Log.getStackTraceString(new Throwable()));
        report.appendLine("[[LUA SCRIPT]]");
        report.appendLine(possibleLuaScript == null ? "null" : possibleLuaScript);
        Log.e(TAG, report.toString(true));
    }

    @SuppressWarnings("unused")
    public ShellInterception createShellContext(boolean isProcessBuilder) { return new ShellInterception(this, isProcessBuilder, getUserMaps()); }

    @SuppressWarnings("unused")
    public boolean isQueryBad(boolean getResult) { return new IntentQueryData(this, getResult).intercept(this); }

    @SuppressWarnings("unused")
    public boolean isSettingsContentBad(boolean getResult) {
        return new SettingsIntentCallData(this, getResult)
                .replaceSettingStringResult(this); }

    @SuppressWarnings("unused")
    public boolean isCommandBad(ShellInterception shellData) {
        if(shellData == null) {
            Log.w(TAG, "[ShellIntercept] Command Shell Data Object is Null...");
            return false;
        }

        ShellInterception res = ShellIntercept.intercept(shellData);
        if(!res.isMalicious() || res.getNewValue() == null) {
            if(DebugUtil.isDebug()) Log.d(TAG, "[ShellIntercept] Command is not Malicious: " + shellData.getCommandLine());
            return false;
        }

        try {
            setOldResult(res.getCommandLine());
            setNewResult(res.getNewValue());
            setResult(ProcessUtils.createProcess(res.process, res.getNewValue()));
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Error Setting the new Intercepted Process Command Value " + e);
            return false;
        }
    }

    @SuppressWarnings("unused")
    public boolean interceptFileOpenProcIfNet6() { return NetworkInterfaceInterceptor.interceptFileOpenProcIfNet6(this); }

    @SuppressWarnings("unused")
    public boolean interceptIoctlInetAddress() { return NetworkInterfaceInterceptor.interceptIoctlInetAddress(this); }

    @SuppressWarnings("unused")
    public boolean interceptFileListForNetworkInterfaces() { return NetworkInterfaceInterceptor.interceptFileList(this); }

    @SuppressWarnings("unused")
    public boolean interceptGetifaddrs() { return NetworkInterfaceInterceptor.interceptGetifaddrs(this); }

    @SuppressWarnings("unused")
    public boolean interceptDhcpInfo(boolean isResult) { return DhcpInfoInterceptor.intercept(this, isResult); }

    @SuppressWarnings("unused")
    public boolean interceptLinkProperties(boolean isResult) {  return LinkPropertiesInterceptor.intercept(this, isResult); }

    @SuppressWarnings("unused")
    public boolean interceptWifiInfo(boolean isResult) { return WifiInfoInterceptor.intercept(this, isResult); }

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

    @SuppressWarnings("unused")
    public Set<BluetoothDevice> filterSavedBluetoothDevices(Set<BluetoothDevice> devices, List<String> allowList) { return ListFilterUtil.filterSavedBluetoothDevices(devices, allowList); }

    @SuppressWarnings("unused")
    public List<ScanResult> filterWifiScanResults(List<ScanResult> results, List<String> allowList) { return ListFilterUtil.filterWifiScanResults(results, allowList); }

    @SuppressWarnings("unused")
    public List<WifiConfiguration> filterSavedWifiNetworks(List<WifiConfiguration> results, List<String> allowList) { return ListFilterUtil.filterSavedWifiNetworks(results, allowList); }


    @SuppressWarnings("unused")
    public InputDevice.MotionRange createXAxis(int height) { return MotionRangeUtil.createXAxis(height); }

    @SuppressWarnings("unused")
    public InputDevice.MotionRange createYAxis(int width) { return MotionRangeUtil.createYAxis(width); }

    @SuppressWarnings("unused")
    public int generateRandomInt(int origin, int bound) { return RandomGenerator.nextInt(origin, bound); }

    @SuppressWarnings("unused")
    public String[] generateMediaCodecSupportedTypeList() { return RandomMediaCodecInfo.generateSupportedTypes(); }

    @SuppressWarnings("unused")
    public String generateMediaCodecName() { return RandomMediaCodec.generateName(); }

    @SuppressWarnings("unused")
    public int[] generateIntArray() {
        int sz = RandomGenerator.nextInt(2, 8);
        int[] elements = new int[sz];
        for(int i = 0; i < sz; i++) {
            elements[i] = RandomGenerator.nextInt(5000, 9999999);
        }

        return elements;
    }

    @SuppressWarnings("unused")
    public String generateRandomString(int min, int max) { return generateRandomString(RandomGenerator.nextInt(min, max + 1)); }

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
        return this.field == null ? this.param.thisObject : null;
    }

    @SuppressWarnings("unused")
    public<T> T tryGetArgument(int index, T defaultValue) {
        try {
            return (T)getArgument(index);
        }catch (Throwable t) {
            Log.e(TAG, "Error Getting Argument at Index: " + index + " Error: " + t);
            return defaultValue;
        }
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
        if(DebugUtil.isDebug())
            Log.i(TAG, "Get " + this.getPackageName() + ":" + this.getUid() + " result=" + (ex == null ? "null" : ex.getMessage()));
        return ex;
    }

    @SuppressWarnings("unused")
    public<T> T  tryGetResult(T defaultValue) {
        try {
            return (T)getResult();
        }catch (Throwable t) {
            Log.e(TAG, "Error Getting Result! Error: " + t);
            return null;
        }
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

        //
        //WORK ON UNBOXING ARRAY TYPES NOT HIGH PRIORITY but still
        //

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
    public String getSetting(String name) { synchronized (this.settings) { return (this.settings.containsKey(name) ? this.settings.get(name) : null); } }

    @SuppressWarnings("unused")
    public void putValue(String name, Object value, Object scope) {
        if(DebugUtil.isDebug()) Log.i(TAG, "Put value " + this.getPackageName() + ":" + this.getUid() + " " + name + "=" + value + " @" + scope);
        synchronized (nv) {
            if (!nv.containsKey(scope))
                nv.put(scope, new HashMap<String, Object>());
            nv.get(scope).put(name, value);
        }
    }

    @SuppressWarnings("unused")
    public <T> T tryGetValue(String name) {
        Object value = getValueInternal(name, getApplicationContext());
        if(value == null) return null;
        try {
            if(DebugUtil.isDebug()) Log.i(TAG, "Get value " + this.getPackageName() + ":" + this.getUid() + " " + name + "=" + value);
            return (T)value;
        }catch (Exception e) {
            Log.e(TAG, "Failed to get Value, Name=" + name + " Error=" + e);
            return null;
        }
    }

    @SuppressWarnings("unused")
    public Object getValue(String name, Object scope) {
        Object value = getValueInternal(name, scope);
        if(DebugUtil.isDebug())
            Log.i(TAG, "Get value " + this.getPackageName() + ":" + this.getUid() + " " + name + "=" + value + " @" + scope);
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
