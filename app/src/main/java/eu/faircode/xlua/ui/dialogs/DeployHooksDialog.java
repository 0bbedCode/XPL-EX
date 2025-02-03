package eu.faircode.xlua.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.faircode.xlua.R;
import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.app.XLuaApp;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.assignment.LuaAssignment;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.ui.HookDeployResult;
import eu.faircode.xlua.ui.interfaces.IHookDeploy;

public class DeployHooksDialog extends AppCompatDialogFragment {
    private static final String TAG = "XLua.DeployHooksDialog";



    private Context context;
    private TextView tvHookStuff;

    private final ExecutorService executor = Executors.newFixedThreadPool(50);

    private List<String> collection;
    private String groupName;
    private XLuaApp app;
    private boolean assign;
    private int position = 0;
    private List<XLuaHook> hooks;
    private IHookDeploy callback;
    private boolean operationFailed = false; // Flag to track if the operation failed

    public DeployHooksDialog setCollections(List<String> collections) { this.collection = collections; return this; }
    public DeployHooksDialog setGroupName(String groupName) { this.groupName = groupName; return this; }
    public DeployHooksDialog setApp(XLuaApp app) { this.app = app; return this; }
    public DeployHooksDialog setAssign(boolean assign) { this.assign = assign; return this; }
    public DeployHooksDialog setPosition(int position) { this.position = position; return this; }
    public DeployHooksDialog setHooks(List<XLuaHook> hooks) { this.hooks = hooks; return this; }
    public DeployHooksDialog setCallback(IHookDeploy callback) { this.callback = callback; return this; }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.hookdeplyload, null);
        tvHookStuff = view.findViewById(R.id.tvDeployHookName);
        builder.setView(view).setTitle(getString(R.string.title_deploy_hooks));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog from being canceled by touching outside
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateTextView();
            }
        }).start();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) { }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) { }

    private void updateTextView() {
        final String pkgName = app.getPackageName();
        Log.i(TAG, pkgName + " " + groupName + "=" + assign);
        final ArrayList<String> hookIds = new ArrayList<>();
        for (final XLuaHook hook : hooks) {
            if (hook.isAvailable(pkgName, collection) && (groupName == null || groupName.equals(hook.getGroup()))) {
                hookIds.add(hook.getSharedId());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        tvHookStuff.setText(hook.getSharedId());
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
                    operationFailed = true; // Set the flag to indicate operation failure
                    //String text = context.getString(R.string.msg_deploy_failed);
                    //String msg = new StringBuilder()
                    //        .append(text)
                    //        .append("\n")
                    //        .append(res.getFullMessage()).toString();
                    //tvHookStuff.setText(msg);
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
        if (!operationFailed) dismissAllowingStateLoss();
    }
}
