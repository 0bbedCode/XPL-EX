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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.net.Uri;
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

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.api.standard.interfaces.IListener;
import eu.faircode.xlua.api.hook.assignment.XLuaAssignment;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.assignment.XLuaAssignmentPacket;

import eu.faircode.xlua.api.app.XLuaApp;

public class AdapterApp extends RecyclerView.Adapter<AdapterApp.ViewHolder> implements Filterable {
    private static final String TAG = "XLua.App";

    private int iconSize;

    public enum enumShow {none, user, icon, all}

    private enumShow show = enumShow.icon;
    private String group = null;
    private CharSequence query = null;
    private List<String> collection = new ArrayList<>();
    private boolean dataChanged = false;
    private List<XLuaHook> hooks = new ArrayList<>();
    private List<XLuaApp> all = new ArrayList<>();
    private List<XLuaApp> filtered = new ArrayList<>();
    private Map<String, Boolean> expanded = new HashMap<>();

    //private ArrayAdapter<XUiConfig> spAdapterPhoneConfig;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener, IListener {
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

        /*final Spinner spPhoneConfig;
        final Button btApplyPhoneConfig;


        final Spinner spUniqueConfig;
        final Button btApplyUniqueConfig;

        final Spinner spCarrierConfig;
        final Button btApplyCarrierCondifg;*/

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

            /*spPhoneConfig = itemView.findViewById(R.id.spPhoneConfig);
            btApplyPhoneConfig = itemView.findViewById(R.id.btApplyPhoneConfig);

            spUniqueConfig = itemView.findViewById(R.id.spUniqueIdConfig);
            btApplyUniqueConfig = itemView.findViewById(R.id.btApplyUniqueIdConfig);

            spCarrierConfig = itemView.findViewById(R.id.spCarrierConfig);
            btApplyCarrierCondifg = itemView.findViewById(R.id.btApplyCarrierIdConfig);*/


            //spAdapterPhoneConfig = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item);
            //spAdapterPhoneConfig.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            /*XUiConfig con1 = new XUiConfig();
            con1.name = "NONE";

            XUiConfig con2 = new XUiConfig();
            con2.name = "Cool";

            List<XUiConfig> confs = new ArrayList<>();
            confs.add(con1);
            confs.add(con2);
            spAdapterPhoneConfig.addAll(confs);*/

            /*spPhoneConfig.setTag(null);
            spPhoneConfig.setAdapter(spAdapterPhoneConfig);
            spCarrierConfig.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(TAG, "COOOL SELECTED");
                    updateSelection();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    updateSelection();
                }

                private void updateSelection() {
                    Log.i(TAG, "COOOL SELECTED2");

                    XUiConfig conf = (XUiConfig) spPhoneConfig.getSelectedItem();
                    spPhoneConfig.setTag(conf);

                    XUiGroup selected = (XUiGroup) spGroup.getSelectedItem();
                    String group = (selected == null ? null : selected.name);

                    if (group == null ? spGroup.getTag() != null : !group.equals(spGroup.getTag())) {
                        Log.i(TAG, "Select group=" + group);
                        spGroup.setTag(group);
                        rvAdapter.setGroup(group);
                    }

                    tvRestrict.setVisibility(group == null ? View.VISIBLE : View.GONE);
                    btnRestrict.setVisibility(group == null ? View.INVISIBLE : View.VISIBLE);
                }
            });*/


            rvGroup = itemView.findViewById(R.id.rvGroup);
            rvGroup.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(itemView.getContext());
            llm.setAutoMeasureEnabled(true);
            rvGroup.setLayoutManager(llm);
            adapter = new AdapterGroup();
            rvGroup.setAdapter(adapter);

