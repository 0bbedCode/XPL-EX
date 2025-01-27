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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.xlua.call.CleanHooksCommand;
import eu.faircode.xlua.api.xlua.provider.XLuaHookProvider;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.utilities.PrefUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.activities.SettingsExActivity;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.ui.dialogs.CollectionsDialog;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.call.PutSettingExCommand;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.root.RootManager;

public class ActivityMain extends ActivityBase {
    private final static String TAG = "XLua.Main";


    private FragmentMain fragmentMain = null;
    private DrawerLayout drawerLayout = null;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle = null;

    private Menu menu = null;

    private AlertDialog firstRunDialog = null;

    public static final int LOADER_DATA = 1;
    public static final String EXTRA_SEARCH_PACKAGE = "package";

    private ExecutorService executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if service is running
        if (!XLuaHookProvider.isAvailable(this)) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_no_service), Snackbar.LENGTH_INDEFINITE);
            final Intent intent = getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
            if (intent != null && intent.resolveActivity(getPackageManager()) != null)
                snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(intent);
                    }
                });

            snackbar.show();
            return;
        }

        // Set layout
        setContentView(R.layout.main);

        // Prepare action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Show fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentMain = new FragmentMain();
        fragmentTransaction.replace(R.id.content_frame, fragmentMain);
        fragmentTransaction.commit();

        // Get drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(XUtil.resolveColor(this, R.attr.colorDrawerScrim));

        // Create drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getString(R.string.app_name));
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        // Get drawer list
        drawerList = findViewById(R.id.drawer_list);

        // Handle drawer list click
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerItem item = (DrawerItem) parent.getAdapter().getItem(position);
                //Log.i(TAG, "Drawer selected " + item.getTitle());
                item.onClick();
                if (!item.isCheckable())
                    drawerLayout.closeDrawer(drawerList);
            }
        });

        boolean notifyNew = GetSettingExCommand.notifyOnNewApps(this);
        boolean restrictNew = GetSettingExCommand.restrictNewApps(this);
        boolean isVerbose = GetSettingExCommand.getVerboseLogs(this);
        boolean isDark = GetSettingExCommand.SETTING_THEME_DEFAULT.equalsIgnoreCase(GetSettingExCommand.getTheme(this, Process.myUid()));
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Notify New=%s Restrict New=%s IsDark=%s", notifyNew, restrictNew, isDark));

        final boolean forceEnglish = getIsForceEnglish();

        final ArrayAdapterDrawer drawerArray = new ArrayAdapterDrawer(ActivityMain.this, R.layout.draweritem);

        if (!XposedUtil.isVirtualXposed())
            drawerArray.add(new DrawerItem(this, R.string.menu_notify_new, notifyNew, new DrawerItem.IListener() {
                @Override
                public void onClick(DrawerItem item) {
                    handleCodeToSnack(PutSettingExCommand.putNotifyNewApps(ActivityMain.this, item.isChecked()), getString(R.string.result_prefix_notify) + "=" + item.isChecked());
                    drawerArray.notifyDataSetChanged();
                }
            }));

        if (!XposedUtil.isVirtualXposed())
            drawerArray.add(new DrawerItem(this, R.string.menu_restrict_new, restrictNew, new DrawerItem.IListener() {
                @Override
                public void onClick(DrawerItem item) {
                    handleCodeToSnack(PutSettingExCommand.putRestrictNewApps(ActivityMain.this, item.isChecked()), getString(R.string.result_prefix_restrict) + "=" + item.isChecked());
                    drawerArray.notifyDataSetChanged();
                }
            }));


        drawerArray.add(new DrawerItem(this, R.string.menu_readme, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/0bbedCode/XPL-EX"));
                if (browse.resolveActivity(getPackageManager()) == null)
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_no_browser), Snackbar.LENGTH_LONG).show();
                else
                    startActivity(browse);
            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_faq, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/0bbedCode/XPL-EX/blob/master/FAQ.md"));
                if (browse.resolveActivity(getPackageManager()) == null)
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_no_browser), Snackbar.LENGTH_LONG).show();
                else
                    startActivity(browse);
            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_donate, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/0bbedCode/XPL-EX/?tab=readme-ov-file#donate"));
                if (browse.resolveActivity(getPackageManager()) == null)
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_no_browser), Snackbar.LENGTH_LONG).show();
                else
                    startActivity(browse);
            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_whats_new_button, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                whatsNew();
            }
        }));


        drawerArray.add(new DrawerItem(this, R.string.menu_collections, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                new CollectionsDialog()
                        .set(ActivityMain.this)
                        .setOnDialogCloseListener(() -> { if(fragmentMain != null) fragmentMain.loadData(); })
                        .show(getSupportFragmentManager(), getString(R.string.menu_collections));
            }
        }));

        if (!XposedUtil.isVirtualXposed())
            drawerArray.add(new DrawerItem(this,R.string.menu_dark, isDark, new DrawerItem.IListener() {
                @Override
                public void onClick(DrawerItem item) {
                    String oldTheme = GetSettingExCommand.getTheme(ActivityMain.this, Process.myUid());
                    String newTheme = item.isChecked() ? "dark" : "light";

                    A_CODE code = PutSettingExCommand.putTheme(ActivityMain.this, newTheme);
                    drawerArray.notifyDataSetChanged();
                    handleCodeToSnack(code, getString(R.string.result_prefix_theme) + "=" + newTheme);

                    if(A_CODE.isSuccessful(code)) {
                        if(!oldTheme.equals(newTheme)) {
                            setTheme(GetSettingExCommand.SETTING_THEME_DEFAULT.equals(newTheme) ? R.style.AppThemeDark : R.style.AppThemeLight);
                            recreate();
                        }
                    }
                }
            }));



        drawerArray.add(new DrawerItem(this, R.string.menu_debug_logs, isVerbose, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                boolean isChecked = item.isChecked();
                DebugUtil.setForceDebug(isChecked);
                handleCodeToSnack(PutSettingExCommand.putVerboseLogging(ActivityMain.this, isChecked),  getString(R.string.result_prefix_debug) + "=" + isChecked);
                drawerArray.notifyDataSetChanged();
            }
        }));

        drawerArray.add(new DrawerItem(this, R.string.menu_force_english, forceEnglish, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                boolean oldFlag = getIsForceEnglish();
                boolean newFlag = item.isChecked();
                setForceEnglish(newFlag);
                drawerArray.notifyDataSetChanged();//fix context issues
                if(oldFlag != forceEnglish) {
                    recreate();
                }
            }
        }));


        drawerArray.add(new DrawerItem(this, R.string.menu_settings, new DrawerItem.IListener() {
            @Override
            public void onClick(DrawerItem item) {
                menuSettings();
            }
        }));


        drawerList.setAdapter(drawerArray);
        //whatsNew
        initCore();
    }


    public void handleCodeToSnack(A_CODE code, String extraIfSucceeded) {
        Snackbar.make(findViewById(android.R.id.content),
                A_CODE.isSuccessful(code) ?
                        Str.combine(getString(R.string.msg_task_finished_command), TextUtils.isEmpty(extraIfSucceeded) ? "" :  " >> " + extraIfSucceeded) :
                        Str.combine(getString(R.string.msg_task_failure), " >> " + code.name(), false) , Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerToggle != null)
            drawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "New " + intent);
        super.onNewIntent(intent);
        setIntent(intent);

        if (this.menu != null)
            updateMenu(this.menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null)
            drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(drawerList))
            drawerLayout.closeDrawer(drawerList);
        else
            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "Create options");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i(TAG, "Prepare options");
        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "Search submit=" + query);
                if (fragmentMain != null) {
                    fragmentMain.filter(query);
                    searchView.clearFocus(); // close keyboard
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "Search change=" + newText);
                if (fragmentMain != null)
                    fragmentMain.filter(newText);
                return true;
            }
        });

        menuSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                Log.i(TAG, "Search expand");
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                Log.i(TAG, "Search collapse");

                // Search uid once
                Intent intent = getIntent();
                intent.removeExtra(EXTRA_SEARCH_PACKAGE);
                setIntent(intent);

                return true;
            }
        });

        updateMenu(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item))
            return true;

        Log.i(TAG, "Selected option " + item.getTitle());
        switch (item.getItemId()) {
            case R.id.menu_show:
                AdapterApp.enumShow show = (fragmentMain == null ? AdapterApp.enumShow.none : fragmentMain.getShow());
                this.menu.findItem(R.id.menu_show_user).setEnabled(show != AdapterApp.enumShow.none);
                this.menu.findItem(R.id.menu_show_icon).setEnabled(show != AdapterApp.enumShow.none);
                this.menu.findItem(R.id.menu_show_all).setEnabled(show != AdapterApp.enumShow.none);
                switch (show) {
                    case user:
                        this.menu.findItem(R.id.menu_show_user).setChecked(true);
                        break;
                    case icon:
                        this.menu.findItem(R.id.menu_show_icon).setChecked(true);
                        break;
                    case all:
                        this.menu.findItem(R.id.menu_show_all).setChecked(true);
                        break;
                }
                return true;

            case R.id.menu_show_user:
            case R.id.menu_show_icon:
            case R.id.menu_show_all:
                item.setChecked(!item.isChecked());
                final AdapterApp.enumShow set;
                switch (item.getItemId()) {
                    case R.id.menu_show_user:
                        set = AdapterApp.enumShow.user;
                        break;
                    case R.id.menu_show_all:
                        set = AdapterApp.enumShow.all;
                        break;
                    default:
                        set = AdapterApp.enumShow.icon;
                        break;
                }
                fragmentMain.setShow(set);
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        //XLuaCall.putSetting(ActivityMain.this, "show", set.name());
                    }
                });
                return true;

            case R.id.menu_help:
                menuHelp();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void menuHelp() {
        startActivity(new Intent(this, ActivityHelp.class));
    }
    private void menuProps() { startActivity(new Intent(this, ActivityProperties.class)); }
    private void menuDBs() { startActivity(new Intent(this, ActivityDatabase.class)); }
    private void menuCPU() { startActivity(new Intent(this, ActivityCpu.class)); }
    private void menuConfig() { startActivity(new Intent(this, ActivityConfig.class)); }
    private void menuSettings() {
        //Intent settingIntent = new Intent(this, ActivitySettings.class);
        //settingIntent.putExtra("packageName", UserIdentityPacket.GLOBAL_NAMESPACE);
        //startActivity(settingIntent);
        //startActivity(new Intent(this, ActivitySettings.class));
        Intent settingIntent = new Intent(this, SettingsExActivity.class);
        settingIntent.putExtra(UserClientAppContext.FIELD_APP_PACKAGE_NAME,  UserClientAppContext.GLOBAL_NAME_SPACE);
        startActivity(settingIntent);
    }


    public void updateMenu(Menu menu) {
        // Search
        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();
        if (searchView != null) {
            String pkg = getIntent().getStringExtra(EXTRA_SEARCH_PACKAGE);
            if (pkg != null) {
                Log.i(TAG, "Search pkg=" + pkg);
                menuSearch.expandActionView();
                searchView.setQuery(pkg, true);
            }
        }
    }

    public void whatsNew() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.thankyou, null, false);
        TextView tvLicence = view.findViewById(R.id.tvThankYouObbed);
        tvLicence.setMovementMethod(LinkMovementMethod.getInstance());
        int year = Calendar.getInstance().get(Calendar.YEAR);
        tvLicence.setText(Html.fromHtml(getString(R.string.whats_new, year)));
        firstRunDialog = new AlertDialog.Builder(this)
                .setView(view).setCancelable(false)
                .setPositiveButton(R.string.option_thanks, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { Toast.makeText(getApplicationContext(), "ObedCode Says good luck", Toast.LENGTH_SHORT).show(); }
                }).create();
        firstRunDialog.show();
    }

    public void initCore() {
        String lastHash = PrefUtil.getString(this, PrefUtil.PREF_LAST_RUN);
        String curHash = String.valueOf(BuildConfig.VERSION_NAME.hashCode());
        if(lastHash == null || lastHash.equals(curHash)) {
            try {
                XResult res = CleanHooksCommand.invokeEx(this);
                if(res.succeeded()) {
                    PrefUtil.setString(this, PrefUtil.PREF_LAST_RUN, curHash);
                    for(int i = 0; i < 6; i++)
                        CleanHooksCommand.invokeEx(this);
                }
            }catch (Exception e) {
                XLog.e("Failed to Clean PEX Hooks", e, true);
            }
        }

        checkFirstRun();
    }

    public void checkFirstRun() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = prefs.getBoolean("firstrun", true);
        if (firstRun && firstRunDialog == null) {
            final XUtil.DialogObserver observer = new XUtil.DialogObserver();

            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.license, null, false);
            TextView tvLicence = view.findViewById(R.id.tvLicense);
            tvLicence.setMovementMethod(LinkMovementMethod.getInstance());

            int year = Calendar.getInstance().get(Calendar.YEAR);
            tvLicence.setText(Html.fromHtml(getString(R.string.title_license, year)));

            firstRunDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .setPositiveButton(R.string.title_accept, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putBoolean("firstrun", false).apply();
                        }
                    })
                    .setNegativeButton(R.string.title_deny, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            firstRunDialog = null;
                            observer.stopObserving();
                        }
                    })
                    .create();
            firstRunDialog.show();

            observer.startObserving(this, firstRunDialog);
        }

        if(!PrefUtil.getBoolean(this, "welcome", false, true)) {
            PrefUtil.setBoolean(this, "welcome", true);
            whatsNew();
        }
    }

    private static class DrawerItem {
        private final int id;
        private final String title;
        private final boolean checkable;
        private boolean checked;
        private final IListener listener;

        DrawerItem(Context context, int title, IListener listener) {
            this.id = title;
            this.title = context.getString(title);
            this.checkable = false;
            this.checked = false;
            this.listener = listener;
        }

        DrawerItem(Context context, int title, boolean checked, IListener listener) {
            this.id = title;
            this.title = context.getString(title);
            this.checkable = true;
            this.checked = checked;
            this.listener = listener;
        }

        public int getId() {
            return this.id;
        }

        public String getTitle() {
            return this.title;
        }

        boolean isCheckable() {
            return this.checkable;
        }

        boolean isChecked() {
            return this.checked;
        }

        void onClick() {
            if (this.checkable)
                this.checked = !this.checked;
            if (this.listener != null)
                this.listener.onClick(this);
        }

        interface IListener {
            void onClick(DrawerItem item);
        }
    }

    private static class ArrayAdapterDrawer extends ArrayAdapter<DrawerItem> {
        private final int resource;

        ArrayAdapterDrawer(@NonNull Context context, int resource) {
            super(context, resource);
            this.resource = resource;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row;
            if (null == convertView)
                row = LayoutInflater.from(getContext()).inflate(this.resource, null);
            else
                row = convertView;

            DrawerItem item = getItem(position);

            TextView tv = row.findViewById(R.id.tvItem);
            CheckBox cb = row.findViewById(R.id.cbItem);
            tv.setText(item.getTitle());
            cb.setVisibility(item.isCheckable() ? View.VISIBLE : View.GONE);
            cb.setChecked(item.isChecked());

            return row;
        }
    }
}
