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
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.settings.LuaSettingPacket;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.assignment.LuaAssignmentPacket;

import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.ui.GroupHelper;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.utilities.ViewUtil;
import eu.faircode.xlua.x.ui.activities.SettingsExActivity;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.commands.call.AssignHooksCommand;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;
import eu.faircode.xlua.x.xlua.hook.AssignmentsPacket;
import eu.faircode.xlua.x.xlua.hook.IAssignListener;
//import eu.faircode.xlua.GlideApp;

public class AdapterApp extends RecyclerView.Adapter<AdapterApp.ViewHolder> implements Filterable {
    private static final String TAG = "XLua.App";

    private int iconSize;

    public enum enumShow {none, user, icon, all}

    private ILoader fragmentLoader;

    private enumShow show = enumShow.icon;
    private String group = null;
    private CharSequence query = null;
    private List<String> collection = new ArrayList<>();
    private boolean dataChanged = false;
    private List<XLuaHook> hooks = new ArrayList<>();
    private final List<AppXpPacket> all = new ArrayList<>();
    private List<AppXpPacket> filtered = new ArrayList<>();
    private final Map<String, Boolean> expanded = new HashMap<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public class ViewHolder extends RecyclerView.ViewHolder
            implements
            View.OnClickListener,
            View.OnLongClickListener,
            CompoundButton.OnCheckedChangeListener,
            IAssignListener {

        final View itemView;
        final ImageView ivExpander;
        final ImageView ivIcon;
        final TextView tvLabel;
        final TextView tvUid;
        final TextView tvPackage;
        final ImageView ivPersistent;
        final ImageView ivSettings;
        final TextView tvAndroid;
        final AppCompatCheckBox cbAssigned;
        final AppCompatCheckBox cbForceStop;
        final RecyclerView rvGroup;
        final Group grpExpanded;

        //final ImageView ivHooks, ivConfigs, ivSettingEx, ivProperties;
        final ImageView ivProfile;

        final AdapterGroup adapter;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            ivExpander = itemView.findViewById(R.id.ivExpander);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvUid = itemView.findViewById(R.id.tvUid);
            tvPackage = itemView.findViewById(R.id.tvPackage);
            ivPersistent = itemView.findViewById(R.id.ivPersistent);
            ivSettings = itemView.findViewById(R.id.ivSettings);
            tvAndroid = itemView.findViewById(R.id.tvAndroid);
            cbAssigned = itemView.findViewById(R.id.cbAssigned);
            cbForceStop = itemView.findViewById(R.id.cbForceStop);

            //ivHooks = itemView.findViewById(R.id.ivGroupHooks);
            //ivConfigs = itemView.findViewById(R.id.ivConfigsButton);
            //ivSettingEx = itemView.findViewById(R.id.ivSettingsExButton);
            //ivProperties = itemView.findViewById(R.id.ivPropertiesButton);

            ivProfile = itemView.findViewById(R.id.ivGroupProfile);

            rvGroup = itemView.findViewById(R.id.rvGroup);
            rvGroup.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(itemView.getContext());
            llm.setAutoMeasureEnabled(true);
            rvGroup.setLayoutManager(llm);
            adapter = new AdapterGroup(fragmentLoader);
            rvGroup.setAdapter(adapter);
            rvGroup.addItemDecoration(GroupHelper.createGroupDivider(itemView.getContext()));
            grpExpanded = itemView.findViewById(R.id.grpExpanded);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            ivSettings.setOnClickListener(this);
            ivSettings.setOnLongClickListener(this);
            cbAssigned.setOnCheckedChangeListener(this);
            cbForceStop.setOnCheckedChangeListener(this);
            //ivHooks.setOnClickListener(this);
            //ivHooks.setOnLongClickListener(this);
            //ivConfigs.setOnClickListener(this);
            //ivConfigs.setOnLongClickListener(this);
            //ivSettingEx.setOnClickListener(this);
            //ivSettingEx.setOnLongClickListener(this);
            //ivProperties.setOnClickListener(this);
            //ivProperties.setOnLongClickListener(this);
            ivProfile.setOnClickListener(this);
            ivProfile.setOnLongClickListener(this);
        }

