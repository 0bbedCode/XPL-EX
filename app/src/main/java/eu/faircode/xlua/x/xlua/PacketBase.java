package eu.faircode.xlua.x.xlua;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;
import eu.faircode.xlua.x.xlua.identity.IUidCompress;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.identity.UserIdentityIO;

public abstract class PacketBase implements IActionObject, IDatabaseEntry, IBundleData, IUidCompress {
    private static final String TAG = "XLua.PacketBase";

    private UserIdentity userIdentity;
    private ActionPacket actionPacket;

    public PacketBase() { }
    public PacketBase(UserIdentity userIdentity) { this.userIdentity = userIdentity; }
    public PacketBase(UserIdentity userIdentity, ActionPacket actionPacket) { this.userIdentity = userIdentity; this.actionPacket = actionPacket; }

    public void setUserIdentity(UserIdentity userIdentity) { this.userIdentity = userIdentity; }
    public void setActionPacket(ActionPacket actionPacket) { this.actionPacket = actionPacket; }

    public void writeUserIdentityToParcel(Parcel p, boolean isCategoryFirst, boolean isUserNotUid) {
        if(p != null) {
            String cat = getCategoryOrGlobal();
            int id = isUserNotUid ? getUserId(false) : getUid();
            if(isCategoryFirst) p.writeString(cat);
            p.writeInt(id);
            if(!isCategoryFirst) p.writeString(cat);
        }
    }

    public UserIdentity getUserIdentityFromParcel(Parcel p, boolean isCategoryFirst, boolean isUserNotUid) {
        UserIdentity uid = new UserIdentity();
        if(p != null) {
            if(isCategoryFirst) uid.setCategory(p.readString());
            int id = p.readInt();
            if(isUserNotUid) uid.setUserId(id);
            else uid.setUid(id);
            if(!isCategoryFirst) uid.setCategory(p.readString());
        }
        return uid;
    }

    @Override
    public void populateContentValues(ContentValues cv) {
        //if(this.userIdentity != null) this.userIdentity = new UserIdentity();
        if(cv != null)
            UserIdentityIO.populateContentValues(this.userIdentity, cv);
    }

    @Override
    public void populateFromContentValues(ContentValues cv) {
        if(cv != null) {
            if(this.userIdentity == null) this.userIdentity = new UserIdentity();
            UserIdentityIO.populateFromContentValues(cv, this.userIdentity);
        }
    }

    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            if(this.userIdentity == null) this.userIdentity = new UserIdentity();
            UserIdentityIO.fromBundleEx(b, this.userIdentity, false);

            ActionPacket actionPacket = new ActionPacket();
            if(b.containsKey(ActionPacket.FILED_ACTION_PACKET)) {
                Bundle inst = b.getBundle(ActionPacket.FILED_ACTION_PACKET);
                actionPacket.fromBundle(inst);
            } else {
                actionPacket.fromBundle(b);
            }

            if(!actionPacket.kill && actionPacket.extra == 0 && actionPacket.flags == ActionFlag.NONE)
                return;

            this.actionPacket = actionPacket;
        }
    }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            UserIdentityIO.toBundleEx(this.userIdentity, b);
            if(this.actionPacket != null) {
                Bundle inst = new Bundle();
                this.actionPacket.toBundle(inst);
                b.putBundle(ActionPacket.FILED_ACTION_PACKET, inst);
            }
        }
    }

    @Override
    public boolean hasAction() { return actionPacket != null; }

    @Override
    public boolean hasIdentification() { return userIdentity != null; }

    @Override
    public int getActionExtra() { return actionPacket != null ? actionPacket.extra : 0; }

    @Override
    public ActionFlag getActionFlags() { return actionPacket != null ? actionPacket.flags : ActionFlag.NONE; }

    @Override
    public boolean shouldKill() { return actionPacket != null && actionPacket.kill; }

    @Override
    public boolean isAction(ActionFlag flag) { return actionPacket != null && actionPacket.flags == flag; }

    @Override
    public int getUid() { return userIdentity != null ? userIdentity.getUid() : UserIdentity.DEFAULT_USER; }

    @Override
    public int getUserId(boolean resolveUid) { return userIdentity != null ? userIdentity.getUserId(resolveUid) : UserIdentity.DEFAULT_USER; }

    @Override
    public String getCategory() { return userIdentity != null ? userIdentity.getCategory() : UserIdentity.GLOBAL_NAMESPACE; }

    public String getCategoryOrGlobal() { return userIdentity != null && !TextUtils.isEmpty(userIdentity.getCategory()) ? userIdentity.getCategory() : UserIdentity.GLOBAL_NAMESPACE; }

    @Override
    public UserIdentity getUserIdentity() { return userIdentity; }

    @Override
    public ActionPacket getActionPacket() { return actionPacket; }

    public static A_CODE ensurePacket(IActionObject obj) {
        if(obj == null) {
            Log.e(TAG, "Packet is Null! ....");
            return A_CODE.GENERIC_INVALID_ARGS;
        }

        if(!obj.hasAction()) {
            Log.e(TAG, "Packet does not have an Action!");
            return A_CODE.GENERIC_INVALID_INPUT;
        }

        return A_CODE.NONE;
    }
}
