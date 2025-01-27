package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.core.dialog.IDialogOptionsEvent;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;

public class OptionsListDialog extends AppCompatDialogFragment {
    private Context context;
    private String message = "";
    private String title = "";
    private int iconResId = -1;
    //private String defaultChecked = "";
    private final List<String> options = new ArrayList<>();
    private final List<String> checked = new ArrayList<>();
    private boolean allowMultiple = true;

    private int spinnerKind = android.R.layout.simple_list_item_checked;

    private IDialogOptionsEvent onOptionsEvent;

    private Runnable onConfirm;
    private Runnable onCancel;
    private ListView listView;

    public static OptionsListDialog create() {
        return new OptionsListDialog();
    }


    public OptionsListDialog setChecked(List<String> checked) {
        ListUtil.addAllIfValid(this.checked, checked, true);
        return this;
    }

    public OptionsListDialog setDefaultCheck(String option) {
        this.checked.add(option);
        return this;
    }

    public OptionsListDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public OptionsListDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public OptionsListDialog setIcon(int resourceId) {
        this.iconResId = resourceId;
        return this;
    }

    public OptionsListDialog setOptions(List<String> options) {
        this.options.clear();
        if (options != null) {
            this.options.addAll(options);
        }
        return this;
    }

    public OptionsListDialog setAllowMultiple(boolean allow) {
        this.allowMultiple = allow;
        return this;
    }

    //public OptionsListDialog onConfirm(Runnable action) {
    //    this.onConfirm = action;
    //    return this;
    //}

    public OptionsListDialog onConfirm(IDialogOptionsEvent onEvent) {
        this.onOptionsEvent = onEvent;
        return this;
    }

    public OptionsListDialog onCancel(Runnable action) {
        this.onCancel = action;
        return this;
    }

    public List<Integer> getSelectedPositions() {
        List<Integer> selected = new ArrayList<>();
        if (listView != null) {
            for (int i = 0; i < listView.getCount(); i++) {
                if (listView.isItemChecked(i)) {
                    selected.add(i);
                }
            }
        }
        return selected;
    }

    public OptionsListDialog setCheckStyleMaterial() {
        spinnerKind = android.R.layout.simple_list_item_multiple_choice;
        return this;
    }

    public OptionsListDialog setCheckStyleCheck() {
        spinnerKind = android.R.layout.simple_list_item_checked;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.options_list_dialog, null);

        TextView messageView = view.findViewById(R.id.tvDialogMessage);
        ImageView iconView = view.findViewById(R.id.ivDialogIcon);
        listView = view.findViewById(R.id.lvOptions);

        messageView.setText(message);

        if (iconResId != -1) {
            iconView.setImageResource(iconResId);
            iconView.setVisibility(View.VISIBLE);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                spinnerKind,
                options);

        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setChoiceMode(allowMultiple ?
                ListView.CHOICE_MODE_MULTIPLE :
                ListView.CHOICE_MODE_SINGLE);


        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = CoreUiUtils.dpToPx(context, options.size() > 2 ? 230 : 130); // Set height in pixels
        listView.setLayoutParams(params);

        // Set default checked option if specified
        if (!checked.isEmpty()) {
            for (int i = 0; i < options.size(); i++) {
                String option = options.get(i);
                if (checked.contains(option)) {
                    listView.setItemChecked(i, true);
                    if(checked.size() == 1)
                        break;
                }
            }
        }


        if (!allowMultiple) {
            listView.setOnItemClickListener((parent, itemView, position, id) -> {
                // Uncheck all other items when in single choice mode
                for (int i = 0; i < listView.getCount(); i++) {
                    if (i != position) {
                        listView.setItemChecked(i, false);
                    }
                }
            });
        }

        builder.setView(view)
                .setTitle(title)
                .setPositiveButton(R.string.option_ok, (dialog, which) -> {
                    if(onOptionsEvent != null) {
                        List<String> enabled = new ArrayList<>();
                        List<String> disabled = new ArrayList<>();
                        if (listView != null) {
                            for (int i = 0; i < listView.getCount(); i++) {
                                if (listView.isItemChecked(i))
                                    enabled.add(options.get(i));
                                else if(listView.isItemChecked(i))
                                    disabled.add(options.get(i));
                            }
                        }

                        onOptionsEvent.onPositive(enabled, disabled);
                    }
                })
                .setNegativeButton(R.string.option_cancel, (dialog, which) -> {
                    if (onCancel != null) onCancel.run();
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}