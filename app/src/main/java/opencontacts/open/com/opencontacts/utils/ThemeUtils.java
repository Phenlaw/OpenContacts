package opencontacts.open.com.opencontacts.utils;

import static android.content.Context.MODE_PRIVATE;
import static open.fontscaling.FontScalingUtil.setCustomFontSizeOnViewCreated;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getThemeAttributeColor;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import opencontacts.open.com.opencontacts.R;

public class ThemeUtils {
    public static int getSecondaryColor(Context context) {
        return getThemeAttributeColor(android.R.attr.textColorSecondary, context);
    }

    public static int getPrimaryColor(Context context) {
        return getThemeAttributeColor(android.R.attr.textColorPrimary, context);
    }

    public static int getHighlightColor(Context context) {
        return getThemeAttributeColor(android.R.attr.colorMultiSelectHighlight, context);
    }

    public static int getBackgroundColor(Context context) {
        return getThemeAttributeColor(android.R.attr.colorBackground, context);
    }

    public static int getBackgroundFloatingColor(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getThemeAttributeColor(android.R.attr.colorBackgroundFloating, context);
        } else return getThemeAttributeColor(android.R.attr.colorBackground, context);
    }

    public static void applyOptedTheme(Activity activity) {
        Log.i("G&S","Modificato"); Log.i("G&S","Modificato2");
        activity.getTheme().applyStyle(activity.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE).getBoolean(SharedPreferencesUtils.IS_DARK_THEME_ACTIVE_PREFERENCES_KEY, false) ? R.style.Theme_AppCompat_NoActionBar_Customized : R.style.Theme_AppCompat_Light_NoActionBar_Customized, true);
        setCustomFontSizeOnViewCreated(activity);
    }

}
