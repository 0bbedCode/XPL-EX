package eu.faircode.xlua.x.ui.core.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;

public abstract class CheckableDialog<T extends IIdentifiableObject> extends AppCompatDialogFragment {
    protected String TAG_ITEMS = SharedRegistry.STATE_TAG_GLOBAL;
    protected Context context;
    protected SharedRegistry viewRegistry;
    protected final ExecutorService executor = Executors.newFixedThreadPool(3); //Max Threads ?
    protected final List<T> items = new ArrayList<>();

    protected boolean useOriginalState = true;
    protected String title = "Check";

    protected IDialogEventFinish dialogEvent;

    private boolean isDialogDismissed = false;
    private boolean areThreadsComplete = false;

    public CheckableDialog<T> setDialogEvent(IDialogEventFinish onDialogEvent) {
        this.dialogEvent = onDialogEvent;
        return this;
    }

    protected abstract void onFinishedPush(List<T> enabled, List<T> disabled);

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_hooks, null);
        ListView listView = view.findViewById(R.id.listViewHooks);

        //Init Pre Checks
        String[] ids = new String[items.size()];
        boolean[] initialCheckedStates = new boolean[items.size()];
        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            ids[i] = item.getId();
            initialCheckedStates[i] = viewRegistry.isChecked(TAG_ITEMS, item.getId());
        }

        builder.setTitle(title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    List<T> enabled = new ArrayList<>();
                    List<T> disabled = new ArrayList<>();

                    for (int i = 0; i < items.size(); i++) {
                        T item = items.get(i);
                        boolean isChecked = listView.isItemChecked(i);
                        if(useOriginalState) {
                            boolean originalFlag = viewRegistry.isChecked(TAG_ITEMS, item.getId());
                            if (isChecked && !originalFlag) {
                                enabled.add(item);
                            } else if (!isChecked && originalFlag) {
                                disabled.add(item);
                            }
                        } else {
                            if (isChecked) {
                                enabled.add(item);
                            } else {
                                disabled.add(item);
                            }
                        }
                    }

                    //onFinishedPush(enabled, disabled);
                    new Thread(() -> {
                        try {
                            // Call onFinishedPush
                            onFinishedPush(enabled, disabled);

                            // Shutdown executor and wait for completion
                            executor.shutdown();
                            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                                executor.shutdownNow();
                            }

                            // Mark threads as complete
                            areThreadsComplete = true;

                            // Check if we can call the callback
                            checkAndCallCallback();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            executor.shutdownNow();
                        }
                    }).start();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dismiss())
                .setNeutralButton(R.string.title_check_all, null);

        AlertDialog dialog = builder.create();

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_list_item_multiple_choice,
                ids
        );

        listView.setAdapter(adapter);

        for (int i = 0; i < initialCheckedStates.length; i++) {
            listView.setItemChecked(i, initialCheckedStates[i]);
        }

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            android.widget.Button checkAllButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            updateButtonText(checkAllButton, listView);
        });

        dialog.setOnShowListener(dialogInterface -> {
            android.widget.Button checkAllButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            updateButtonText(checkAllButton, listView);

            checkAllButton.setOnClickListener(view1 -> {
                boolean allChecked = areAllChecked(listView);
                boolean someChecked = areSomeChecked(listView);

                if (allChecked || someChecked) {
                    for (int i = 0; i < items.size(); i++) {
                        listView.setItemChecked(i, false);
                    }
                } else {
                    for (int i = 0; i < items.size(); i++) {
                        listView.setItemChecked(i, true);
                    }
                }
                updateButtonText(checkAllButton, listView);
            });
        });

        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private synchronized void checkAndCallCallback() {
        if (isDialogDismissed && areThreadsComplete && dialogEvent != null) {
            // Just create a new thread directly
            new Thread(() -> dialogEvent.onFinish()).start();
        }
    }

    private boolean areAllChecked(ListView listView) {
        for (int i = 0; i < listView.getCount(); i++) {
            if (!listView.isItemChecked(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean areSomeChecked(ListView listView) {
        int checkedCount = 0;
        for (int i = 0; i < listView.getCount(); i++) {
            if (listView.isItemChecked(i)) {
                checkedCount++;
            }
        }
        return checkedCount > 0 && checkedCount < listView.getCount();
    }

    @SuppressLint("SetTextI18n")
    private void updateButtonText(android.widget.Button button, ListView listView) {
        int checkedCount = 0;
        int totalCount = listView.getCount();

        for (int i = 0; i < totalCount; i++) {
            if (listView.isItemChecked(i)) {
                checkedCount++;
            }
        }

        if (checkedCount == totalCount) {
            button.setText(getString(R.string.title_uncheck_all) + " (" + checkedCount + ")");
        } else if (checkedCount > 0) {
            button.setText(getString(R.string.title_uncheck_all) + " (" + checkedCount + ")");
        } else {
            button.setText(getString(R.string.title_check_all) + " (" + totalCount + ")");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        isDialogDismissed = true;
        checkAndCallCallback();
    }
}
