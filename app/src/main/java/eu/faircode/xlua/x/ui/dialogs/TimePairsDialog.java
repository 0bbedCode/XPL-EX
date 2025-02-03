package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;

public class TimePairsDialog extends AppCompatDialogFragment implements TimeInputDialog.IDialogTimeFinish {
    private static final String TAG = LibUtil.generateTag(TimePairsDialog.class);

    private Context context;
    private final List<String> timePairs = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView lvTimePairs;
    private Button btnLeftAction;
    private IDialogTimePairsFinish timePairsFinishListener;

    public interface IDialogTimePairsFinish {
        void onTimePairsFinish(List<String> timePairs);
    }

    public static TimePairsDialog create() {
        return new TimePairsDialog();
    }

    public TimePairsDialog setTimePairs(List<String> pairs) {
        this.timePairs.clear();
        ListUtil.addAllIfValid(this.timePairs, pairs);
        return this;
    }

    public TimePairsDialog setTimePairsFinishListener(IDialogTimePairsFinish listener) {
        this.timePairsFinishListener = listener;
        return this;
    }

    @Override
    public void onTimeFinish(long value, String symbol) {
        String timePair = value + ":" + symbol;
        timePairs.add(timePair);
        adapter.notifyDataSetChanged();
        updateLeftButtonState();
    }

    private void updateLeftButtonState() {
        if (lvTimePairs == null || btnLeftAction == null) return;

        boolean hasSelection = false;
        for (int i = 0; i < lvTimePairs.getCount(); i++) {
            if (lvTimePairs.isItemChecked(i)) {
                hasSelection = true;
                break;
            }
        }

        btnLeftAction.setText(hasSelection ? R.string.option_delete : R.string.option_create);
    }

    private void handleLeftButtonClick() {
        if (lvTimePairs == null) return;

        boolean hasSelection = false;
        List<Integer> selectedPositions = new ArrayList<>();

        for (int i = 0; i < lvTimePairs.getCount(); i++) {
            if (lvTimePairs.isItemChecked(i)) {
                hasSelection = true;
                selectedPositions.add(i);
            }
        }

        if (hasSelection) {
            // Remove selected items (in reverse order to maintain indices)
            for (int i = selectedPositions.size() - 1; i >= 0; i--) {
                timePairs.remove(selectedPositions.get(i).intValue());
            }
            adapter.notifyDataSetChanged();

            // Clear all selections after deletion
            for (int i = 0; i < lvTimePairs.getCount(); i++) {
                lvTimePairs.setItemChecked(i, false);
            }
        } else {
            // Show time input dialog
            TimeInputDialog.create()
                    .setTimeFinishListener(this)
                    .show(getParentFragmentManager(), getString(R.string.title_time_input));
        }

        updateLeftButtonState();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.time_pairs_dialog, null);

        lvTimePairs = view.findViewById(R.id.lvTimePairs);
        adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_list_item_multiple_choice,
                timePairs
        );
        lvTimePairs.setAdapter(adapter);

        lvTimePairs.setOnItemClickListener((parent, itemView, position, id) -> {
            updateLeftButtonState();
        });

        builder.setView(view)
                .setTitle(R.string.title_time_pairs)
                .setNegativeButton(R.string.option_cancel, null)
                .setNeutralButton(R.string.option_create, null)
                .setPositiveButton(R.string.option_apply, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            btnLeftAction = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            updateLeftButtonState();

            btnLeftAction.setOnClickListener(v -> handleLeftButtonClick());

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (timePairsFinishListener != null) {
                    timePairsFinishListener.onTimePairsFinish(new ArrayList<>(timePairs));
                }
                dialog.dismiss();
            });
        });

        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}