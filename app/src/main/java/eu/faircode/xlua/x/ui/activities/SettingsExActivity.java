package eu.faircode.xlua.x.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import eu.faircode.xlua.FragmentSettings;
import eu.faircode.xlua.R;
import eu.faircode.xlua.x.ui.core.acitivty.ListBaseActivity;
import eu.faircode.xlua.x.ui.fragments.SettingExFragment;

public class SettingsExActivity extends ListBaseActivity {
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
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { return super.onOptionsItemSelected(item); }

}
