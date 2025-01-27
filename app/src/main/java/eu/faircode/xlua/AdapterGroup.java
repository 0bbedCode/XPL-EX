/*
    This file is part of XPrivacyLua.

    XPrivacyLua is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    XPrivacyLua is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with XPrivacyLua.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2017-2019 Marcel Bokhorst (M66B)
 */

package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import eu.faircode.xlua.api.hook.LuaHooksGroup;
import eu.faircode.xlua.api.hook.XLuaHook;

import eu.faircode.xlua.api.xstandard.interfaces.IDividerKind;
import eu.faircode.xlua.ui.GroupHelper;
import eu.faircode.xlua.ui.HookWarnings;
import eu.faircode.xlua.ui.dialogs.HookWarningDialog;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.utilities.SettingUtil;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;


public class AdapterGroup extends RecyclerView.Adapter<AdapterGroup.ViewHolder> implements IDividerKind {
    private AppXpPacket app;
    private List<LuaHooksGroup> groups = new ArrayList<>();
    private ILoader fragmentLoader;

    @Override
    public String getDividerID(int position) { return groups.get(position).groupId; }

    @Override
    public String getLongID(int position) { return groups.get(position).name; }

    @Override
    public boolean isSearching() { return false; }

    @Override
    public boolean hasChanged() { return false; }

    @Override
    public void resetHashChanged() { }

    public class ViewHolder extends
            RecyclerView.ViewHolder
            implements CompoundButton.OnCheckedChangeListener,
            View.OnClickListener {
        final View itemView;
        final ImageView ivException;
        final ImageView ivInstalled;
        final TextView tvUsed;
        final TextView tvGroup;
        final AppCompatCheckBox cbAssigned;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            ivException = itemView.findViewById(R.id.ivException);
            ivInstalled = itemView.findViewById(R.id.ivInstalled);
            tvUsed = itemView.findViewById(R.id.tvUsed);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            cbAssigned = itemView.findViewById(R.id.cbAssigned);
        }

        private void wire() {
            ivException.setOnClickListener(this);
            tvGroup.setOnClickListener(this);
            cbAssigned.setOnCheckedChangeListener(this);
        }

        private void unWire() {
            ivException.setOnClickListener(null);
            tvGroup.setOnClickListener(null);
            cbAssigned.setOnCheckedChangeListener(null);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            LuaHooksGroup group = groups.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.ivException:
                    StringBuilder sb = new StringBuilder();
                    for (AssignmentPacket assignment : app.getAssignments(group.name))
                        if (assignment.hookObj.getGroup().equals(group.name))
                            if (assignment.exception != null) {
                                sb.append("<b>");
                                sb.append(Html.escapeHtml(assignment.hookObj.getId()));
                                sb.append("</b><br><br>");
                                for (String line : assignment.exception.split("\n")) {
                                    sb.append(Html.escapeHtml(line));
                                    sb.append("<br>");
                                }
                                sb.append("<br><br>");
                            }

                    LayoutInflater inflater = LayoutInflater.from(view.getContext());
                    View alert = inflater.inflate(R.layout.exception, null, false);
                    TextView tvException = alert.findViewById(R.id.tvException);
                    tvException.setText(Html.fromHtml(sb.toString()));

                    new AlertDialog.Builder(view.getContext())
                            .setView(alert)
                            .create()
                            .show();
                    break;

                case R.id.tvGroup:
                    cbAssigned.setChecked(!cbAssigned.isChecked());//Invoke the onCheck
                    break;

                    //if they click on the Name then pop up
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            final LuaHooksGroup group = groups.get(getAdapterPosition());
            switch (compoundButton.getId()) {
                case R.id.cbAssigned:
                    if(group.hasWarning && checked) {
                        String wMsg = HookWarnings.getWarningMessage(compoundButton.getContext(), group.name);
                        if(wMsg != null)
                            new HookWarningDialog()
                                    .setGroup(group)
                                    .setText(wMsg)
                                    .show(fragmentLoader.getManager(), compoundButton.getContext().getString(R.string.title_hook_warning));
                    }

                    app.setAssigned(compoundButton.getContext(), group.name, checked);
                    break;
            }
        }
    }

    AdapterGroup() {
        setHasStableIds(true);
    }
    AdapterGroup(ILoader loader) { this(); this.fragmentLoader = loader; }

    @SuppressLint("NotifyDataSetChanged")
    void set(AppXpPacket app, List<XLuaHook> hooks, Context context) {
        this.app = app;

        Map<String, LuaHooksGroup> map = new HashMap<>();
        for (XLuaHook hook : hooks) {
            LuaHooksGroup group = map.get(hook.getGroup());
            if(group == null) {
                group = new LuaHooksGroup();
                map.put(hook.getGroup(), group);

                Resources resources = context.getResources();
                String name = hook.getGroup().toLowerCase().replaceAll("[^a-z]", "_");
                group.id = resources.getIdentifier("group_" + name, "string", context.getPackageName());
                group.name = hook.getGroup();
                group.title = (group.id > 0 ? resources.getString(group.id) : hook.getGroup());
                group.groupId = GroupHelper.getGroupId(group.name);
                group.hasWarning = HookWarnings.hasWarning(context, group.name);
            }

            group.hooks.add(hook);
        }

        for (String groupId : map.keySet()) {
            for (AssignmentPacket assignment : app.assignments)
                if (assignment.hookObj.getGroup().equals(groupId)) {
                    LuaHooksGroup group = map.get(groupId);
                    if(group == null)
                        continue;

                    if (assignment.exception != null)
                        group.exception = true;
                    if (assignment.installed >= 0)
                        group.installed++;
                    if (assignment.hookObj.isOptional())
                        group.optional++;
                    if (assignment.restricted)
                        group.used = Math.max(group.used, assignment.used);

                    group.assigned++;
                }
        }

        this.groups = new ArrayList<>(map.values());

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
        Collections.sort(this.groups, (group1, group2) -> collator.compare(group1.groupId, group2.groupId));

        notifyDataSetChanged();//Invoke to Update the UI
    }

    @Override
    public long getItemId(int position) { return groups.get(position).id; }

    @Override
    public int getItemCount() { return groups.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group, parent, false)); }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.unWire();
        LuaHooksGroup group = groups.get(position);

        Context context = holder.itemView.getContext();
        Resources resources = holder.itemView.getContext().getResources();

        holder.ivException.setVisibility(group.hasException() ? View.VISIBLE : View.GONE);
        holder.ivInstalled.setVisibility(group.hasInstalled() ? View.VISIBLE : View.GONE);
        holder.ivInstalled.setAlpha(group.allInstalled() ? 1.0f : 0.5f);
        holder.tvUsed.setVisibility(group.lastUsed() < 0 ? View.GONE : View.VISIBLE);
        holder.tvUsed.setText(DateUtils.formatDateTime(context, group.lastUsed(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL));

        String cleanName = SettingUtil.cleanSettingName(group.title);

        holder.tvGroup.setText(cleanName);
        holder.cbAssigned.setChecked(group.hasAssigned());
        holder.cbAssigned.setButtonTintList(ColorStateList.valueOf(resources.getColor(
                group.allAssigned() ? R.color.colorAccent : android.R.color.darker_gray, null)));

        holder.wire();
    }
}
