package eu.faircode.xlua.x.ui.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.core.view_registry.SharedRegistry;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
public class SettingSmallAdapter extends BaseAdapter {
    private final Context context;
    private final List<SettingPacket> settings;
    private final SharedRegistry sharedRegistry;
    private final Runnable updateCallback;

    public SettingSmallAdapter(Context context, List<SettingPacket> settings, SharedRegistry sharedRegistry, Runnable updateCallback) {
        this.context = context;
        this.settings = settings;
        this.sharedRegistry = sharedRegistry;
        this.updateCallback = updateCallback;
    }

    @Override
    public int getCount() {
        return settings.size();
    }

    @Override
    public Object getItem(int position) {
        return settings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_setting_small, parent, false);
            holder = new ViewHolder();
            holder.tvSettingName = convertView.findViewById(R.id.tvSettingName);
            holder.tvSettingValueLabel = convertView.findViewById(R.id.tvSettingValueLabel);
            holder.tvSettingValue = convertView.findViewById(R.id.tvSettingValue);
            holder.checkBoxSelect = convertView.findViewById(R.id.cbSetting);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SettingPacket setting = settings.get(position);
        holder.tvSettingName.setText(setting.name);
        holder.tvSettingValue.setText(setting.value);

        // Update checkbox state from registry
        boolean isChecked = sharedRegistry.isChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getSharedId());
        holder.checkBoxSelect.setOnCheckedChangeListener(null);
        holder.checkBoxSelect.setChecked(isChecked);

        holder.checkBoxSelect.setOnCheckedChangeListener((buttonView, isChecked1) -> {
            sharedRegistry.setChecked(SharedRegistry.STATE_TAG_SETTINGS, setting.getSharedId(), isChecked1);
            updateCallback.run();
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView tvSettingName;
        TextView tvSettingValueLabel;
        TextView tvSettingValue;
        CheckBox checkBoxSelect;
    }
}
