package eu.faircode.xlua.x.data.box.bases;

import android.os.Bundle;

import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;

public class XUser {
    public static final String FIELD_UID = "uid";
    public static final String FIELD_USER = "user";
    public static final String FIELD_CATEGORY = "category";

    public int uid;
    public int user;
    public String category;

    public int resolveUserId() {
        if(user < 0) {
            //user = UserIdentityUtils.getUserId(useru);
        }

        return 0;
    }
}
