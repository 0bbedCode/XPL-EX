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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.api.xlua.XLuaQuery;
import eu.faircode.xlua.api.hook.HookDatabaseEntry;
import eu.faircode.xlua.api.hook.LuaHookPacket;
import eu.faircode.xlua.api.xlua.call.PutHookCommand;
import eu.faircode.xlua.ui.dialogs.PrivacyGroupWarningDialog;
import eu.faircode.xlua.ui.interfaces.ILoader;
import eu.faircode.xlua.utilities.CollectionUtil;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.app.XLuaApp;
import eu.faircode.xlua.utilities.PrefUtil;
import eu.faircode.xlua.utilities.UiUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.PrefManager;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.GetGroupsCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAppsCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.hook.AppXpPacket;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;


public class FragmentMain extends Fragment implements ILoader {
    private final static String TAG = LibUtil.generateTag(FragmentMain.class);

    private ProgressBar pbApplication;
    private Spinner spGroup;
    private ArrayAdapter<XUiGroup> spAdapter;
    private Button btnRestrict;
    private TextView tvRestrict;
    private Group grpApplication;
    private SwipeRefreshLayout swipeRefresh;
    private AdapterApp rvAdapter;

    //Filter what apps to show not needed for us
    private AdapterApp.enumShow show = AdapterApp.enumShow.none;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View main = inflater.inflate(R.layout.restrictions, container, false);

        ActivityMain.manager.ensureIsOpen(requireContext(), PrefManager.SETTINGS_MAIN);
        show = PrefManager.settingToShow(ActivityMain.manager.getString(PrefManager.SETTING_APPS_SHOW, "show_user", true));


        pbApplication = main.findViewById(R.id.pbApplication);
        btnRestrict = main.findViewById(R.id.btnRestrict);
        tvRestrict = main.findViewById(R.id.tvRestrict);
        grpApplication = main.findViewById(R.id.grpApplication);

        int colorAccent = XUtil.resolveColor(requireContext(), R.attr.colorAccent);

        swipeRefresh = main.findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeColors(colorAccent, colorAccent, colorAccent);

        //RIP
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        // Initialize app list
        RecyclerView rvApplication = main.findViewById(R.id.rvApplication);
        //new FastScrollerBuilder(rvApplication).useMd2Style().build();

