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

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import java.util.Locale;

public class ApplicationEx extends Application {
    private static final String TAG = "XLua.App";

    @Override
    public void onCreate() {
        //setLocale();
        super.onCreate();
        //setLocale();
        Log.i(TAG, "Create version=" + BuildConfig.VERSION_NAME);
        Log.w(TAG, "SHA1 Fingerprint For [" + XCommandBridgeStatic.PRO_PACKAGE + "] package equals=[" + XSecurity.getProFingerPrint(getApplicationContext()) + "]");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setLocale();
    }

    /*private void setLocale() {
        final Resources resources = getResources();
        final Configuration configuration = resources.getConfiguration();
        //final Locale locale = ActivityBase.getLocale(this);
        final Locale locale = new Locale("es");
        if (!configuration.locale.equals(locale)) {
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, null);
        }
    }*/
}