            grpExpanded = itemView.findViewById(R.id.grpExpanded);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            ivSettings.setOnClickListener(this);
            cbAssigned.setOnCheckedChangeListener(this);
            cbForceStop.setOnCheckedChangeListener(this);
        }

        private void unWire() {
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);
            ivSettings.setOnClickListener(null);
            cbAssigned.setOnCheckedChangeListener(null);
            cbForceStop.setOnCheckedChangeListener(null);
        }

        @Override
        public void onClick(View view) {
            XLuaApp app = filtered.get(getAdapterPosition());
            String pkgName = app.getPackageName();
            switch (view.getId()) {
                case R.id.itemView:
                    if (!expanded.containsKey(pkgName))
                        expanded.put(pkgName, false);
                    expanded.put(pkgName, !expanded.get(pkgName));
                    updateExpand();
                    break;

                case R.id.ivSettings:
                    PackageManager pm = view.getContext().getPackageManager();
                    Intent settings = pm.getLaunchIntentForPackage(XUtil.PRO_PACKAGE_NAME);
                    if (settings == null) {
                        Intent browse = new Intent(Intent.ACTION_VIEW);
                        browse.setData(Uri.parse(https://github.com/0bbedCode/XPL-EX/releases"));
                        if (browse.resolveActivity(pm) == null)
                            Snackbar.make(view, view.getContext().getString(R.string.msg_no_browser), Snackbar.LENGTH_LONG).show();
                        else
                            view.getContext().startActivity(browse);
                    } else {
                        settings.putExtra("packageName", pkgName);
                        view.getContext().startActivity(settings);
                    }
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            XLuaApp app = filtered.get(getAdapterPosition());
            Intent launch = view.getContext().getPackageManager().getLaunchIntentForPackage(app.getPackageName());
            if (launch != null)
                view.getContext().startActivity(launch);
            return true;
        }

        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, boolean checked) {
            Log.i(TAG, "Check changed");
            final XLuaApp app = filtered.get(getAdapterPosition());

            switch (compoundButton.getId()) {
                case R.id.cbAssigned:
                    updateAssignments(compoundButton.getContext(), app, group, checked);
                    notifyItemChanged(getAdapterPosition());
                    break;

                case R.id.cbForceStop:
                    app.setForceStop(checked);
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            XLuaCall.putSettingBoolean(
                                    compoundButton.getContext(), app.getPackageName(), "forcestop", app.getForceStop());
                        }
                    });
                    break;
            }
        }

        @Override
        public void onAssign(Context context, String groupName, boolean assign) {
            Log.i(TAG, "Group changed");
            XLuaApp app = filtered.get(getAdapterPosition());
            updateAssignments(context, app, groupName, assign);
            notifyItemChanged(getAdapterPosition());
        }

        private void updateAssignments(final Context context, final XLuaApp app, String groupName, final boolean assign) {
            final String pkgName = app.getPackageName();
            Log.i(TAG, pkgName + " " + groupName + "=" + assign);


            final ArrayList<String> hookIds = new ArrayList<>();
            for (XLuaHook hook : hooks)
                if (hook.isAvailable(pkgName, collection) &&
                        (groupName == null || groupName.equals(hook.getGroup()))) {
                    hookIds.add(hook.getId());
                    if (assign)
                        app.addAssignment(new XLuaAssignment(hook));
                    else
                        app.removeAssignment(new XLuaAssignment(hook));
                }

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    XLuaCall.assignHooks(
                            context, hookIds, pkgName, app.getUid(), !assign, app.getForceStop());
                }
            });
        }

        void updateExpand() {
            XLuaApp app = filtered.get(getAdapterPosition());
            boolean isExpanded = (group == null && expanded.containsKey(app.getPackageName()) && expanded.get(app.getPackageName()));
            ivExpander.setImageLevel(isExpanded ? 1 : 0);
            ivExpander.setVisibility(group == null ? View.VISIBLE : View.INVISIBLE);
            grpExpanded.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            /*spPhoneConfig.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            btApplyPhoneConfig.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            spUniqueConfig.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            btApplyUniqueConfig.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            spCarrierConfig.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            btApplyCarrierCondifg.setVisibility(isExpanded ? View.VISIBLE : View.GONE);*/

            /*spAdapterPhoneConfig.clear();
            XUiConfig con1 = new XUiConfig();
            con1.name = "NONE";

            XUiConfig con2 = new XUiConfig();
            con2.name = "Cool";

            List<XUiConfig> confs = new ArrayList<>();
            confs.add(con1);
            confs.add(con2);
            spAdapterPhoneConfig.addAll(confs);*/
        }
    }

    AdapterApp(Context context) {
        //This is to create the icons for each app.xml item
        //Needs context
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, typedValue, true);
        int height = TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
        iconSize = Math.round(height * context.getResources().getDisplayMetrics().density + 0.5f);

        setHasStableIds(true);
    }

    /*void setConfigs(List<XUiConfig> configs) {
        if(spAdapterPhoneConfig != null) {
            spAdapterPhoneConfig.clear();
            spAdapterPhoneConfig.addAll(configs);
        }
    }*/

    void set(List<String> collection, List<XLuaHook> hooks, List<XLuaApp> apps) {
        this.dataChanged = (this.hooks.size() != hooks.size());
        for (int i = 0; i < this.hooks.size() && !this.dataChanged; i++) {
            XLuaHook hook = this.hooks.get(i);
            XLuaHook other = hooks.get(i);
            if(hook == null || other == null || hook.getId() == null || other.getId() == null) {
                Log.e(TAG, "Invalid Hook! index=" + i + " set function for adapter ");
                continue;
            }

            if(BuildConfig.DEBUG)
                Log.i(TAG, "hook1=" + hook + "   hook2=" + other);

            if (!hook.getGroup().equals(other.getGroup()) || !hook.getId().equals(other.getId()))
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

        Collections.sort(apps, new Comparator<XLuaApp>() {
            @Override
            public int compare(XLuaApp app1, XLuaApp app2) {
                return collator.compare(app1.getLabel(), app2.getLabel());
            }
        });

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
        //Set the group from the Spinner / Drop Down Menu at top of UI menu
        //That being said ALL is all but specific kinds
        //Get passed down to here to update view as well as show Restrict button
        if (group == null ? name != null : !group.equals(name)) {
            group = name;
            this.dataChanged = true;
            getFilter().filter(query);
        }
    }

    void restrict(final Context context) {
        //This is just the restrict button if im not mistaken
        //Note this button will only be visible when specific group is selected
        //final List<Bundle> actions = new ArrayList<>();
        final List<XLuaAssignmentPacket> actions = new ArrayList<>();

        boolean revert = false;
        for (XLuaApp app : filtered)
            for (XLuaHook hook : hooks)
                if (group == null || group.equals(hook.getGroup())) {
                    XLuaAssignment assignment = new XLuaAssignment(hook);
                    if (app.hasAssignment(assignment)) {
                        revert = true;
                        break;
                    }
                }
        Log.i(TAG, "revert=" + revert);

        for (XLuaApp app : filtered) {
            ArrayList<String> hookIds = new ArrayList<>();

            for (XLuaHook hook : hooks)
                if (hook.isAvailable(app.getPackageName(), this.collection) &&
                        (group == null || group.equals(hook.getGroup()))) {
                    XLuaAssignment assignment = new XLuaAssignment(hook);
                    if (revert) {
                        if (app.hasAssignment(assignment)) {
                            hookIds.add(hook.getId());
                            app.removeAssignment(assignment);
                        }
                    } else {
                        if (!app.hasAssignment(assignment)) {
                            hookIds.add(hook.getId());
                            app.addAssignment(assignment);
                        }
                    }
                }

            if (hookIds.size() > 0) {
                Log.i(TAG, "Applying " + group + "=" + hookIds.size() + "=" + revert + " package=" + app.getPackageName());
                XLuaAssignmentPacket packet = new XLuaAssignmentPacket();
                packet.hookIds = hookIds;
                packet.packageName = app.getPackageName();
                packet.uid = app.getUid();
                packet.delete = revert;
                packet.kill = app.getForceStop();
                actions.add(packet);
            }
        }

        notifyDataSetChanged();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                for (XLuaAssignmentPacket packet : actions)
                    XLuaCall.assignHooks(context, packet);
            }
        });
    }

    @Override
    public Filter getFilter() {
        //Some big filter shit
        //Dunno rn we no need filter for PROPS
        //Also Filter is a liternal class from java hmm didnt know
        return new Filter() {
            private boolean expanded1 = false;

            @Override
            protected FilterResults performFiltering(CharSequence query) {
                AdapterApp.this.query = query;

                List<XLuaApp> visible = new ArrayList<>();
                if (show == enumShow.all || !TextUtils.isEmpty(query))
                    visible.addAll(all);
                else
                    for (XLuaApp app : all)
                        if (app.getUid() > Process.FIRST_APPLICATION_UID && app.isEnabled() &&
                                (show == enumShow.icon ? app.getIcon() > 0 : !app.isSystem()))
                            visible.add(app);

                List<XLuaApp> results = new ArrayList<>();

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

                    for (XLuaApp app : visible) {
                        if (restricted || unrestricted) {
                            int assignments = app.getAssignments(group).size();
                            if (restricted && assignments == 0)
                                continue;
                            if (unrestricted && assignments > 0)
                                continue;
                        }
                        if (system && !app.isSystem())
                            continue;
                        if (user && app.isSystem())
                            continue;

                        if (app.getUid() == uid ||
                                app.getPackageName().toLowerCase().contains(q) ||
                                (app.getLabel() != null && app.getLabel().toLowerCase().contains(q)))
                            results.add(app);
                    }
                }

                if (results.size() == 1) {
                    String packageName = results.get(0).getPackageName();
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
                final List<XLuaApp> apps = (result.values == null
                        ? new ArrayList<XLuaApp>()
                        : (List<XLuaApp>) result.values);
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
        private final List<XLuaApp> prev;
        private final List<XLuaApp> next;

        AppDiffCallback(boolean refresh, List<XLuaApp> prev, List<XLuaApp> next) {
            this.refresh = refresh;
            this.prev = prev;
            this.next = next;
        }

        @Override
        public int getOldListSize() {
            return prev.size();
        }

        @Override
        public int getNewListSize() {
            return next.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            XLuaApp app1 = prev.get(oldItemPosition);
            XLuaApp app2 = next.get(newItemPosition);

            return (!refresh && app1.getPackageName().equals(app2.getPackageName()) && app1.getUid() == app2.getUid());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            XLuaApp app1 = prev.get(oldItemPosition);
            XLuaApp app2 = next.get(newItemPosition);

            if (app1.getIcon() != app2.getIcon() ||
                    !app1.getLabel().equals(app2.getLabel()) ||
                    app1.isEnabled() != app2.isEnabled() ||
                    app1.isPersistent() != app2.isPersistent() ||
                    app1.getAssignments(group).size() != app2.getAssignments(group).size())
                return false;

            for (XLuaAssignment a1 : app1.getAssignments(group)) {
                //Hmmm make sure this still works
                int i2 = app2.assignmentIndex(a1); // by hookid
                if (i2 < 0)
                    return false;

                XLuaAssignment a2 = app2.getAssignmentAt(i2);
                if (a1.getInstalled() != a2.getInstalled() ||
                        a1.getUsed() != a2.getUsed() ||
                        a1.getRestricted() != a2.getRestricted())
                    return false;
            }

            return true;
        }
    }

    @Override
    public long getItemId(int position) {
        XLuaApp assignment = filtered.get(position);
        return ((long) assignment.getPackageName().hashCode()) << 32 | assignment.getUid();
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.unWire();
        XLuaApp app = filtered.get(position);
        app.setListener(holder);

        Resources resources = holder.itemView.getContext().getResources();

        // App icon
        if (app.getIcon() <= 0)
            holder.ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
        else {
            Uri uri = Uri.parse("android.resource://" + app.getPackageName() + "/" + app.getIcon());
            GlideApp.with(holder.itemView.getContext())
                    .applyDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565))
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(iconSize, iconSize)
                    .into(holder.ivIcon);
        }

        // App info
        holder.itemView.setBackgroundColor(app.isSystem()
                ? XUtil.resolveColor(holder.itemView.getContext(), R.attr.colorSystem)
                : resources.getColor(android.R.color.transparent, null));

        holder.tvLabel.setText(app.getLabel());
        holder.tvUid.setText(Integer.toString(app.getUid()));
        holder.tvPackage.setText(app.getPackageName());
        holder.ivPersistent.setVisibility(app.isPersistent() ? View.VISIBLE : View.GONE);

        List<XLuaHook> selectedHooks = new ArrayList<>();
        for (XLuaHook hook : hooks)
            if (hook.isAvailable(app.getPackageName(), collection) &&
                    (group == null || group.equals(hook.getGroup())))
                selectedHooks.add(hook);

        // Assignment info
        holder.cbAssigned.setChecked(app.getAssignments(group).size() > 0);
        holder.cbAssigned.setButtonTintList(ColorStateList.valueOf(resources.getColor(
                selectedHooks.size() > 0 && app.getAssignments(group).size() == selectedHooks.size()
                        ? R.color.colorAccent
                        : android.R.color.darker_gray, null)));

        holder.tvAndroid.setVisibility("android".equals(app.getPackageName()) ? View.VISIBLE : View.GONE);

        holder.cbForceStop.setChecked(app.getForceStop());
        holder.cbForceStop.setEnabled(!app.isPersistent());

        holder.adapter.set(app, selectedHooks, holder.itemView.getContext());

        holder.updateExpand();

        holder.wire();
    }
}
