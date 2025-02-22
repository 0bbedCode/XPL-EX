package eu.faircode.xlua.x.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.XUtil;

public class HelpDialog extends AppCompatDialogFragment {
    private Context context;
    private final List<HelpItem> helpItems = new ArrayList<>();

    public static class HelpItem {
        public final int resourceId;  // Either drawable resource ID or color attribute
        public final boolean isColor; // Flag to determine if this is a color item
        public final String title;
        public final String description;

        // Constructor for drawable icons
        public HelpItem(int drawableResId, String title, String description) {
            this.resourceId = drawableResId;
            this.isColor = false;
            this.title = title;
            this.description = description;
        }

        // Static factory method for colors
        public static HelpItem createWithColor(int colorAttr, String title, String description) {
            return new HelpItem(colorAttr, true, title, description);
        }

        // Private constructor used by createWithColor
        private HelpItem(int resourceId, boolean isColor, String title, String description) {
            this.resourceId = resourceId;
            this.isColor = isColor;
            this.title = title;
            this.description = description;
        }
    }

    private class HelpAdapter extends BaseAdapter {
        private final LayoutInflater inflater;

        public HelpAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return helpItems.size();
        }

        @Override
        public Object getItem(int position) {
            return helpItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.help_list_item, parent, false);
                holder = new ViewHolder();
                holder.icon = convertView.findViewById(R.id.ivHelpIcon);
                holder.colorBlock = convertView.findViewById(R.id.vHelpColor);
                holder.title = convertView.findViewById(R.id.tvHelpTitle);
                holder.description = convertView.findViewById(R.id.tvHelpDescription);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HelpItem item = helpItems.get(position);

            if (item.isColor) {
                holder.icon.setVisibility(View.GONE);
                holder.colorBlock.setVisibility(View.VISIBLE);
                holder.colorBlock.setBackgroundColor(XUtil.resolveColor(context, item.resourceId));
            } else {
                holder.icon.setVisibility(View.VISIBLE);
                holder.colorBlock.setVisibility(View.GONE);
                holder.icon.setImageResource(item.resourceId);
            }

            holder.title.setText(item.title);
            holder.description.setText(item.description);

            return convertView;
        }

        private class ViewHolder {
            ImageView icon;
            View colorBlock;
            TextView title;
            TextView description;
        }
    }

    public static HelpDialog create() {
        return new HelpDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.help_dialog, null);

        // Initialize help items here where we have context
        initializeHelpItems();

        ListView lvHelp = view.findViewById(R.id.lvHelp);
        lvHelp.setAdapter(new HelpAdapter(context));

        builder.setView(view)
                .setTitle(R.string.title_help)
                .setNegativeButton(R.string.option_close, null);

        return builder.create();
    }

    private void initializeHelpItems() {
        helpItems.clear();

        // Delete action
        helpItems.add(new HelpItem(
                R.drawable.ic_delete18,
                getString(R.string.help_delete_title),
                getString(R.string.help_delete_description)
        ));

        // Save action
        helpItems.add(new HelpItem(
                R.drawable.ic_save18,
                getString(R.string.help_save_title),
                getString(R.string.help_save_description)
        ));

        // Reset action
        helpItems.add(new HelpItem(
                R.drawable.ic_reset18,
                getString(R.string.help_reset_title),
                getString(R.string.help_reset_description)
        ));

        // Randomize action
        helpItems.add(new HelpItem(
                R.drawable.ic_random18,
                getString(R.string.help_randomize_title),
                getString(R.string.help_randomize_description)
        ));

        // Super Random action
        helpItems.add(new HelpItem(
                R.drawable.ic_rnd18,
                getString(R.string.help_super_random_title),
                getString(R.string.help_super_random_description)
        ));

        // Hook Control action
        helpItems.add(new HelpItem(
                R.drawable.ic_hook_control18,
                getString(R.string.help_hook_title),
                getString(R.string.help_hook_description)
        ));

        helpItems.add(new HelpItem(
                android.R.drawable.ic_dialog_alert,
                getString(R.string.help_warning_title),
                getString(R.string.help_warning_description)
        ));

        // Color items
        helpItems.add(HelpItem.createWithColor(
                R.attr.colorUnsavedSetting,
                getString(R.string.help_color_unsaved_title),
                getString(R.string.help_color_unsaved_description)
        ));

        helpItems.add(HelpItem.createWithColor(
                R.attr.colorAccent,
                getString(R.string.help_color_saved_title),
                getString(R.string.help_color_saved_description)
        ));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}