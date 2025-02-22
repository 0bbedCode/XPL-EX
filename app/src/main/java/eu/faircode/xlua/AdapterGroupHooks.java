package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.assignment.LuaAssignment;
import eu.faircode.xlua.api.settings.LuaSettingExtended;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.HookGroup;
import eu.faircode.xlua.ui.interfaces.IHookTransaction;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.utilities.SettingUtil;
import eu.faircode.xlua.utilities.StringUtil;
import eu.faircode.xlua.utilities.UiUtil;
import eu.faircode.xlua.utilities.ViewUtil;

public class AdapterGroupHooks extends RecyclerView.Adapter<AdapterGroupHooks.ViewHolder> implements Filterable {
    private final LinkedHashMap<HookGroup, List<XLuaHook>> all = new LinkedHashMap<>();
    private LinkedHashMap<HookGroup, List<XLuaHook>> filtered_max = new LinkedHashMap<>();

    private List<HookGroup> filtered_groups = new ArrayList<>();
    private List<List<XLuaHook>> filtered_hooks = new ArrayList<>();

    private final HashMap<String, Boolean> expanded = new HashMap<>();

    private boolean dataChanged = false;
    private CharSequence query = null;
    private ILoader fragmentLoader;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements
            CompoundButton.OnCheckedChangeListener,
            View.OnClickListener,
            IHookTransaction {

        final View view;
        final TextView tvGroupName, tvHooksCount, tvHooksSelectedCount;
        final CheckBox cbGroup;
        final ImageView ivExpander;
        final CardView cardView;
        final RecyclerView rvHooks;
        final AdapterHook adapterHook;

        ViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;
            this.tvGroupName = itemView.findViewById(R.id.tvHookGroupName);
            this.cbGroup = itemView.findViewById(R.id.cbHookGroup);
            this.rvHooks = itemView.findViewById(R.id.rvHookGroup);
            this.ivExpander = itemView.findViewById(R.id.ivExpanderGroup);
            this.tvHooksCount = itemView.findViewById(R.id.tvHookGroupsHooksCount);
            this.tvHooksSelectedCount = itemView.findViewById(R.id.tvHookGroupsHooksCountSelected);

            this.cardView = itemView.findViewById(R.id.cvHookGroups);
            adapterHook = new AdapterHook(fragmentLoader);
            UiUtil.initRv(itemView.getContext(), rvHooks, adapterHook);
        }

        @SuppressLint("ClickableViewAccessibility")
        private void unWire() {
            this.ivExpander.setOnClickListener(null);
            this.tvGroupName.setOnClickListener(null);
            this.cbGroup.setOnCheckedChangeListener(null);
        }

