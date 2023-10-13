package opencontacts.open.com.opencontacts.fragments;

import static android.content.Context.MODE_PRIVATE;
import static open.fontscaling.FontScalingUtil.setCustomFontSizeOnViewCreated;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

public class AppBaseFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i("G&S","Modificato"); Log.i("G&S","Modificato2"); Log.i("G&S","Modificato3");
       getActivity().getTheme().applyStyle(getActivity().getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE).getBoolean(SharedPreferencesUtils.IS_DARK_THEME_ACTIVE_PREFERENCES_KEY, false) ? R.style.Theme_AppCompat_NoActionBar_Customized : R.style.Theme_AppCompat_Light_NoActionBar_Customized, true);
        setCustomFontSizeOnViewCreated(getActivity());
        super.onCreate(savedInstanceState);
    }

    public boolean handleBackPress() {
        return false;
    }
}
