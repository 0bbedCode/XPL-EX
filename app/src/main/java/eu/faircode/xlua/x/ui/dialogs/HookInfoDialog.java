package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;

public class HookInfoDialog extends AppCompatDialogFragment {
    private static final String TAG =  LibUtil.generateTag(HooksDialog.class);

    public static final Map<String, String> MAPPED_CACHE = new HashMap<>();


    public static String getMessage(Context context, String groupName) {
        if(context == null || Str.isEmpty(groupName))
            return context.getString(R.string.error_no_description_hook);

        // Normalize group name for cache lookup
        String normalizedName = groupName.toLowerCase().replaceAll("[^a-z0-9_]", "_");

        // Check cache first
        String cached = MAPPED_CACHE.get(normalizedName);
        if(cached != null) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Got cached description for: " + groupName);
            return cached;
        }

        // Build resource name from group name
        String resourceName = "description_hook_group_" + normalizedName;
        int resId = context.getResources().getIdentifier(resourceName, "string", context.getPackageName());

        if(resId == 0) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "No description found for hook group: " + groupName);
            return context.getString(R.string.error_no_description_hook);
        }

        String message = context.getString(resId);

        // Cache the result
        MAPPED_CACHE.put(normalizedName, message);

        if(DebugUtil.isDebug())
            Log.d(TAG, "Cached new description for: " + groupName);

        return message;
    }


    private Context context;
    private String title;
    private String hookName = "";
    private String message = "";
    private int iconResId = -1;

    public static HookInfoDialog create() {
        return new HookInfoDialog();
    }

    public HookInfoDialog setHookGroupName(String name) {
        this.hookName = name;
        return this;
    }

    public HookInfoDialog setHookGroupMessage(String message) {
        this.message = message;
        return this;
    }

    public HookInfoDialog setIcon(int resourceId) {
        this.iconResId = resourceId;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.hook_info_dialog, null);

        ImageView ivIcon = view.findViewById(R.id.ivDialogIcon);
        TextView tvHookName = view.findViewById(R.id.tvHookName);
        TextView tvHookGroupMessage = view.findViewById(R.id.tvHookGroupMessage);

        if (iconResId != -1) {
            ivIcon.setImageResource(iconResId);
            ivIcon.setVisibility(View.VISIBLE);
        }

        tvHookName.setText(hookName);
        tvHookGroupMessage.setText(message);

        builder.setView(view)
                .setPositiveButton(R.string.option_ok, null);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}