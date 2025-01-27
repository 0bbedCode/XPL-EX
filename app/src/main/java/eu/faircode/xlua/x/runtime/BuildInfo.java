package eu.faircode.xlua.x.runtime;

import android.os.Build;
import android.util.Log;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.process.ProcessUtils;

public class BuildInfo {
    private static final String TAG = "XLua.BuildInfo";

    //Build.VERSION.SDK_INT > Build.VERSION_CODES.M
    //https://apilevels.com/

    public static boolean isMiuiOrHyperOs() {
        boolean checkOne = Str.isValidNotWhitespaces(RuntimeUtils.getProperty("ro.mi.os.version.name"));
        boolean checkTwo = Str.isValidNotWhitespaces(RuntimeUtils.getProperty("ro.miui.ui.version.name"));
        boolean hasBuild = Str.containsAny(Build.BRAND, "xiaomi", "redmi", "poco", "blackshark");
        return hasBuild && (checkTwo || checkOne);
    }

    public static boolean isMarshmallowApi23Android6() { return Build.VERSION.SDK_INT == Build.VERSION_CODES.M; }
    public static boolean isNougatApi24and25Android7() { return isNougatApi24and25Android7(false); }
    public static boolean isNougatApi24and25Android7(boolean orIsHigher) {
        return orIsHigher ? Build.VERSION.SDK_INT >= Build.VERSION_CODES.N :
                Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1;
    }

    public static boolean isSnowConeApi31Android12() { return isSnowConeApi31Android12(false); }
    public static boolean isSnowConeApi31Android12(boolean orIsHigher) {
        //return orIsHigher ? Build.VERSION.SDK_INT >= Build.VERSION_CODES.S :
        //        Build.VERSION.SDK_INT == Build.VERSION_CODES.S;
        return orIsHigher ? Build.VERSION.SDK_INT >= 31 :
                Build.VERSION.SDK_INT == 31;
    }

    public static boolean isRedVelvetCakeApi30Android11() { return isRedVelvetCakeApi30Android11(false); }
    public static boolean isRedVelvetCakeApi30Android11(boolean orIsHigher) {
        return orIsHigher ? Build.VERSION.SDK_INT >= Build.VERSION_CODES.R :
                Build.VERSION.SDK_INT == Build.VERSION_CODES.R;
    }

    public static boolean isQuinceTartApi29Android10() { return isQuinceTartApi29Android10(false); }
    public static boolean isQuinceTartApi29Android10(boolean orIsHigher) {
        return orIsHigher ? Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q :
                Build.VERSION.SDK_INT == Build.VERSION_CODES.Q;
    }

    public static boolean isPieApi28Android9() { return isPieApi28Android9(false); }
    public static boolean isPieApi28Android9(boolean orIsHigher) {
        return orIsHigher ? Build.VERSION.SDK_INT >= Build.VERSION_CODES.P :
                Build.VERSION.SDK_INT == Build.VERSION_CODES.P;
    }

    public static boolean isApiLevel(int code) { return Build.VERSION.SDK_INT == code; }

    //@ChecksSdkIntAtLeast(parameter = 0)
    public static boolean isApiLevelOrHigher(int code) { return Build.VERSION.SDK_INT >= code; }

    public static boolean isBetweenApiLevels(int minCode, int maxCode) { return Build.VERSION.SDK_INT >= maxCode && Build.VERSION.SDK_INT <= maxCode;  }

    public static int getApiLevelFromVersion(double versionCode) {
        if(versionCode == 0) return Build.VERSION.SDK_INT;
        if(versionCode < 2) {
            if(versionCode == 1)
                return 1;       //BASE (NONE)
            if(versionCode == 1.1)
                return 2;       //BASE_1_1  (Petit Four)
            if(versionCode == 1.2 || versionCode == 1.5)
                return 3;       //CUPCAKE (Cupcake)
            if(versionCode == 1.3 || versionCode == 1.6 )
                return 4;       //DONUT (Donut)

            return Build.VERSION.SDK_INT;
        }

        if(versionCode < 3) {
            if(versionCode == 2)
                return 5;       //ECLAIR (ECLAIR)
            if(versionCode == 2.01)
                return 6;       //ECLAIR_0_1 (ECLAIR)
            if(versionCode == 2.1)
                return 7;       //ECLAIR_MR1 (ECLAIR)
            if(versionCode == 2.2)
                return 8;       //FROYO (Froyo)
            if(versionCode == 2.3 || versionCode == 2.32)
                return 9;       //GINGERBREAD (Gingerbread)
            if(versionCode == 2.33 || versionCode == 2.37)
                return 10;      //GINGERBREAD_MR1 (Gingerbread)

            return 10;//?
        }

        //Cool
        if(versionCode < 4) {
            if(versionCode == 3)
                return 11;      //HONEYCOMB (Honeycomb)
            if(versionCode == 3.1)
                return 12;      //HONEYCOMB_MR1 (Honeycomb)
            if(versionCode == 3.2)
                return 13;      //HONEYCOMB_MR2 (Honeycomb)

            return Build.VERSION.SDK_INT;
        }

        if(versionCode < 5) {
            if(versionCode == 4.0 || versionCode == 4.01 || versionCode == 4.02)
                return 14;      //ICE_CREAM_SANDWICH (Ice Cream Sandwich)
            if(versionCode == 4.03 || versionCode == 4.04)
                return 15;      //ICE_CREAM_SANDWICH_MR1 (Ice Cream Sandwich)
            if(versionCode == 4.1)
                return 16;      //JELLY_BEAN (Jelly Bean)
            if(versionCode == 4.2)
                return 17;      //JELLY_BEAN_MR1 (Jelly Bean)
            if(versionCode == 4.3)
                return 18;      //JELLY_BEAN_MR2 (Jelly Bean)
            if(versionCode == 4.4)
                return 19;      //KITKAT (KitKat)
            if(versionCode == 4.42 || versionCode == 4.5)
                return 20;      //KITKAT_WATCH (KitKat)

            return Build.VERSION.SDK_INT;
        }

        if(versionCode == 5)
            return 21;      //LOLLIPOP, (L) (Lollipop)
        if(versionCode == 5.1)
            return 22;      //LOLLIPOP_MR1, (L?) (Lollipop)
        if(versionCode == 6)
            return 23;      //M (Marshmallow)
        if(versionCode == 7)
            return 24;      //N (Nougat)
        if(versionCode == 7.1)
            return 25;      //N_MR1 (Nougat)
        if(versionCode == 8)
            return 26;      //O (Oreo)
        if(versionCode == 8.1)
            return 27;      //O_MR1 (Oreo)
        if(versionCode == 9)
            return 28;      //P (Pie)
        if(versionCode == 10)
            return 29;      //Q (Quince Tart)
        if(versionCode == 11)
            return 30;      //R (Red Velvet Cake)
        if(versionCode == 12)
            return 31;      //S (Snow Cone)
        if(versionCode == 12.1)
            return 32;      //S_V2 (Snow Cone)
        if(versionCode == 13)
            return 33;      //TIRAMISU (Tiramisu)
        if(versionCode == 14)
            return 34;      //UPSIDE_DOWN_CAKE (Upside Down Cake)
        if(versionCode == 15)
            return 35;      //VANILLA_ICE_CREAM (Vanilla Ice Cream)

        return Build.VERSION.SDK_INT;
    }
}
