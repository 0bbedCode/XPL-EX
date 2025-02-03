package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.assignment.LuaAssignment;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.HookGroup;
import eu.faircode.xlua.ui.interfaces.IHookTransactionEx;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.ui.transactions.HookTransactionResult;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.utilities.UiUtil;
import eu.faircode.xlua.utilities.ViewUtil;

public class AdapterHook extends RecyclerView.Adapter<AdapterHook.ViewHolder> {
    private final HashMap<String, Boolean> expanded = new HashMap<>();
    private List<XLuaHook> hooks = new ArrayList<>();
    private HookGroup group;
    private ILoader fragmentLoader;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements
            CompoundButton.OnCheckedChangeListener,
            View.OnClickListener,
            View.OnLongClickListener,
            IHookTransactionEx {

        final View view;
        final TextView tvHookName;
        final CheckBox cbEnableHook;
        final RecyclerView rvHookSettings;
        final AdapterHookSettings adapterSettings;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.tvHookName = view.findViewById(R.id.tvHookName);
            this.rvHookSettings = view.findViewById(R.id.rvHookSettings);
            this.cbEnableHook = view.findViewById(R.id.cbHook);

            adapterSettings = new AdapterHookSettings(fragmentLoader);
            UiUtil.initRv(itemView.getContext(), rvHookSettings, adapterSettings);
        }

        @SuppressLint("ClickableViewAccessibility")
        private void unWire() {
            this.view.setOnClickListener(null);
            this.tvHookName.setOnClickListener(null);
            this.cbEnableHook.setOnCheckedChangeListener(null);
            this.view.setOnLongClickListener(null);
            this.tvHookName.setOnLongClickListener(null);
        }

        @SuppressLint("ClickableViewAccessibility")
        private void wire() {
            this.view.setOnClickListener(this);
            this.tvHookName.setOnClickListener(this);
            this.cbEnableHook.setOnCheckedChangeListener(this);
            this.view.setOnLongClickListener(this);
            this.tvHookName.setOnLongClickListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(final View view) {
            int id = view.getId();
            XLog.i("onClick id=" + id);
            try {
                final XLuaHook hook = hooks.get(getAdapterPosition());
                switch (id) {
                    case R.id.itemViewHooks:
                    case R.id.tvHookName:
                        ViewUtil.internalUpdateExpanded(expanded, hook.getSharedId());
                        updateExpanded();
                        break;
                }
            }catch (Exception e) { XLog.e("onClick Failed: code=" + id, e); }
        }

        @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
        @Override
        public void onCheckedChanged(final CompoundButton cButton, final boolean isChecked) {
            int id = cButton.getId();
            XLog.i("onCheckedChanged id=" + id);
            try {
                int position = getAdapterPosition();
                XLuaHook hook = hooks.get(position);
                group.send(cButton.getContext(), hook, position, isChecked, this);
            }catch (Exception e) { XLog.e("Failed to update assignment. code=" + id, e, true); }
        }

        void updateExpanded() {
            XLuaHook hook = hooks.get(getAdapterPosition());
            String name = hook.getSharedId();
            boolean isExpanded = expanded.containsKey(name) && Boolean.TRUE.equals(expanded.get(name));
            ViewUtil.setViewsVisibility(null, isExpanded, rvHookSettings);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View v) {
            int id = view.getId();
            XLog.i("onLongClick id=" + id);
            try {
                final XLuaHook hook = hooks.get(getAdapterPosition());
                switch (id) {
                    case R.id.itemViewHooks:
                    case R.id.tvHookName:
                        if(StringUtil.isValidAndNotWhitespaces(hook.getDescription())) Toast.makeText(view.getContext(), hook.getDescription(), Toast.LENGTH_SHORT).show();
                        else Toast.makeText(view.getContext(), R.string.error_no_description_hook, Toast.LENGTH_SHORT).show();
                        return true;
                }
            }catch (Exception e) { XLog.e("onLongClick Failed: code=" + id, e); }
            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onHookUpdate(HookTransactionResult result) {
            if(result == null) return;
            Snackbar.make(view, result.result.getResultMessage(), Snackbar.LENGTH_LONG).show();
            if(result.hasAnySucceeded()) {
                if (!result.getPacket().isDelete()) result.group.putAssignment(new LuaAssignment(result.getHook()));
                else result.group.removeAssignment(new LuaAssignment(result.getHook()));

                fragmentLoader.loadData();

                //if(result.getAdapterPosition() > -1) {
                //    try {
                //        notifyItemChanged(result.getAdapterPosition());
                //    }catch (Exception e) {
                //        notifyDataSetChanged();
                //        XLog.e("Failed to update Hook: " + result.getHook().getId(), e, true);
                //    }
                //}
                //else notifyDataSetChanged();
            }
        }
    }

    AdapterHook() { setHasStableIds(true); }
    AdapterHook(ILoader loader) { this(); this.fragmentLoader = loader;  }

    @SuppressLint("NotifyDataSetChanged")
    public void set(HookGroup group, List<XLuaHook> filtered_hooks) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new HooksDiffCallback(false, this.hooks, filtered_hooks));
        this.group = group;
        this.hooks = new ArrayList<>(filtered_hooks);
        diffResult.dispatchUpdatesTo(this);
    }

    private static class HooksDiffCallback extends DiffUtil.Callback {
        private final boolean refresh;
        private final List<XLuaHook> prev;
        private final List<XLuaHook> next;
        HooksDiffCallback(boolean refresh, List<XLuaHook> prev, List<XLuaHook> next) {
            this.refresh = refresh;
            this.prev = prev;
            this.next = next;
        }

        @Override
        public int getOldListSize() { return prev.size(); }

        @Override
        public int getNewListSize() { return next.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            XLuaHook h1 = prev.get(oldItemPosition);
            XLuaHook h2 = next.get(newItemPosition);
            return (!refresh && h1.getSharedId().equalsIgnoreCase(h2.getSharedId()));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            //XLuaHook h1 = prev.get(oldItemPosition);
            //XLuaHook h2 = next.get(newItemPosition);
            //return h1.getId().equalsIgnoreCase(h2.getId());
            return true;
        }
    }

    @Override
    public long getItemId(int position) { return hooks.get(position).getSharedId().hashCode(); }

    @Override
    public int getItemCount() { return hooks.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.hookgrouphook, parent, false)); }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.unWire();
        final XLuaHook hook = hooks.get(position);
        holder.tvHookName.setText(hook.getName());
        holder.tvHookName.setSelected(true);
        holder.adapterSettings.set(hook.getManagedSettings());
        holder.cbEnableHook.setChecked(this.group.containsAssignedHook(hook.getSharedId()));
        holder.updateExpanded();
        holder.wire();
    }
}
