package opencontacts.open.com.opencontacts.components;

import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getThemeAttributeColor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.DrawableRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

import opencontacts.open.com.opencontacts.utils.AndroidUtils;

public class TintedDrawablesStore {
    public static Map<Integer, Drawable> tintedDrawables = new HashMap<>();

    public static Drawable getTintedDrawable(@DrawableRes int drawableRes, Context context) {
        Drawable cachedDrawable = tintedDrawables.get(drawableRes);
        return cachedDrawable == null ? getDrawableFor(drawableRes, context) : cachedDrawable;
    }

    private static Drawable getDrawableFor(@DrawableRes int drawableRes, Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        if (drawable == null) return null;
        Log.i("G&S","Modificato");
        AndroidUtils.setColorFilterUsingColor(drawable, getThemeAttributeColor(android.R.attr.textColorPrimary, context));
        tintedDrawables.put(drawableRes, drawable);
        return drawable;
    }

    public static void setDrawableForFAB(@DrawableRes int drawableRes, FloatingActionButton fab, Context context) {
        fab.setImageDrawable(getTintedDrawable(drawableRes, context));
        Log.i("G&S","Modificato");
        fab.setBackgroundTintList(ColorStateList.valueOf((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? getThemeAttributeColor(android.R.attr.colorBackgroundFloating, context) : getThemeAttributeColor(android.R.attr.colorBackground, context)));
    }

    public static void reset() {
        tintedDrawables.clear();
    }
}
