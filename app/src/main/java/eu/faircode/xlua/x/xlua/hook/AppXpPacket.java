package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.interfaces.IParcelType;

/*
    To Keep Legacy System we use "UID", though it can mean UID or UserId
    But also since assignment isnt following the Legacy Style maybe now, for now ignore this
 */
public class AppXpPacket implements IParcelType, IJsonType, Parcelable, IAssignListener, IBundleData {
    private static final String TAG = "XLua.AppXpPacket";

    public String packageName;
    public int uid;
    public int icon;
    public String label;
    public boolean enabled;
    public boolean persistent;
    public boolean system;
    public boolean forceStop = false;
    public final List<AssignmentPacket> assignments = new ArrayList<>();

    private IAssignListener onAssign;

    public static final String FIELD_APP = "app_info";

    public AppXpPacket() { }
    public AppXpPacket(Parcel in) { fromParcel(in); }
    public AppXpPacket(Bundle b) { populateFromBundle(b); }

    public AssignmentPacket assignmentAt(int index) { return ListUtil.getAtIndex(assignments, index); }

    public boolean hasAssignment(AssignmentPacket assignment) { return assignmentIndex(assignment) > -1; }

    public void addAssignment(AssignmentPacket assignment) { if(assignment != null) assignments.add(assignment); }
    public void addAllAssignments(List<AssignmentPacket> assignments) { if(!ListUtil.isValid(assignments)) ListUtil.addAllIfValid(this.assignments, assignments); }
    public void removeAssignment(AssignmentPacket assignment) { ListUtil.removeAt(assignments, assignmentIndex(assignment)); }

    public int assignmentIndex(AssignmentPacket assignment) { return ListUtil.indexOf(assignments, assignment, (a, b) -> a.hook.equals(b.hook)); }

    public Collection<AssignmentPacket> getAssignments(String group) {
        if (group == null)
            return assignments;

        Collection<AssignmentPacket> filtered = new ArrayList<>();
        for (AssignmentPacket assignment : assignments)
            if (group.equals(assignment.hookObj.getGroup()))
                filtered.add(assignment);

        return filtered;
    }

    @Override
    public void fromParcel(Parcel in) {
        if(in != null) {
            this.packageName = in.readString();
            this.uid = in.readInt();
            this.icon = in.readInt();
            this.label = in.readString();
            this.enabled = (in.readByte() != 0);
            this.persistent = (in.readByte() != 0);
            this.system = (in.readByte() != 0);
            this.forceStop = (in.readByte() != 0);
            ListUtil.addAllIfValid(assignments, in.createTypedArrayList(AssignmentPacket.CREATOR));
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(this.packageName);
        parcel.writeInt(this.uid);
        parcel.writeInt(this.icon);
        parcel.writeString(this.label);
        parcel.writeByte(this.enabled ? (byte) 1 : (byte) 0);
        parcel.writeByte(this.persistent ? (byte) 1 : (byte) 0);
        parcel.writeByte(this.system ? (byte) 1 : (byte) 0);
        parcel.writeByte(this.forceStop ? (byte) 1 : (byte) 0);
        parcel.writeTypedList(new ArrayList<>(this.assignments));
    }

    @Override
    public String toJSONString() throws JSONException { return toJSONObject().toString(); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jRoot = new JSONObject();

        jRoot.put("packageName", this.packageName);
        jRoot.put("uid", this.uid);
        jRoot.put("icon", this.icon);
        jRoot.put("label", this.label);
        jRoot.put("enabled", this.enabled);
        jRoot.put("persistent", this.persistent);
        jRoot.put("system", this.system);
        jRoot.put("forcestop", this.forceStop);

        JSONArray jAssignments = new JSONArray();
        for (AssignmentPacket assignment : this.assignments)
            jAssignments.put(assignment.toJSONObject());

        jRoot.put("assignments", jAssignments);

        return jRoot;
    }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException {
        if(obj != null) {
            // return fromJSONObject(new JSONObject(json));
            this.packageName = obj.optString("packageName");
            this.uid = obj.optInt("uid");
            this.icon = obj.optInt("icon");
            this.label = (obj.has("label") ? obj.getString("label") : null);
            this.enabled = obj.optBoolean("enabled");
            this.persistent = obj.optBoolean("persistent");
            this.system = obj.optBoolean("system");
            this.forceStop = obj.optBoolean("forcestop");

            JSONArray jAssignment = obj.optJSONArray("assignments");
            if(jAssignment != null) {
                for (int i = 0; i < jAssignment.length(); i++) {
                    AssignmentPacket assignment = new AssignmentPacket();
                    assignment.fromJSONObject((JSONObject) jAssignment.get(i));
                    this.assignments.add(assignment);
                }
            }
        }
    }

    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            try {
                String dataBlob = b.getString(FIELD_APP);
                if(TextUtils.isEmpty(dataBlob))
                    throw new Exception("Data Blob for JSON App Info from Bundle is NULL or Empty!");

                fromJSONObject(new JSONObject(dataBlob));
            }catch (Exception e) {
                Log.e(TAG, "Failed to Read AppXpPacket from Bundle to JSON! Error=" + e);
            }
        }
    }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            try {
                b.putString(FIELD_APP, toJSONString());
            }catch (Exception e) {
                Log.e(TAG, "Failed to Write AppXpPacket to JSON to Bundle! Error=" + e);
            }
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    public void setListener(IAssignListener onAssign) {
        if(onAssign != null)
            this.onAssign = onAssign;
    }

    @Override
    public void setAssigned(Context context, String group, boolean assign) {
        if(onAssign != null)
            onAssign.setAssigned(context, group, assign);
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof AppXpPacket)) return false;
        AppXpPacket other = (AppXpPacket) obj;
        return this.packageName.equalsIgnoreCase(other.packageName) && this.uid == other.uid;
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("PackageName", this.packageName)
                .appendFieldLine("UserId", this.uid)
                .appendFieldLine("Icon", this.icon)
                .appendFieldLine("Label", this.label)
                .appendFieldLine("Enabled", this.enabled)
                .appendFieldLine("Persistent", this.persistent)
                .appendFieldLine("System", this.forceStop)
                .appendFieldLine("ForceStop", this.forceStop)
                .appendFieldLine("Assignment Count", ListUtil.size(assignments))
                .toString(true);
    }

    public static final Creator<AppXpPacket> CREATOR = new Creator<AppXpPacket>() {
        @Override
        public AppXpPacket createFromParcel(Parcel in) { return new AppXpPacket(in); }

        @Override
        public AppXpPacket[] newArray(int size) { return new AppXpPacket[size]; }
    };
}
