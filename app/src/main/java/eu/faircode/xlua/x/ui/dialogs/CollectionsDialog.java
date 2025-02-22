package eu.faircode.xlua.x.ui.dialogs;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.core.dialog.CheckableDialog;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class CollectionsDialog extends CheckableDialog<CollectionsDialog.XCollection> {
    private static final String TAG = "XLua.CollectionsDialog";

    public static class XCollection implements IIdentifiableObject {
        public String id;
        @Override
        public String getObjectId() { return id; }

        @Override
        public void setId(String id) { this.id = id; }
    }

    public static interface OnDialogCloseListener {
        void onDialogClose();
    }

    private OnDialogCloseListener closeListener;
    public CollectionsDialog setOnDialogCloseListener(OnDialogCloseListener listener) {
        this.closeListener = listener;
        return this;
    }

    @Override
    protected void onFinishedPush(List<XCollection> enabled, List<XCollection> disabled) {
        StringBuilder sb = new StringBuilder();
        for(XCollection c : enabled) {
            if(sb.length() > 0)
                sb.append(",");

            sb.append(c.getObjectId());
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Sending new Collections to Service! Collections=" + sb);

        SettingPacket packet = new SettingPacket(GetSettingExCommand.SETTING_COLLECTION, sb.toString());
        packet.setUserIdentity(UserIdentity.fromUid(Process.myUid(), UserIdentity.GLOBAL_NAMESPACE));
        packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
        A_CODE code = PutSettingExCommand.call(context, packet);
        if(DebugUtil.isDebug())
            Log.d(TAG, "Send Collection Setting Packet, Code=" + code + " Packet=" + packet);

        try {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if(closeListener != null)
                        closeListener.onDialogClose();
                });
            } else {
                if(closeListener != null)
                    closeListener.onDialogClose();
            }
        }catch (Exception e) {
            Log.e(TAG, "Error Invoking end Event for Dialog Collection! Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
        }
    }

    public CollectionsDialog set(Context context) {
        if(context == null)
            return this;

        this.title = context.getString(R.string.menu_collections);
        this.useOriginalState = false;
        this.viewRegistry = new SharedRegistry();
        this.TAG_ITEMS = SharedRegistry.STATE_TAG_COLLECTIONS;

        List<XLuaHook> allHooks =  GetHooksCommand.getHooks(context, true, true);
        List<String> collections = GetSettingExCommand.getCollections(context, Process.myUid());
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Got Hooks Count=%s Collections=%s]", allHooks.size(), Str.joinList(collections)));

        List<String> added = new ArrayList<>();
        for(XLuaHook hook : allHooks) {
            String collection = hook.getCollection();
            if(!TextUtils.isEmpty(collection)) {
                if(!added.contains(collection)) {
                    XCollection obj = new XCollection();
                    obj.id = collection;

                    items.add(obj);
                    added.add(collection);

                    boolean enabled = collections.contains(collection);
                    this.viewRegistry.setChecked(TAG_ITEMS, collection, enabled);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Added Collection to List, Collection=" + collection + " Enabled=" + enabled);
                }
            }
        }

        return this;
    }
 }
