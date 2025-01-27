package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

public class SettingsProgressDialog extends AppCompatDialogFragment {

    private static final String TAG = "XLua.SettingsProgressDialog";

    private Context context;
    private Context context2;

    public Context getContext() { return context == null ? context2 : context; }

    private TextView tvProgressData;
    private ProgressBar pbProgress;

    private Map<SettingHolder, SettingPacket> data;

    public SettingsProgressDialog setData(Map<SettingHolder, SettingPacket> data, Context context) {
        this.data = data;
        this.context2 = context;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.hookdeplyload, null);
        pbProgress = view.findViewById(R.id.pbDeployHooks);
        tvProgressData = view.findViewById(R.id.tvDeployHookName);
        builder.setView(view).setTitle(getString(R.string.title_deploy_settings));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        new Thread(this::updateTextView).start();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) { }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) { }


    private void updateTextView() {
        List<String> succeeded = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        if(DebugUtil.isDebug())
            Log.d(TAG, "Sending Settings Size=" + data.size());

        for(Map.Entry<SettingHolder, SettingPacket> entry : data.entrySet()) {
            SettingPacket packet = entry.getValue();

            new Handler(Looper.getMainLooper()).post(() -> tvProgressData.setText(packet.name));

            A_CODE code = PutSettingExCommand.call(context, packet);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Send Setting Packet, Name=" + packet.name + " Result=" + code + " Value=" + packet.value + " UID=" + packet.getUid() + " Category=" + packet.getCategory());

            SettingHolder holder = entry.getKey();
            if (code == A_CODE.SUCCESS) {
                holder.setValue(holder.getNewValue(), true);
                holder.setNameLabelColor(getContext());
                succeeded.add(holder.getName());
            } else {
                failed.add(holder.getName());
            }
        }

        final StrBuilder sb = StrBuilder.create();
        sb.appendLine(context.getText(R.string.msg_succeeded_count));
        sb.newLine();
        for(String s : succeeded)
            sb.appendLine(s);

        if(!failed.isEmpty()) {
            sb.appendLine(context.getText(R.string.msg_failed_count));
            sb.newLine();
            for(String f : failed)
                sb.appendLine(f);
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            tvProgressData.setText(sb.toString());
            pbProgress.setVisibility(View.INVISIBLE);
            getDialog().setCancelable(true);
            getDialog().setCanceledOnTouchOutside(true);
        });

        final CountDownLatch latch = new CountDownLatch(1);
        // Signal that the callback is done
        new Handler(Looper.getMainLooper()).post(latch::countDown);

        try { latch.await(); } catch (InterruptedException ignored) {  }



        /*final String pkgName = app.getPackageName();
        Log.i(TAG, pkgName + " " + groupName + "=" + assign);
        final ArrayList<String> hookIds = new ArrayList<>();
        for (final XLuaHook hook : hooks) {
            if (hook.isAvailable(pkgName, collection) && (groupName == null || groupName.equals(hook.getGroup()))) {
                hookIds.add(hook.getId());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        tvHookStuff.setText(hook.getId());
                    }
                });

                if (assign) app.addAssignment(new LuaAssignment(hook));
                else app.removeAssignment(new LuaAssignment(hook));

                //try { Thread.sleep(50); } catch (InterruptedException ignored) { }
            }
        }

        final XResult res = XLuaCall.assignHooks(context, app.getUid(), pkgName, hookIds, !assign, app.getForceStop());
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (res.succeeded()) {
                    tvHookStuff.setText(R.string.msg_deploy_succeeded);
                } else {

                    try { Thread.sleep(2500); } catch (InterruptedException ignored) { }
                    getDialog().setCancelable(true); // Allow the user to close the dialog manually
                    getDialog().setCanceledOnTouchOutside(true); // Allow the user to close the dialog by touching outside

                }
            }
        });

        // Use CountDownLatch to wait for callback to finish
        final CountDownLatch latch = new CountDownLatch(1);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    HookDeployResult result = new HookDeployResult();
                    result.context = context;
                    result.result = res;
                    result.assign = assign;
                    result.groupName = groupName;
                    result.position = position;
                    callback.onFinish(result);
                }
                latch.countDown(); // Signal that the callback is done
            }
        });

        try { latch.await(); } catch (InterruptedException ignored) {  }
        // Only dismiss the dialog if the operation succeeded
        if (!operationFailed) dismissAllowingStateLoss();*/

    }
}
