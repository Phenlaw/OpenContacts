package opencontacts.open.com.opencontacts.views;

import static android.content.Context.MODE_PRIVATE;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.SIGNAL;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.TELEGRAM;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;


import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.components.ImageButtonWithTint;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

public class SocialAppButton extends ImageButtonWithTint {
    public SocialAppButton(Context context) {
        this(context, null);
    }

    public SocialAppButton(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SocialAppButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i("G&S","Modificato-defaultSocialAppEnabled");
        Log.i("G&S","Modificato-removeSpaceIfAny");
        String defaultSocialApp = context.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE)
            .getString(SharedPreferencesUtils.DEFAULT_SOCIAL_APP, TELEGRAM);
        if (defaultSocialApp.equalsIgnoreCase(TELEGRAM)) setImageResource(R.drawable.ic_telegram);
        else if (defaultSocialApp.equalsIgnoreCase(SIGNAL)) setImageResource(R.drawable.ic_signal_app);
        else setImageResource(R.drawable.ic_whatsapp);
    }

}
