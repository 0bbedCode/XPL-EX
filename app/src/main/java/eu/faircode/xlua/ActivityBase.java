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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import eu.faircode.xlua.api.XResult;
import eu.faircode.xlua.api.xlua.XLuaCall;
import eu.faircode.xlua.utilities.PrefUtil;

public class ActivityBase extends AppCompatActivity {
    private String theme;
    private boolean isForceEnglish;
    //private Locale mCurrentLocale;

    @Override
    protected void onStart() {
        super.onStart();
        //PrefUtil.setString(this, "language", "es");
        //mCurrentLocale = getResources().getConfiguration().locale;
        //mCurrentLocale = new Locale("es");
    }

    public void setForceEnglish(boolean force) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean("forceenglish", force).apply();
    }

    public boolean getIsForceEnglish() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!prefs.contains("forceenglish")) {
            prefs.edit().putBoolean("forceenglish", false).apply();
            return false;
        }

        return prefs.getBoolean("forceenglish", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        theme = XLuaCall.getTheme(this);
        isForceEnglish = getIsForceEnglish();
        setTheme("dark".equals(theme) ? R.style.AppThemeDark : R.style.AppThemeLight);
        if(isForceEnglish) {
            try {
                String languageToLoad  = "en"; // your language
                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
            }catch (Exception e) {
                Log.i("XLua.ActivityBase", "Not good mr huberth");
            }
        }

        //Both methods work tho the first one not sure how to change and keeps ovveriding to english this one one alone works as well
        /*String languageToLoad  = "es"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());*/

        super.onCreate(savedInstanceState);
    }


    /*@Override
    protected void onRestart() {
        //set theme ?
        super.onRestart();
        Locale locale = getLocale(this);
        if (!locale.equals(mCurrentLocale)) {
            mCurrentLocale = locale;
            recreate();
        }
    }*/

    public String getThemeName() { return (theme == null ? "dark" : theme); }

    /*public static Locale getLocale(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lang = sharedPreferences.getString("language", "es");
        switch (lang) {
            case "English":
                lang = "en";
                break;
            case "Spanish":
                lang = "es";
                break;
        }
        return new Locale(lang);
    }*/
}
