package eu.faircode.xlua.api.hook.assignment;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.api.xstandard.UserIdentityPacket;
import eu.faircode.xlua.utilities.BundleUtil;

//Seperate communication ? like this ?
//Have packets for each similar to a C2 server

//Have like toReadItems or something read the independant items from it or something
//Example XAssignmentIO
//Have even maybe post call ? then way for return ? ...
//Again these classes can specifically be for communiction within the Proxy/Gum/Content Resolver
//Have even maybe a "needsCheckPermissions" then checks caller if specific com requires
//Possibly if possible have key communication shit ? prevent detection via Content Resolver
//For the "invokeCall" function this can be passed ?

//Hmm when dynamically made ? or something have within here a "Handle" function to now Handle the command on the other side ????

//Have the util functions able to read 'backup' fields like uid vs user

public class LuaAssignmentPacket extends UserIdentityPacket {
    public static LuaAssignmentPacket create(Integer user, String packageName, List<String> hookIds) { return new LuaAssignmentPacket(user, packageName, hookIds, false, false); }
    public static LuaAssignmentPacket create(Integer user, String packageName, List<String> hookIds, Boolean delete) { return new LuaAssignmentPacket(user, packageName, hookIds, delete, false); }
    public static LuaAssignmentPacket create(Integer user, String packageName, List<String> hookIds, Boolean delete, Boolean kill) { return new LuaAssignmentPacket(user, packageName, hookIds, delete, kill); }

    public static final int CODE_PUT_ASSIGNMENTS = 0x0;
    public static final int CODE_DELETE_ASSIGNMENTS = 0x1;

    protected List<String> hookIds;
    protected Boolean delete;

    public LuaAssignmentPacket() { setUseUserIdentity(true); }
    public LuaAssignmentPacket(Bundle b) { this(); fromBundle(b); }
    public LuaAssignmentPacket(Integer user, String packageName, List<String> hookIds, Boolean delete, Boolean kill) {
        this();
        setUser(user);
        setCategory(packageName);
        setHookIds(hookIds);
        setKill(kill);
        setCode(getCodeForDeleteOr(delete));
    }

    public LuaAssignmentPacket setIsDelete(Boolean isDelete) { if(isDelete != null) this.delete = isDelete; return this; }
    public boolean isDelete() { return isNullOrEmptyCode() ? this.delete : isCode(CODE_DELETE_ASSIGNMENTS); }

    public List<String> getHookIds() { return this.hookIds; }
    public LuaAssignmentPacket setHookIds(List<String> hookIds) { if(hookIds != null) this.hookIds = hookIds; return this; }

    @Override
    public Bundle toBundle() {
        Bundle b = super.toBundle();
        if(this.hookIds != null) b.putStringArrayList("hooks", new ArrayList<>(this.hookIds));
        if(this.delete != null) b.putBoolean("delete", this.delete);
        writePacketHeaderBundle(b);
        return b;
    }

    @Override
    public void fromBundle(Bundle b) {
        if(b != null) {
            super.fromBundle(b);
            this.hookIds = b.getStringArrayList("hooks");



            this.delete = BundleUtil.readBoolean(b, "delete");
            readPacketHeaderBundle(b);
        }
    }

    public static int getCodeForDeleteOr(boolean deleteAssignments) { return deleteAssignments ? CODE_DELETE_ASSIGNMENTS : CODE_PUT_ASSIGNMENTS; }
}