        rvApplication.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull RecyclerView.State state, @NonNull View child, View focused) {
                return true;
            }
        };
        llm.setAutoMeasureEnabled(true);
        rvApplication.setLayoutManager(llm);
        rvAdapter = new AdapterApp(getActivity(), this);
        rvApplication.setAdapter(rvAdapter);

        spAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spGroup = main.findViewById(R.id.spGroup);
        spGroup.setTag(null);
        spGroup.setAdapter(spAdapter);
        spGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateSelection(); }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                updateSelection();
            }

            private void updateSelection() {
                XUiGroup selected = (XUiGroup) spGroup.getSelectedItem();
                String group = (selected == null ? null : selected.name);
                if (group == null ? spGroup.getTag() != null : !group.equals(spGroup.getTag())) {
                    spGroup.setTag(group);
                    rvAdapter.setGroup(group);
                    if(DebugUtil.isDebug()) Log.i(TAG, "Select group=" + group);
                }

                tvRestrict.setVisibility(group == null ? View.VISIBLE : View.GONE);
                btnRestrict.setVisibility(group == null ? View.INVISIBLE : View.VISIBLE);
            }
        });

        btnRestrict.setOnClickListener(view -> {
            XUiGroup selected = (XUiGroup) spGroup.getSelectedItem();
            XUtil.areYouSure(
                    (ActivityBase) getActivity(),
                    getString(R.string.msg_restrict_sure, selected.title),
                    () -> rvAdapter.restrict(getContext()));
        });

        return main;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter ifPackage = new IntentFilter();
        ifPackage.addAction(Intent.ACTION_PACKAGE_ADDED);
        ifPackage.addAction(Intent.ACTION_PACKAGE_CHANGED);
        ifPackage.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        ifPackage.addDataScheme("package");
        Objects.requireNonNull(getContext()).registerReceiver(packageChangedReceiver, ifPackage);

        ActivityMain.manager.ensureIsOpen(requireContext(), PrefManager.SETTINGS_MAIN);
        show = PrefManager.settingToShow(ActivityMain.manager.getString(PrefManager.SETTING_APPS_SHOW, "show_user", true));
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getContext()).unregisterReceiver(packageChangedReceiver);
    }

    public AdapterApp.enumShow getShow() {
        return this.show;
    }

    public void setShow(AdapterApp.enumShow value) {
        this.show = value;
        if (rvAdapter != null)
            rvAdapter.setShow(value);
    }

    public void filter(String query) {
        if (rvAdapter != null) rvAdapter.getFilter().filter(query);
    }

    @Override
    public FragmentManager getManager() { return getFragmentManager(); }

    @Override
    public Fragment getFragment() { return this; }

    @Override
    public AppGeneric getApplication() { return null; }

    @Override
    public void loadData() {
        LoaderManager manager = getActivity().getSupportLoaderManager();
        manager.restartLoader(ActivityMain.LOADER_DATA, new Bundle(), dataLoaderCallbacks).forceLoad();
    }

    LoaderManager.LoaderCallbacks<DataHolder> dataLoaderCallbacks = new LoaderManager.LoaderCallbacks<DataHolder>() {
        @NonNull
        @Override
        public Loader<DataHolder> onCreateLoader(int id, Bundle args) {
            return new DataLoader(getContext());
        }

        @Override
        public void onLoadFinished(@NonNull Loader<DataHolder> loader, DataHolder data) {
            if (data.exception == null) {
                UiUtil.initTheme(getActivity(), data.theme);
                spAdapter.clear();
                spAdapter.addAll(data.groups);

                show = data.show;
                rvAdapter.setShow(data.show);

                ListUtil.filterCondition(data.hooks,
                        (o) -> !Str.isEmpty(o.getGroup())
                                && !o.getGroup().toLowerCase().startsWith("intercept."));

                rvAdapter.set(data.collection, data.hooks, data.apps);

                swipeRefresh.setRefreshing(false);
                pbApplication.setVisibility(View.GONE);
                grpApplication.setVisibility(View.VISIBLE);

                //This will determine if its all or a actual name
                //If a actual group is selected then show the restrict button
                XUiGroup selected = (XUiGroup) spGroup.getSelectedItem();
                String group = (selected == null ? null : selected.name);
                tvRestrict.setVisibility(group == null ? View.VISIBLE : View.GONE);
                btnRestrict.setVisibility(group == null ? View.INVISIBLE : View.VISIBLE);
            } else {
                Log.e(TAG, Log.getStackTraceString(data.exception));
                Snackbar.make(Objects.requireNonNull(getView()), data.exception.toString(), Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<DataHolder> loader) { }
    };

    private static class DataLoader extends AsyncTaskLoader<DataHolder> {
        DataLoader(Context context) {
            super(context);
            setUpdateThrottle(1000);
        }

        @Nullable
        @Override
        public DataHolder loadInBackground() {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Data Loader Started!");

            DataHolder data = new DataHolder();
            try {
                data.theme = GetSettingExCommand.getTheme(getContext(), Process.myUid());

                // Define hooks
                /*if (BuildConfig.DEBUG) {
                    String apk = getContext().getApplicationInfo().publicSourceDir;
                    List<XLuaHook> hooks = XLuaHook.readHooks(getContext(), apk);
                    //Have one but that just takes in HookPacket / HookDatabase Entry
                    Log.i(TAG, "Loaded hooks=" + hooks.size());
                    for (XLuaHook hook : hooks) {
                        HookDatabaseEntry entry = hook.toHookDatabase();
                        if(entry == null)
                            continue;

                        PutHookCommand.invoke(getContext(), (LuaHookPacket)entry);
                    }
                }*/

                //Get Show
                //String show = GetSettingExCommand.getShow(getContext(), Process.myUid());
                List<String> collections = GetSettingExCommand.getCollections(getContext(), Process.myUid());

                ActivityMain.manager.ensureIsOpen(getContext(), PrefManager.SETTINGS_MAIN);
                //data.show = PrefManager.getShow(getContext());
                data.show = PrefManager.settingToShow(ActivityMain.manager.getString(PrefManager.SETTING_APPS_SHOW, "show_user", true));

                if(DebugUtil.isDebug())
                    Log.d(TAG, "[SHOW_SHOW] Data Loader Got Show, Show=" + data.show.name() + " Collections=" + Str.joinList(collections));


                /*if (show != null && show.equals("user"))
                    data.show = AdapterApp.enumShow.user;
                else if (show != null && show.equals("all"))
                    data.show = AdapterApp.enumShow.all;
                else
                    data.show = AdapterApp.enumShow.icon;*/

                // Get collection
                data.collection.addAll(collections);

                if(DebugUtil.isDebug())
                    Log.d(TAG, "Getting Groups...");

                // Load groups
                Resources res = getContext().getResources();
                List<String> groupsCopy = GetGroupsCommand.get(getContext());
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Got Groups, Size=" + ListUtil.size(groupsCopy));

                if(ListUtil.isValid(groupsCopy)) {
                    for(String name : groupsCopy) {
                        String g = name.toLowerCase().replaceAll("[^a-z]", "_");
                        int id = res.getIdentifier("group_" + g, "string", getContext().getPackageName());

                        XUiGroup group = new XUiGroup();
                        group.name = name;
                        group.title = (id > 0 ? res.getString(id) : name);
                        data.groups.add(group);
                    }
                }

                final Collator collator = Collator.getInstance(Locale.getDefault());
                collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc
                Collections.sort(data.groups, (group1, group2) -> collator.compare(group1.title, group2.title));

                XUiGroup all = new XUiGroup();
                all.name = null;
                all.title = getContext().getString(R.string.title_all);
                data.groups.add(0, all);

                List<XLuaHook> hooksCopy = GetHooksCommand.getHooks(getContext(), true, false);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Hooks loaded=" + ListUtil.size(hooksCopy));

                data.hooks.addAll(hooksCopy);

                // Load apps
                List<AppXpPacket> appsCopy = GetAppsCommand.get(getContext(), true);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Apps Loaded=" + ListUtil.size(appsCopy));

                data.apps.addAll(appsCopy);
            } catch (Throwable ex) {
                data.collection = null;
                data.groups.clear();
                data.hooks.clear();
                data.apps.clear();
                data.exception = ex;
            }

            Log.i(TAG, "Data loader finished groups=" + data.groups.size() +
                    " hooks=" + data.hooks.size() + " apps=" + data.apps.size());
            return data;
        }
    }

    private final BroadcastReceiver packageChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Received Package!");

            String packageName = Objects.requireNonNull(intent.getData()).getSchemeSpecificPart();
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            if(DebugUtil.isDebug())
                Log.d(TAG, "pkg=" + packageName + ":" + uid);

            loadData();
        }
    };

    private static class DataHolder {
        AdapterApp.enumShow show;
        String theme;
        List<String> collection = new ArrayList<>();
        List<XUiGroup> groups = new ArrayList<>();
        List<XLuaHook> hooks = new ArrayList<>();
        List<AppXpPacket> apps = new ArrayList<>();
        Throwable exception = null;
    }
}
