package eu.faircode.xlua.x.xlua.database;

import android.os.Bundle;

import androidx.annotation.NonNull;

import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;

public class ActionPacket {
    public static ActionPacket create(ActionFlag flag, boolean kill) { return new ActionPacket(flag, kill); }

    public static final String FILED_ACTION_PACKET = "action_packet";

    public static final String FIELD_KILL = "kill";
    public static final String FIELD_FLAGS = "flags";
    public static final String FIELD_EXTRA = "extra_action_flags";

    public static final String FIELD_CODE = "code";

    public static final String ACTION_DUMP = "dump";
    public static final String ACTION_ALL = "all";

    public UserIdentity identity;   //Resolve here ? as actions should be linked to Identities

    public boolean kill = false;
    public ActionFlag flags = ActionFlag.NONE;
    public int extra = 0;

    public ActionPacket() { }
    public ActionPacket(ActionFlag flags, boolean kill) {
        this.kill = kill;
        this.flags = flags;
    }

    public ActionPacket(ActionFlag flags, boolean kill, int extra) {
        this.kill = kill;
        this.flags = flags;
        this.extra = extra;
    }

    public void fromBundle(Bundle b) {
        if(b != null) {
            this.kill = b.getBoolean(FIELD_KILL, false);
            this.flags = ActionFlag.fromInt(b.getInt(FIELD_FLAGS, 0));
            this.extra = b.getInt(FIELD_EXTRA, 0);
        }
    }

    public void toBundle(Bundle b) {
        if(b != null) {
            b.putBoolean(FIELD_KILL, kill);
            b.putInt(FIELD_FLAGS, flags.getValue());
            b.putInt(FIELD_EXTRA, extra);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendFieldLine("Kill", this.kill)
                .appendFieldLine("Flags", this.flags)
                .appendFieldLine("Extra", this.extra)
                .toString(true);
    }
}
