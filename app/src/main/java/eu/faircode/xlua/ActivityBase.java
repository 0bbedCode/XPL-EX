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

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import eu.faircode.xlua.api.xlua.XLuaCall;

public class ActivityBase extends AppCompatActivity {
    private String theme;
    private static final String TAG = "XLua.ActivityBase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //theme = XProvider.getSetting(this, "global", "theme");
        theme = XLuaCall.getTheme(this);
        setTheme("dark".equals(theme) ? R.style.AppThemeDark : R.style.AppThemeLight);

        super.onCreate(savedInstanceState);
    }

    String getThemeName() {
        return (theme == null ? "light" : theme);
    }

    void setDarkMode() { setThemeName("dark"); }
    void setLightMode() { setThemeName("light"); }
    private void setThemeName(String name) {
        Log.i(TAG, "Set Theme=" + name);
        //XProvider.putSetting(this, "global", "theme", name);
        XLuaCall.putSetting(this, "theme", name);
        theme = name;
        setTheme("dark".equals(name) ? R.style.AppThemeDark : R.style.AppThemeLight);
        recreate();
    }
}
