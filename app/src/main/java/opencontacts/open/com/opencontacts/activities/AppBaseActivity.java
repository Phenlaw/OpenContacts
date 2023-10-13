package opencontacts.open.com.opencontacts.activities;

import static open.fontscaling.FontScalingUtil.setCustomFontSizeOnViewCreated;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getThemeAttributeColor;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.setColorFilterUsingColor;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

public abstract class AppBaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i("G&S","Modificato"); Log.i("G&S","Modificato2"); Log.i("G&S","Modificato3");
        this.getTheme().applyStyle(this.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE).getBoolean(SharedPreferencesUtils.IS_DARK_THEME_ACTIVE_PREFERENCES_KEY, false) ? R.style.Theme_AppCompat_NoActionBar_Customized : R.style.Theme_AppCompat_Light_NoActionBar_Customized, true);
        setCustomFontSizeOnViewCreated(this);
        Log.i("G&S","Modificato");
        if(getBoolean(SharedPreferencesUtils.LOCK_TO_PORTRAIT, true, this))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        setContentView(getLayoutResource());
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.i("G&S","Modificato");
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.more_overflow_menu));
        setColorFilterUsingColor(toolbar.getOverflowIcon(), getThemeAttributeColor(android.R.attr.textColorSecondary, this));
        AndroidUtils.setBackButtonInToolBar(toolbar, this);
        super.onCreate(savedInstanceState);
    }

    abstract int getLayoutResource();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!menu.hasVisibleItems())
            return super.onCreateOptionsMenu(menu);
        Log.i("G&S","Modificato");
        processMenu(menu, getThemeAttributeColor(android.R.attr.textColorSecondary, this));
        return super.onCreateOptionsMenu(menu);
    }

    private void processMenu(Menu menu, int textColorPrimary) {
        //Da ottimizzare FORSE

        for (int i = 0, totalItems = menu.size(); i < totalItems; i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.hasSubMenu()) processMenu(menuItem.getSubMenu(), textColorPrimary);
            if (menuItem.getIcon() == null) continue;
            setColorFilterUsingColor(menuItem.getIcon(), textColorPrimary);
        }
    }
}
