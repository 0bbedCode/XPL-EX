package eu.faircode.xlua;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import eu.faircode.xlua.ui.SettingsDialogCallbackActivity;

public class ActivitySettings extends SettingsDialogCallbackActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingscontent);
        initFragmentTransaction(FragmentSettings.class, R.id.content_frame_settings, true);
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
        prepareMenuSearch(menu.findItem(R.id.menu_search_settings));    //Here
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { return super.onOptionsItemSelected(item); }
}
