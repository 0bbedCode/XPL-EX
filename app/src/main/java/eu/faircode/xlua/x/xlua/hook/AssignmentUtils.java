package eu.faircode.xlua.x.xlua.hook;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.Str;

public class AssignmentUtils {
    //PS We can add Client Side Interception ?
    //This can block non cleaned cache of items being sent to client that they should not be using unless they think they are smart
    //Eventually migrate these to JSON, and the MAIN JSONs move them to a sub folder away from the filth
    //We can also maybe, make separate commands instead of handing off shit poor args in one com

    public static final List<String> BAD_ASSIGNMENTS = Arrays.asList(
            "PrivacyEx.Filter/Settings$System.getString",
            "PrivacyEx.Filter/Settings$Secure.getString",
            "PrivacyEx.Filter/Settings$Global.getString",
            "PrivacyEx.Filter/ContentResolver/Settings",
            "PrivacyEx.Filter/BinderProxy.transact(int, Parcel, Parcel, int)",
            "PrivacyEx.BlockGuardOs.open/Filter/Filter",
            "PrivacyEx.File.canRead/Filter",
            "PrivacyEx.File.canWrite/Filter",
            "PrivacyEx.File.canExecute/Filter",
            "PrivacyEx.File.exists/Filter",
            "PrivacyEx.File.isFile/Filter",
            "PrivacyEx.File.isDirectory/Filter",
            "PrivacyEx.File.list/Filter",
            "PrivacyEx.File.list/FileNameFilter/Filter",
            "PrivacyEx.File.listFiles/Filter",
            "PrivacyEx.File.listFiles/FileNameFilter/Filter",
            "PrivacyEx.File.listFiles/FileFilter/Filter",
            "PrivacyEx.File.listRoots/Filter",
            "PrivacyEx.Filter/ContentResolver.query16",
            "PrivacyEx.Filter/ContentResolver.query26",
            "PrivacyEx.Filter/ContentResolver.query1",
            "PrivacyEx.Intercept.Runtime.exec(command)",
            "PrivacyEx.Intercept.Runtime.exec(command, envp)",
            "PrivacyEx.Intercept.Runtime.exec(command, envp, dir)",
            "PrivacyEx.Intercept.Runtime.exec(commands)",
            "PrivacyEx.Intercept.Runtime.exec(commands, envp)",
            "PrivacyEx.Intercept.Runtime.exec(commands, envp, dir)",
            "PrivacyEx.Intercept.ProcessBuilder.start",

            "PrivacyEx.Intercept.System.getProperty(string)",
            "PrivacyEx.Intercept.System.getProperty(string, string)",
            "PrivacyEx.Intercept.SystemProperties.get(string)",
            "PrivacyEx.Intercept.SystemProperties.get(string, string)"
    );

    public static boolean isSpecialSetting(String name) { return !Str.isEmpty(name) && name.toLowerCase().contains("setting:"); }
    public static boolean isFilterHook(String name) { return !Str.isEmpty(name) && BAD_ASSIGNMENTS.contains(name); }
}