        private void unWire() {
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);
            ivSettings.setOnClickListener(null);
            ivSettings.setOnLongClickListener(null);
            cbAssigned.setOnCheckedChangeListener(null);
            cbForceStop.setOnCheckedChangeListener(null);
            //ivHooks.setOnClickListener(null);
            //ivHooks.setOnLongClickListener(null);
            //ivConfigs.setOnClickListener(null);
            //ivConfigs.setOnLongClickListener(null);
            //ivSettingEx.setOnClickListener(null);
            //ivSettingEx.setOnLongClickListener(null);
            //ivProperties.setOnClickListener(null);
            //ivProperties.setOnLongClickListener(null);
            ivProfile.setOnClickListener(null);
            ivProfile.setOnLongClickListener(null);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            try {
                AppXpPacket app = filtered.get(getAdapterPosition());
                String pkgName = app.packageName;
                switch (view.getId()) {
                    case R.id.itemView:
                        ViewUtil.internalUpdateExpanded(expanded, pkgName);
                        updateExpand();
                        break;
                    /*case R.id.ivGroupHooks:
                        Intent settingIntent = new Intent(view.getContext(), ActivityAppControl.class);
                        settingIntent.putExtra("packageName", app.packageName);
                        view.getContext().startActivity(settingIntent);
                        break;
                    case R.id.ivConfigsButton:
                        Intent configIntent = new Intent(view.getContext(), ActivityConfig.class);
                        configIntent.putExtra("packageName", app.packageName);
                        view.getContext().startActivity(configIntent);
                        break;
                    case R.id.ivSettingsExButton:

                        //public static String FIELD_ICON = "icon";
                        //public static String FIELD_USER_ID = "userId";
                        //public static String FIELD_APP_UID = "appUid";
                        //public static String FIELD_APP_NAME = "appName";
                        //public static String FIELD_APP_PACKAGE_NAME = "appPackageName";


                        //Intent settingExIntent = new Intent(view.getContext(), ActivitySettings.class);
                        //settingExIntent.putExtra("packageName", app.getPackageName());
                        //view.getContext().startActivity(settingExIntent);

                        Intent settingsExIntent = new Intent(view.getContext(), SettingsExActivity.class);
                        settingsExIntent.putExtra(UserClientAppContext.USER_CONTEXT_ARG, UserClientAppContext.create(app).toBundle());
                        view.getContext().startActivity(settingsExIntent);
                        break;
                    case R.id.ivPropertiesButton:
                        Intent propsIntent = new Intent(view.getContext(), ActivityProperties.class);
                        propsIntent.putExtra("packageName", app.packageName);
                        view.getContext().startActivity(propsIntent);
                        break;*/
                    case R.id.ivSettings:
                        Intent settingsExIntent = new Intent(view.getContext(), SettingsExActivity.class);
                        settingsExIntent.putExtra(UserClientAppContext.USER_CONTEXT_ARG, UserClientAppContext.create(app).toBundle());
                        view.getContext().startActivity(settingsExIntent);
                        /*PackageManager pm = view.getContext().getPackageManager();
                        Intent settings = pm.getLaunchIntentForPackage(XUtil.PRO_PACKAGE_NAME);
                        if (settings == null) {
                            Intent browse = new Intent(Intent.ACTION_VIEW);
                            browse.setData(Uri.parse("https://lua.xprivacy.eu/pro/"));
                            if (browse.resolveActivity(pm) == null)
                                Snackbar.make(view, view.getContext().getString(R.string.msg_no_browser), Snackbar.LENGTH_LONG).show();
                            else
                                view.getContext().startActivity(browse);
                        } else {
                            settings.putExtra("packageName", pkgName);
                            view.getContext().startActivity(settings);
                        }*/
                        break;
                }
            }catch (Exception e) {
                XLog.e("Error with AdapterApp onClick", e);
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onLongClick(View view) {
            try {
                AppXpPacket app = filtered.get(getAdapterPosition());
                int id = view.getId();
                Log.i(TAG, "onLongClick=" + id + " full=" + view);
                switch (id) {
                    /*case R.id.ivGroupHooks:
                        Toast.makeText(view.getContext(), R.string.button_hooks_group_hint, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.ivConfigsButton:
                        Toast.makeText(view.getContext(), R.string.button_configs_hint, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.ivSettingsExButton:
                        Toast.makeText(view.getContext(), R.string.button_settings_ex_hint, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.ivPropertiesButton:
                        Toast.makeText(view.getContext(), R.string.button_props_hint, Toast.LENGTH_SHORT).show();
                        break;*/
                    default:
                        Intent launch = view.getContext().getPackageManager().getLaunchIntentForPackage(app.packageName);
                        if (launch != null) view.getContext().startActivity(launch);
                        else Toast.makeText(view.getContext(), R.string.error_no_activity, Toast.LENGTH_SHORT).show();
                        break;
                }

                return true;
            }catch (Exception e) {
                XLog.e("Error with AdapterApp onLongClick", e);
                return false;
            }
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, boolean checked) {
            try {
                Log.i(TAG, "Check changed");
                final AppXpPacket app = filtered.get(getAdapterPosition());
                switch (compoundButton.getId()) {
                    case R.id.cbAssigned:
                        updateAssignments(compoundButton.getContext(), app, group, checked);
                        notifyItemChanged(getAdapterPosition());
                        break;
                    case R.id.cbForceStop:
                        app.forceStop = checked;

                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                LuaSettingPacket packet = LuaSettingPacket.create("forcestop", Boolean.toString(app.forceStop));
                                packet.setCategory(app.packageName);
                                final XResult ret = XLuaCall.sendSetting(compoundButton.getContext(), packet);

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void run() {
                                        Toast.makeText(compoundButton.getContext(), ret.getResultMessage(), Toast.LENGTH_SHORT).show();
                                        //notifyDataSetChanged();
                                    }
                                });


                            }
                        });

                        break;
                }
            }catch (Exception e) {
                XLog.e("Failed to update Check State of Application Hook Group! ", e);
            }
        }

        @Override
        public void setAssigned(Context context, String groupName, boolean assign) {
            Log.i(TAG, "Group changed: " + groupName);

            AppXpPacket app = filtered.get(getAdapterPosition());
            updateAssignments(context, app, groupName, assign);
            notifyItemChanged(getAdapterPosition());
        }

        private void updateAssignments(final Context context, final AppXpPacket app, String groupName, final boolean assign) {
            final String pkgName = app.packageName;
            Log.i(TAG, pkgName + " " + groupName + "=" + assign);
            final ArrayList<String> hookIds = new ArrayList<>();
            for (XLuaHook hook : hooks) {
                if (hook.isAvailable(pkgName, collection) && (groupName == null || groupName.equals(hook.getGroup()))) {
                    hookIds.add(hook.getSharedId());
                    if(assign)
                        app.addAssignment(AssignmentPacket.create(hook));
                    else
                        app.removeAssignment(AssignmentPacket.create(hook));
                }
            }

            executor.submit(() -> AssignHooksCommand.call(context, AssignmentsPacket.create(app.uid, app.packageName, hookIds, !assign, app.forceStop)));
        }

        void updateExpand() {
            AppXpPacket app = filtered.get(getAdapterPosition());
            boolean isExpanded = (group == null && expanded.containsKey(app.packageName) && Boolean.TRUE.equals(expanded.get(app.packageName)));
            ivExpander.setImageLevel(isExpanded ? 1 : 0);
            ivExpander.setVisibility(group == null ? View.VISIBLE : View.INVISIBLE);
            grpExpanded.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }
    }

    AdapterApp(Context context, ILoader loader) { this(context); this.fragmentLoader = loader; }
    AdapterApp(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, typedValue, true);
        int height = TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
        iconSize = Math.round(height * context.getResources().getDisplayMetrics().density + 0.5f);
        setHasStableIds(true);
    }

    void set(List<String> collection, List<XLuaHook> hooks, List<AppXpPacket> apps) {
        this.dataChanged = (this.hooks.size() != hooks.size());
        for (int i = 0; i < this.hooks.size() && !this.dataChanged; i++) {
            XLuaHook hook = this.hooks.get(i);
            XLuaHook other = hooks.get(i);
            if(hook == null || other == null || hook.getSharedId() == null || other.getSharedId() == null) {
                Log.e(TAG, "Invalid Hook! index=" + i + " set function for adapter ");
                continue;
            }

            if(BuildConfig.DEBUG)
                Log.i(TAG, "hook1=" + hook + "   hook2=" + other);

            if (!hook.getGroup().equals(other.getGroup()) || !hook.getSharedId().equals(other.getSharedId()))
                this.dataChanged = true;
        }

        Log.i(TAG, "Set collections=" + collection.size() +
                " hooks=" + hooks.size() +
                " apps=" + apps.size() +
                " changed=" + this.dataChanged);

        this.collection = collection;
        this.hooks = hooks;

        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(apps, (app1, app2) -> collator.compare(app1.label, app2.label));

        all.clear();
        all.addAll(apps);
        getFilter().filter(query);
    }

    void setShow(enumShow value) {
        //What kind of apps to show (system, only with icon etc ... )
        //The button at the Top with 3 lines
        if (show != value) {
            show = value;
            getFilter().filter(query);
        }
    }

    void setGroup(String name) {
        if (!Objects.equals(group, name)) {
            group = name;
            this.dataChanged = true;
            getFilter().filter(query);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    void restrict(final Context context) {
        final List<LuaAssignmentPacket> actions = new ArrayList<>();
        boolean revert = false;
        for (AppXpPacket app : filtered) {
            for (XLuaHook hook : hooks) {
                if (group == null || group.equals(hook.getGroup())) {
                    AssignmentPacket assignment = new AssignmentPacket(hook);
                    if (app.hasAssignment(assignment)) {
                        revert = true;
                        break;
                    }
                }
            }
        }

        Log.i(TAG, "revert=" + revert);

        for (AppXpPacket app : filtered) {
            ArrayList<String> hookIds = new ArrayList<>();

            for (XLuaHook hook : hooks)
                if (hook.isAvailable(app.packageName, this.collection) &&
                        (group == null || group.equals(hook.getGroup()))) {
                    AssignmentPacket assignment = new AssignmentPacket(hook);
                    if (revert) {
                        if (app.hasAssignment(assignment)) {
                            hookIds.add(hook.getSharedId());
                            app.removeAssignment(assignment);
                        }
                    } else {
                        if (!app.hasAssignment(assignment)) {
                            hookIds.add(hook.getSharedId());
                            app.addAssignment(assignment);
                        }
                    }
                }

            if (!hookIds.isEmpty()) {
                Log.i(TAG, "Applying " + group + "=" + hookIds.size() + "=" + revert + " package=" + app.packageName);
                LuaAssignmentPacket packet = new LuaAssignmentPacket();
                packet.setHookIds(hookIds);
                packet.setCategory(app.packageName);
                packet.setUser(app.uid);
                packet.setIsDelete(revert);
                packet.setKill(app.forceStop);
                actions.add(packet);
            }
        }

        notifyDataSetChanged();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                for (LuaAssignmentPacket packet : actions)
                    XLuaCall.assignHooks(context, packet);
            }
        });
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            private boolean expanded1 = false;

            @Override
            protected FilterResults performFiltering(CharSequence query) {
                AdapterApp.this.query = query;

                List<AppXpPacket> visible = new ArrayList<>();
                if (show == enumShow.all || !TextUtils.isEmpty(query))
                    visible.addAll(all);
                else
                    for (AppXpPacket app : all)
                        if (app.uid > Process.FIRST_APPLICATION_UID && app.enabled &&
                                (show == enumShow.icon ? app.icon > 0 : !app.system))
                            visible.add(app);

                List<AppXpPacket> results = new ArrayList<>();

                if (TextUtils.isEmpty(query))
                    results.addAll(visible);
                else {
                    String q = query.toString().toLowerCase().trim();

                    boolean restricted = false;
                    boolean unrestricted = false;
                    boolean system = false;
                    boolean user = false;

                    while (true) {
                        if (q.startsWith("!")) {
                            restricted = true;
                            q = q.substring(1);
                            continue;
                        } else if (q.startsWith("?")) {
                            unrestricted = true;
                            q = q.substring(1);
                            continue;
                        } else if (q.startsWith("#")) {
                            system = true;
                            q = q.substring(1);
                            continue;
                        } else if (q.startsWith("@")) {
                            user = true;
                            q = q.substring(1);
                            continue;
                        }
                        break;
                    }

                    int uid;
                    try {
                        uid = Integer.parseInt(q);
                    } catch (NumberFormatException ignore) {
                        uid = -1;
                    }

                    for (AppXpPacket app : visible) {
                        if (restricted || unrestricted) {
                            int assignments = app.getAssignments(group).size();
                            if (restricted && assignments == 0)
                                continue;
                            if (unrestricted && assignments > 0)
                                continue;
                        }
                        if (system && !app.system)
                            continue;
                        if (user && app.system)
                            continue;

                        if (app.uid == uid ||
                                app.packageName.toLowerCase().contains(q) ||
                                (app.label != null && app.label.toLowerCase().contains(q)))
                            results.add(app);
                    }
                }

                if (results.size() == 1) {
                    String packageName = results.get(0).packageName;
                    if (!expanded.containsKey(packageName)) {
                        expanded1 = true;
                        expanded.put(packageName, true);
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = results;
                filterResults.count = results.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence query, FilterResults result) {
                final List<AppXpPacket> apps = (result.values == null
                        ? new ArrayList<AppXpPacket>()
                        : (List<AppXpPacket>) result.values);
                Log.i(TAG, "Filtered apps count=" + apps.size());

                if (dataChanged) {
                    dataChanged = false;
                    filtered = apps;
                    notifyDataSetChanged();
                } else {
                    DiffUtil.DiffResult diff =
                            DiffUtil.calculateDiff(new AppDiffCallback(expanded1, filtered, apps));
                    filtered = apps;
                    diff.dispatchUpdatesTo(AdapterApp.this);
                }
            }
        };
    }

    private class AppDiffCallback extends DiffUtil.Callback {
        private final boolean refresh;
        private final List<AppXpPacket> prev;
        private final List<AppXpPacket> next;

        AppDiffCallback(boolean refresh, List<AppXpPacket> prev, List<AppXpPacket> next) {
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
            AppXpPacket app1 = prev.get(oldItemPosition);
            AppXpPacket app2 = next.get(newItemPosition);
            return (!refresh && app1.packageName.equals(app2.packageName) && Objects.equals(app1.uid, app2.uid));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            AppXpPacket app1 = prev.get(oldItemPosition);
            AppXpPacket app2 = next.get(newItemPosition);

            if (!Objects.equals(app1.icon, app2.icon) ||
                    !app1.label.equals(app2.label) ||
                    app1.enabled != app2.enabled ||
                    app1.persistent != app2.persistent ||
                    app1.getAssignments(group).size() != app2.getAssignments(group).size())
                return false;

            for (AssignmentPacket a1 : app1.getAssignments(group)) {
                int i2 = app2.assignmentIndex(a1);
                if (i2 < 0) return false;

                AssignmentPacket a2 = app2.assignmentAt(i2);
                if (a1.installed != a2.installed ||
                        a1.used != a2.used ||
                        a1.restricted != a2.restricted)
                    return false;
            }

            return true;
        }
    }

    @Override
    public long getItemId(int position) {
        AppXpPacket assignment = filtered.get(position);
        return ((long) assignment.packageName.hashCode()) << 32 | assignment.uid;
    }

    @Override
    public int getItemCount() { return filtered.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app, parent, false)); }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.unWire();
        AppXpPacket app = filtered.get(position);
        app.setListener(holder);

        Resources resources = holder.itemView.getContext().getResources();

        // App icon
        if (app.icon <= 0)
            holder.ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
        else
            UserClientAppContext.attachIcon(holder.itemView.getContext(), iconSize, holder.ivIcon, app.packageName, app.icon);


        // App info
        holder.itemView.setBackgroundColor(app.system
                ? XUtil.resolveColor(holder.itemView.getContext(), R.attr.colorSystem)
                : resources.getColor(android.R.color.transparent, null));

        holder.tvLabel.setText(app.label);
        holder.tvUid.setText(Integer.toString(app.uid));
        holder.tvPackage.setText(app.packageName);
        holder.ivPersistent.setVisibility(app.persistent ? View.VISIBLE : View.GONE);

        List<XLuaHook> selectedHooks = new ArrayList<>();
        for (XLuaHook hook : hooks)
            if (hook.isAvailable(app.packageName, collection) &&
                    (group == null || group.equals(hook.getGroup())))
                selectedHooks.add(hook);

        // Assignment info
        holder.cbAssigned.setChecked(!app.getAssignments(group).isEmpty());
        holder.cbAssigned.setButtonTintList(ColorStateList.valueOf(resources.getColor(
                !selectedHooks.isEmpty() && app.getAssignments(group).size() == selectedHooks.size()
                        ? R.color.colorAccent
                        : android.R.color.darker_gray, null)));

        holder.tvAndroid.setVisibility("android".equals(app.packageName) ? View.VISIBLE : View.GONE);
        holder.cbForceStop.setChecked(app.forceStop);
        holder.cbForceStop.setEnabled(!app.persistent);
        holder.adapter.set(app, selectedHooks, holder.itemView.getContext());
        holder.updateExpand();
        holder.wire();
    }
}
