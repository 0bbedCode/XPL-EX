package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.util.Pair;

import com.topjohnwu.superuser.Shell;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.file.ChmodModeBuilder;
import eu.faircode.xlua.x.file.FileApi;
import eu.faircode.xlua.x.file.FileEx;
import eu.faircode.xlua.x.file.FileUtils;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.configs.AppProfile;
import eu.faircode.xlua.x.xlua.configs.PathDetails;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.root.RootManager;
import eu.faircode.xlua.x.xlua.root.RootUtils;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

public class PutAppProfileCommand extends CallCommandHandlerEx {
    private static final String TAG = "XLua.PutAppProfileCommand";
    public static final String COMMAND_NAME = "putAppProfile";

    public PutAppProfileCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        requiresSingleThread = true;
    }

    /*
        They click Create then they set extra flag or something Apply or not

     */

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        AppProfile profile = commandData.readExtraAs(AppProfile.class);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Received a Put App Profile Command: Packet=" + Str.toStringOrNull(profile));


        long oldIdentity = Binder.clearCallingIdentity();
        //No matter whatever is sent it should be valid
        //It can exist if so just push over old thats all
        //If not exist then push in general
        //Extra flag to "apply" it

        //BUT WE NEED to handle backing up the profile
        //If Profile does not exist then insert else
        //Simply put if the user pushes this packet as the "updated" one then it will delete old
        //xplex-folder/profile_backups/0/packageName/profileNameHash

        FileEx dbDir = commandData.getDatabase().getDirectoryFile();
        //this.file.setPermissions(FileApi.MODE_SOME_RW__770);
        //targetFolder.setPermissions();
        dbDir.setPermissions(FileApi.MODE_SOME_RW__770, true);
        if(DebugUtil.isDebug())
            Log.d(TAG, "DB Directory Exists: " + dbDir.getAbsolutePath() + " Is Directory:" + dbDir.isDirectory());

        String userId = String.valueOf(commandData.getUserId());
        String pkgName = commandData.getCategory();
        String prHash = "P" + String.valueOf(profile.name.hashCode()).replaceAll("-", "M");
        String profilePath = FileApi.buildPath(commandData.getDatabase().getDirectory(), "profile_backups", userId, pkgName, prHash);

        FileEx profileFile = FileEx.createFromDirectory(profilePath);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Backup up [%s] Files [%s] to Directory: %s :: %s  Is Directory:%s Profile=%s", pkgName, profile.fileBackups.size(), profilePath, profileFile.getAbsolutePath(), profileFile.isDirectory(), Str.toStringOrNull(profile)));

        if(profileFile.isDirectory()) {
            boolean deleted = profileFile.delete();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Deleted old Profile Save: " + profileFile.getAbsolutePath() + " Result:" + deleted + " Name=" + profile.name);

            if(!deleted)
                return A_CODE.FAILED.toBundle();
        }

        for(PathDetails path : profile.fileBackups) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Backing up path: " + path + " Profile Name=" + profile.name + " Pkg=" + pkgName);

            String targetPath = FileApi.buildPath(profilePath, path.tag);
            FileEx targetFolder = FileEx.createFromDirectory(targetPath);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Starting back up on Path: " + path + " to: " + targetFolder.getAbsolutePath() + " :: " + targetPath + " Profile Name=" + profile.name + " Pkg=" + pkgName + " Target Folder Exists: " + targetFolder.exists());

            //Best to backup permissions, maybe create files in backup folder like "DataData__perm_777_666" parse the "777" AND "666"

            FileEx fromFolder = FileEx.createFromDirectory(path.fullPath);
            Pair<Integer, Integer> fromCount = fromFolder.fileAndDirectoryCount(true);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Created From Folder: " + fromFolder.getAbsolutePath() + " Is Directory: " + fromFolder.isDirectory() + " To: " + targetFolder.getAbsolutePath() + " From File Count=" + fromCount.first + " From Dir Count=" + fromCount.second);

            //this.file.setPermissions(FileApi.MODE_SOME_RW__770);
            //targetFolder.setPermissions();
            //targetFolder.setPermissions(FileApi.MODE_SOME_RW__770, true);

            boolean makeDirs = targetFolder.mkdirsEx();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Created Target Folder: " + targetFolder.getAbsolutePath() + " With result: " + makeDirs);

            if(!makeDirs)
                return A_CODE.FAILED.toBundle();

            //targetFolder.setPermissions(FileApi.MODE_SOME_RW__770, true);
            dbDir.takeOwnership();
            dbDir.setPermissions(FileApi.MODE_SOME_RW__770);

            if(fromFolder.isDirectory()) {
                /*ChmodModeBuilder mode = FileApi.getPermissionsOfFileOrDirectory(fromFolder.getCanonicalPath());
                if(DebugUtil.isDebug())
                    Log.d(TAG, " File: " + fromFolder.getAbsolutePath() + " Mode:" + Str.toStringOrNull(mode));


                fromFolder.takeOwnership();
                fromFolder.setPermissions(FileApi.MODE_SOME_RW__770, true);

                fromFolder.copyToDirectory(targetFolder, true, false, 0);

                fromFolder.takeOwnership(mode.getUid(), mode.getGuid(), true);
                fromFolder.setPermissions(mode.getMode(), true);*/

                Pair<Integer, Integer> toCount = targetFolder.fileAndDirectoryCount(true);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Finished Backing up Files! to: " + targetFolder.getAbsolutePath() + " Is Dir=" + targetFolder.isDirectory() + " To File Count=" + toCount.first + " To Dir Count=" + toCount.second);
            }
        }

        FileEx targetJsonFile = FileEx.createFromDirectory(FileApi.buildPath(profilePath, "profile.json"));
        if(!targetJsonFile.isFile()) {
            boolean createdFile = targetJsonFile.createNewFile();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Created the Profile JSON File, Success=" + createdFile + " Path:" + targetJsonFile.getAbsolutePath());

            if(!createdFile)
                return A_CODE.FAILED.toBundle();
        }

        String jsonOutput = JSONUtil.objectToString(JSONUtil.toObject(profile));
        FileUtils.writeStringToFile(targetJsonFile, jsonOutput, false);
        if(DebugUtil.isDebug())
            Log.d(TAG, "JSON File: " + targetJsonFile.getAbsolutePath() + " Is File: " + targetJsonFile.isFile() + " JSON Data=" + jsonOutput);

        Pair<Integer, Integer> baseCount = profileFile.fileAndDirectoryCount(true);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Created the App Data Backups, Dir=" + profileFile.getAbsolutePath() + " Backup Dir File Count=" + baseCount.first + " Backup Dir Dir Count=" + baseCount.second + " Pkg=" + pkgName + " Name=" + profile.name);


        //This should be all, we are just about prepared now
        //

        //I want to write to JSON file in that backup dir
        //if(DebugUtil.isDebug())
        //    Log.d(TAG, "Now ")
        boolean result = DatabaseHelpEx.insertItem(commandData.getDatabase(), AppProfile.TABLE_NAME, profile);
        A_CODE code = A_CODE.resultToCode_x(result);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Put App Profile Result Code=%s  Packet=%s", code, Str.toStringOrNull(profile)));

        Binder.restoreCallingIdentity(oldIdentity);
        return code.toBundle();
    }


    public static A_CODE callEx(Context context, AppProfile profile) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Calling [putAppProfile] Command, Profile=" + Str.toStringOrNull(profile));



        String d_path = GetDatabasePathCommand.get(context);

        String userId = String.valueOf(0);
        String pkgName = profile.getCategory();
        String prHash = "P" + String.valueOf(profile.name.hashCode()).replaceAll("-", "M");
        String profilePath = FileApi.buildPath(d_path, "profile_backups", userId, pkgName, prHash);

        FileEx profileFile = FileEx.createFromDirectory(profilePath);
        if(DebugUtil.isDebug())
            Log.d(TAG, "DPath=" + d_path + " PrHash=" + prHash + " Path=" + profilePath + " File=" + profileFile.getAbsolutePath());


        for(PathDetails path : profile.fileBackups) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "[c] Backing up path: " + path + " Profile Name=" + profile.name + " Pkg=" + pkgName);

            String targetPath = FileApi.buildPath(profilePath, path.tag);
            FileEx targetFolder = FileEx.createFromDirectory(targetPath);
            if(DebugUtil.isDebug())
                Log.d(TAG, "[c] Starting back up on Path: " + path + " to: " + targetFolder.getAbsolutePath() + " :: " + targetPath + " Profile Name=" + profile.name + " Pkg=" + pkgName + " Target Folder Exists: " + targetFolder.exists());

            //Best to backup permissions, maybe create files in backup folder like "DataData__perm_777_666" parse the "777" AND "666"

            FileEx fromFolder = FileEx.createFromDirectory(path.fullPath);
            //Pair<Integer, Integer> fromCount = fromFolder.fileAndDirectoryCount(true);
            //if(DebugUtil.isDebug())
            //    Log.d(TAG, "[c] Created From Folder: " + fromFolder.getAbsolutePath() + " Is Directory: " + fromFolder.isDirectory() + " To: " + targetFolder.getAbsolutePath() + " From File Count=" + fromCount.first + " From Dir Count=" + fromCount.second);

            //this.file.setPermissions(FileApi.MODE_SOME_RW__770);
            //targetFolder.setPermissions();
            //targetFolder.setPermissions(FileApi.MODE_SOME_RW__770, true);

            //boolean makeDirs = targetFolder.mkdirsEx();
            //if(DebugUtil.isDebug())
            //    Log.d(TAG, "[c] Created Target Folder: " + targetFolder.getAbsolutePath() + " With result: " + makeDirs);

            RootUtils.MANAGER.requestRoot();
            boolean hasRoot = Shell.getShell().isRoot();
            if(DebugUtil.isDebug())
                Log.d(TAG, "Has Root=" + hasRoot);

            RootUtils.MANAGER.copyFiles(fromFolder.getAbsolutePath(), targetFolder.getAbsolutePath());
            if(DebugUtil.isDebug())
                Log.d(TAG, "CP => [" + fromFolder.getAbsolutePath() + "] to [" + targetFolder.getAbsolutePath() + "]");

            //if(!makeDirs)
            //    return A_CODE.FAILED.toBundle();
            //targetFolder.setPermissions(FileApi.MODE_SOME_RW__770, true);
            //dbDir.setPermissions(FileApi.MODE_SOME_RW__770);
            //if(fromFolder.isDirectory()) {
                //RootUtils.MANAGER.copyFiles(fromFolder.getAbsolutePath(), targetFolder.getAbsolutePath());
                //if(DebugUtil.isDebug())
                //    Log.d(TAG, "CP => [" + fromFolder.getAbsolutePath() + "] to [" + targetFolder.getAbsolutePath() + "]");

                /*ChmodModeBuilder mode = FileApi.getPermissionsOfFileOrDirectory(fromFolder.getCanonicalPath());
                if(DebugUtil.isDebug())
                    Log.d(TAG, " File: " + fromFolder.getAbsolutePath() + " Mode:" + Str.toStringOrNull(mode));



                fromFolder.takeOwnership();
                fromFolder.setPermissions(FileApi.MODE_SOME_RW__770, true);

                fromFolder.copyToDirectory(targetFolder, true, false, 0);

                fromFolder.takeOwnership(mode.getUid(), mode.getGuid(), true);
                fromFolder.setPermissions(mode.getMode(), true);*/

                //Pair<Integer, Integer> toCount = targetFolder.fileAndDirectoryCount(true);
                //if(DebugUtil.isDebug())
                //    Log.d(TAG, "Finished Backing up Files! to: " + targetFolder.getAbsolutePath() + " Is Dir=" + targetFolder.isDirectory() + " To File Count=" + toCount.first + " To Dir Count=" + toCount.second);
            //}
        }

        return A_CODE.SUCCESS;
    }

    public static A_CODE call(Context context, AppProfile profile) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Calling [putAppProfile] Command, Profile=" + Str.toStringOrNull(profile));

        Bundle b = profile.toBundle();
        if(DebugUtil.isDebug())
            Log.d(TAG, "Calling [putAppProfile] Command, Profile Bundle=" + Str.toStringOrNull(b));


        return A_CODE.fromBundle(XProxyContent.luaCall(
                context,
                COMMAND_NAME, b));
    }
}
