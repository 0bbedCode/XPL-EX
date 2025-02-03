package eu.faircode.xlua.x.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import eu.faircode.xlua.FragmentSettings;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.core.acitivty.ListBaseActivity;
import eu.faircode.xlua.x.ui.dialogs.HelpDialog;
import eu.faircode.xlua.x.ui.fragments.SettingExFragment;

public class SettingsExActivity extends ListBaseActivity {
    public static final String SHARED_TAG = "x_settings_x";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_ex_activity);
        super.startFragmentTransaction(SettingExFragment.class, R.id.content_frame_settings_ex, true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) { super.onPostCreate(savedInstanceState); }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) { super.onConfigurationChanged(newConfig); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settingsmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        bindMenuSearch(menu.findItem(R.id.menu_search_settings));

        MenuItem help = menu.findItem(R.id.menu_help_settings);
        if(help != null) {
            help.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                    HelpDialog.create()
                            .show(getSupportFragmentManager(), getString(R.string.title_help));

                    return true;
                }
            });
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { return super.onOptionsItemSelected(item); }

    @Override
    protected String getSharedTagId() {
        return SHARED_TAG;
    }
}