        @SuppressLint("ClickableViewAccessibility")
        private void wire() {
            this.ivExpander.setOnClickListener(this);
            this.tvGroupName.setOnClickListener(this);
            this.cbGroup.setOnCheckedChangeListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(final View view) {
            int id = view.getId();
            XLog.i("onClick id=" + id);
            try {
                final HookGroup group = filtered_groups.get(getAdapterPosition());
                final String name = group.name;
                switch (id) {
                    case R.id.itemViewGroupHooks:
                    case R.id.tvHookGroupName:
                    case R.id.ivExpanderGroup:
                        ViewUtil.internalUpdateExpanded(expanded, name);
                        updateExpanded();
                        break;
                }
            }catch (Exception e) { XLog.e("onClick Failed: code=" + id, e, true); }
        }

        @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
        @Override
        public void onCheckedChanged(final CompoundButton cButton, final boolean isChecked) {
            HookGroup group = filtered_groups.get(getAdapterPosition());//this was originally group ??
            group.sendAll(cButton.getContext(), getAdapterPosition(), isChecked, this);
        }

        void updateExpanded() {
            HookGroup group = filtered_groups.get(getAdapterPosition());
            String name = group.name;
            boolean isExpanded = expanded.containsKey(name) && Boolean.TRUE.equals(expanded.get(name));
            ViewUtil.setViewsVisibility(ivExpander, isExpanded, rvHooks);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onGroupFinished(List<LuaAssignment> assignments, int position, boolean assign, XResult result) {
            try {
                if(position < 0) throw new Exception("Invalid Position: " + position);
                notifyItemChanged(position);
            }catch (Exception e) {
                XLog.e("Failed to Init Update for Hooks: position=" + position + " assign=" + assign + " assignments count=" + assignments.size(), e, true);
                notifyDataSetChanged();
            }
        }
    }

    AdapterGroupHooks() { setHasStableIds(true); }
    AdapterGroupHooks(ILoader fragmentLoader) {  this(); this.fragmentLoader = fragmentLoader; }

    @Override
    public long getItemId(int position) { return filtered_groups.get(position).name.hashCode(); }

    @Override
    public int getItemCount() { return filtered_max.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.hookgroup, parent, false)); }

    public void set(List<HookGroup> groups) {
        this.dataChanged = true;
        all.clear();
        for(HookGroup g : groups) all.put(g, g.getHooks());
        XLog.i("Groups size=" + this.all.size());
        getFilter().filter(query);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            private boolean expanded1 = false;
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                AdapterGroupHooks.this.query = query;

                LinkedHashMap<HookGroup, List<XLuaHook>> visible = new LinkedHashMap<>(all);
                LinkedHashMap<HookGroup, List<XLuaHook>> results = new LinkedHashMap<>();

                if (!StringUtil.isValidAndNotWhitespaces(query)) results.putAll(visible);
                else {
                    String q = query.toString().toLowerCase().trim();
                    if(q.equals("!")) {
                        results.putAll(visible);
                    }else {
                        boolean filterSubHooks = q.startsWith("!");
                        if(filterSubHooks)  q = q.substring(1);
                        XLog.i("Filtering query=" + q + "\nSubHooks=" + filterSubHooks);

                        for(Map.Entry<HookGroup, List<XLuaHook>> e : visible.entrySet()) {
                            HookGroup group = e.getKey();
                            if(SettingUtil.cleanSettingName(group.name).toLowerCase().contains(q) ||
                                    group.name.toLowerCase().contains(q) ||
                                    group.title.toLowerCase().contains(q) ||
                                    filterSubHooks) {
                                List<XLuaHook> hooks = new ArrayList<>();
                                if(filterSubHooks) {
                                    for(XLuaHook h : group.getHooks()) {
                                        if(h.getObjectId().toLowerCase().contains(q))
                                            hooks.add(h);
                                        else {
                                            for(LuaSettingExtended s : h.getManagedSettings()) {
                                                if(s.getName().toLowerCase().contains(q) || SettingUtil.cleanSettingName(s.getName()).toLowerCase().contains(q)) {
                                                    hooks.add(h);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }else {
                                    hooks = group.getHooks();
                                }

                                if(!hooks.isEmpty())
                                    results.put(group, hooks);
                            }
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = results;
                filterResults.count = results.size();
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence query, FilterResults result) {
                try {
                    final LinkedHashMap<HookGroup, List<XLuaHook>> groups =
                            (result == null ? new LinkedHashMap<HookGroup, List<XLuaHook>>() : (LinkedHashMap<HookGroup, List<XLuaHook>>)result.values);

                    if(dataChanged) {
                        dataChanged = false;
                        filtered_max = groups;
                        filtered_groups = new ArrayList<>(groups.keySet());
                        filtered_hooks = new ArrayList<>(groups.values());
                        XLog.i("FM=" + filtered_max.size() + " FG=" + filtered_groups.size() + " FH=" + filtered_hooks.size());
                        notifyDataSetChanged();
                    }else {
                        DiffUtil.DiffResult diff =
                                DiffUtil.calculateDiff(new GroupHooksDiffCallback(expanded1,
                                        filtered_max,
                                        groups));
                        filtered_max = groups;
                        filtered_groups = new ArrayList<>(groups.keySet());
                        filtered_hooks = new ArrayList<>(groups.values());
                        XLog.i("FM=" + filtered_max.size() + " FG=" + filtered_groups.size() + " FH=" + filtered_hooks.size());
                        diff.dispatchUpdatesTo(AdapterGroupHooks.this);
                    }
                }catch (Exception e) {
                    XLog.e("Failed to Publish Results for Adapter Settings", e);
                }
            }
        };
    }

    private static class GroupHooksDiffCallback extends DiffUtil.Callback {
        private final boolean refresh;
        private final List<HookGroup> prevGroups;
        private final List<List<XLuaHook>> prevHooks;
        private final List<HookGroup> nextGroups;
        private final List<List<XLuaHook>> nextHooks;

        GroupHooksDiffCallback(boolean refresh, LinkedHashMap<HookGroup, List<XLuaHook>> prev, LinkedHashMap<HookGroup, List<XLuaHook>> next) {
            this.refresh = refresh;
            this.prevGroups = new ArrayList<>(prev.keySet());
            this.prevHooks = new ArrayList<>(prev.values());
            this.nextGroups = new ArrayList<>(next.keySet());
            this.nextHooks = new ArrayList<>(next.values());
        }

        @Override
        public int getOldListSize() { return prevGroups.size(); }

        @Override
        public int getNewListSize() { return nextGroups.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            HookGroup g1 = prevGroups.get(oldItemPosition);
            HookGroup g2 = nextGroups.get(newItemPosition);
            return !refresh && g1.name.equalsIgnoreCase(g2.name);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            List<XLuaHook> h1 = prevHooks.get(oldItemPosition);
            List<XLuaHook> h2 = nextHooks.get(newItemPosition);
            if(h1.size() == h2.size()) {
                for(XLuaHook h : h1)
                    if(!h2.contains(h))
                        return false;
            }else return false;
            return true;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.unWire();
        HookGroup group = filtered_groups.get(position);
        List<XLuaHook> hooks = filtered_hooks.get(position);
        holder.tvGroupName.setText(SettingUtil.cleanSettingName(group.title));
        holder.adapterHook.set(group, hooks);
        holder.tvHooksCount.setText(new StringBuilder().append("[").append(group.hooksSize()).append("]"));
        holder.tvHooksSelectedCount.setText(new StringBuilder().append(group.getAssigned()));
        holder.tvHooksSelectedCount.setVisibility(group.hasAssigned() ? View.VISIBLE : View.GONE);
        Resources resources = holder.itemView.getContext().getResources();
        holder.cbGroup.setButtonTintList(ColorStateList.valueOf(resources.getColor(
                group.allAssigned() ? R.color.colorAccent : android.R.color.darker_gray, null)));

        holder.cbGroup.setChecked(group.hasAssigned());
        XLog.i("group=" + group.name + " assignments=" + group.getAssigned());
        holder.updateExpanded();
        holder.wire();
    }
}
