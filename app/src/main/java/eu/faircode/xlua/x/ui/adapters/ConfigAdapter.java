package eu.faircode.xlua.x.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.xlua.configs.XPConfig;

public class ConfigAdapter extends ArrayAdapter<XPConfig> {
    private final Context context;
    private final LayoutInflater inflater;

    //private XPConfig enabledConfig;
    private XPConfig checkedConfig;
    private final OnConfigActionListener listener;

    private List<XPConfig> originalConfigs;
    private Filter configFilter;

    public interface OnConfigActionListener {
        void onConfigChecked(XPConfig config);
        void onConfigDelete(XPConfig config);
    }

    public ConfigAdapter(Context context, List<XPConfig> configs, OnConfigActionListener listener) {
        super(context, 0, configs);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public XPConfig checkedOrEnabled(XPConfig config, boolean followFilter) {
        List<XPConfig> items = new ArrayList<>();
        if(followFilter) {
            for (int i = 0; i < getCount(); i++)
                items.add(getItem(i));
        } else {
            items.addAll(originalConfigs);
        }

        XPConfig checked = null;
        if(this.checkedConfig != null) {
            for(XPConfig c : items) {
                if(c.name.equalsIgnoreCase(this.checkedConfig.name)) {
                    checked = c;
                    break;
                }
            }
        }

        if(checked == null && config != null) {
            XPConfig copy = config;
            config = null;
            for(XPConfig c : items) {
                if(c.name.equalsIgnoreCase(copy.name)) {
                    checked = c;
                    break;
                }
            }
        }

        return checked == null ? config : checked;
    }

    public void setCheckedConfig(XPConfig config) {
        this.checkedConfig = config;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        XPConfig config = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.config_list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (config != null) {
            holder.tvConfigName.setText(config.name);
            holder.tvConfigAuthor.setText(config.author);
            holder.tvConfigVersion.setText(config.version);
            holder.cbConfigEnabled.setChecked(config == checkedConfig);

            holder.tvSettingsCount.setText(config.settings.isEmpty() ? "---" : String.valueOf(config.settings.size()));
            holder.tvHookCount.setText(config.hooks.isEmpty() ? "---" : String.valueOf(config.hooks.size()));

            List<String> tags = config.getTags();

            holder.tvTagCount.setText(tags.isEmpty() ? "---" : String.valueOf(tags.size()));


            holder.tagContainer.removeAllViews();
            holder.flexTags.removeAllViews();

            int totalWidth = 0;
            int parentWidth = parent.getWidth() - CoreUiUtils.dpToPx(context, 40); // Account for padding and margins

            for (String tag : tags) {
                Button tagButton = createStyledButton(tag);
                Button scrollTagButton = createStyledButton(tag);

                // Measure button width
                scrollTagButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                totalWidth += scrollTagButton.getMeasuredWidth(); // Account for margins


                holder.tagContainer.addView(scrollTagButton);
                holder.flexTags.addView(tagButton);
            }

            boolean needsExpander = totalWidth > parentWidth;
            holder.ivTagsExpander.setVisibility(needsExpander ? View.VISIBLE : View.GONE);

            // Handle expander click
            holder.ivTagsExpander.setOnClickListener(v -> {
                boolean isExpanded = holder.flexTags.getVisibility() == View.VISIBLE;
                holder.flexTags.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                holder.horizontalTagScroll.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                holder.ivTagsExpander.setRotation(isExpanded ? 0 : 180); // Rotate arrow
            });

            // Handle checkbox click
            holder.cbConfigEnabled.setOnClickListener(v -> {
                boolean isChecked = holder.cbConfigEnabled.isChecked();
                if (isChecked) {
                    //if (listener != null)
                    //    listener.onConfigChecked(config);

                    //setCheckedConfig(config);
                    //if(listener.onConfigChecked(config))
                    this.checkedConfig = config;
                    if(listener != null)
                        listener.onConfigChecked(config);

                } else if (config == checkedConfig) {
                    holder.cbConfigEnabled.setChecked(true); // Keep it checked if trying to uncheck current ?
                }
            });

            // Handle delete button click
            holder.ivConfigDeleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfigDelete(config);

                }
            });
        }

        return convertView;
    }


    @Override
    public Filter getFilter() {
        if (configFilter == null) {
            configFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    if (constraint == null || constraint.length() == 0) {
                        results.values = new ArrayList<>(originalConfigs);
                        results.count = originalConfigs.size();
                    } else {
                        List<XPConfig> filteredList = new ArrayList<>();
                        String filterPattern = constraint.toString().toLowerCase().trim();

                        for (XPConfig config : originalConfigs) {
                            if (config.name.toLowerCase().contains(filterPattern) ||
                                    config.author.toLowerCase().contains(filterPattern) ||
                                    config.version.toLowerCase().contains(filterPattern)) {
                                filteredList.add(config);
                            }
                        }

                        results.values = filteredList;
                        results.count = filteredList.size();
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    clear();
                    addAll((List<XPConfig>) results.values);
                    notifyDataSetChanged();
                }
            };
        }
        return configFilter;
    }

    public void setOriginalConfigs(List<XPConfig> configs) {
        this.originalConfigs = new ArrayList<>(configs);
    }

    private Button createStyledButton(String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setBackgroundResource(R.drawable.rounded_corner);
        button.setPadding(6, 0, 6, 0); // Padding for content inside the button
        button.setTextSize(10);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(12, 12, 12, 12); // Equal spacing between buttons
        params.height = CoreUiUtils.dpToPx(context, 30);
        button.setLayoutParams(params);
        return button;
    }

    /*private Button createStyledButton(String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setBackgroundResource(R.drawable.rounded_corner);
        //button.setPadding(30, 0, 30, 0);
        button.setPadding(10, 0, 10, 0); // Padding for content inside the button
        button.setTextSize(10);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                CoreUiUtils.dpToPx(context, 30));

        //params.setMargins(8, 6, 8, 6);
        params.setMargins(12, 12, 12, 12); // Equal spacing between buttons
        params.height = CoreUiUtils.dpToPx(context, 30);

        button.setLayoutParams(params);
        button.setMinimumWidth(0); // Allow button to shrink to content
        button.setMinWidth(0);
        return button;
    }*/

    private static class ViewHolder {
        final ImageView ivConfigIcon;
        final TextView tvConfigName;
        final TextView tvConfigAuthor;
        final TextView tvConfigVersion;
        final CheckBox cbConfigEnabled;
        final ImageView ivConfigDeleteButton;

        final TextView tvSettingsCount;
        final TextView tvHookCount;
        final TextView tvTagCount;

        final LinearLayout tagContainer;
        final HorizontalScrollView horizontalTagScroll;
        final ImageView ivTagsExpander;
        final FlexboxLayout flexTags;

        ViewHolder(View view) {
            ivConfigIcon = view.findViewById(R.id.ivConfigIcon);
            tvConfigName = view.findViewById(R.id.tvConfigName);
            tvConfigAuthor = view.findViewById(R.id.tvConfigAuthor);
            tvConfigVersion = view.findViewById(R.id.tvConfigVersion);
            cbConfigEnabled = view.findViewById(R.id.cbConfigEnabled);
            ivConfigDeleteButton = view.findViewById(R.id.ivConfigDeleteButton);

            tvSettingsCount = view.findViewById(R.id.tvSettingsCount);
            tvHookCount = view.findViewById(R.id.tvHookCount);
            tvTagCount = view.findViewById(R.id.tvTagCount);

            tagContainer = view.findViewById(R.id.tagContainer);
            horizontalTagScroll = view.findViewById(R.id.horizontalTagScroll);
            ivTagsExpander = view.findViewById(R.id.ivTagsExpander);
            flexTags = view.findViewById(R.id.flexTags);
        }
    }
}