package eu.faircode.xlua.x.hook.filter.kinds;

import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.IFilterContainer;

public class FileFilterContainer extends FilterContainerElement implements IFilterContainer {
    public static IFilterContainer create() { return new FileFilterContainer(); }

    public static final String ALLOW_FILES_SETTING = "storage.allow.files";
    public static final String BLOCK_FILES_SETTING = "storage.block.files";

    /*
        ToDo: Maybe Add ?
                java.libcore.io.Os.java
                java.libcore.io.Linux.java
     */

    public static final String GROUP_NAME = "Intercept.File";
    public static final TypeMap DEFINITIONS = TypeMap.create()
            .add(File.class, "exists",
                    "list",
                    "listFiles",
                    "isFile",
                    "isDirectory",
                    "canExecute",
                    "canWrite",
                    "canRead")
            .add("libcore.io.BlockGuardOs", "open")
            .add("libcore.io.ForwardingOs", "open")
            .add("libcore.io.IoBridge", "open");

    protected HashMap<String, Boolean> fileRules = new HashMap<>();

    public FileFilterContainer() { super(GROUP_NAME, DEFINITIONS); }

    @Override
    public boolean hasSwallowedAsRule(XLuaHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            String method = hook.getMethodName();
            String[] params = hook.getParameterTypes();
            if(TextUtils.isEmpty(method) || "allow".equalsIgnoreCase(method) || "block".equalsIgnoreCase(method) || !ArrayUtils.isValid(params)) {
                factory.removeRule(hook);
                //Still return true , avoid caller from adding it to list of global hooks but don't add it to our list so block it
            } else {
                boolean isAllow = "allow".equalsIgnoreCase(method);
                for(String p : params) {
                    if(TextUtils.isEmpty(p)) continue;
                    String trimmed = Str.trim(p, File.separator, true, true);
                    if(!Str.isEmpty(trimmed))
                        fileRules.put(trimmed, isAllow);

                }
            }
        }
        return isRule;
    }

    @Override
    public void initializeDefinitions(List<XLuaHook> hooks, Map<String, String> settings) {
        super.initializeDefinitions(hooks, settings);
        if(fileRules.isEmpty())
            return;

        StringBuilder allowed = new StringBuilder();
        StringBuilder blocked = new StringBuilder();
        String allowedCached = settings.get(ALLOW_FILES_SETTING);
        String blockedCached = settings.get(BLOCK_FILES_SETTING);

        if(Str.isValidNotWhitespaces(allowedCached)) {
            allowedCached = Str.trim(allowedCached, ",", true, true);
            if(Str.isValidNotWhitespaces(allowedCached))
                allowed.append(allowedCached);
        }

        if(Str.isValidNotWhitespaces(blockedCached)) {
            blockedCached = Str.trim(blockedCached, ",", true, true);
            if(Str.isValidNotWhitespaces(blockedCached))
                blocked.append(blockedCached);
        }

        for(Map.Entry<String, Boolean> entry : fileRules.entrySet()) {
            String file = entry.getKey();
            boolean isAllowed = entry.getValue();
            if(!TextUtils.isEmpty(file)) {
                if(isAllowed) {
                    if(allowed.length() > 0) allowed.append(",");
                    allowed.append(file);
                }
                else {
                    if(blocked.length() > 0) blocked.append(",");
                    blocked.append(file);
                }
            }
        }

        if(allowed.length() > 0)
            settings.put(ALLOW_FILES_SETTING, allowed.toString());
        if(blocked.length() > 0)
            settings.put(BLOCK_FILES_SETTING, blocked.toString());
    }
}