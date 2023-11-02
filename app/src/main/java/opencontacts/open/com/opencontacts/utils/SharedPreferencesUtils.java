package opencontacts.open.com.opencontacts.utils;

import static android.content.Context.MODE_PRIVATE;
import static android.text.TextUtils.isEmpty;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getLong;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getStringFromPreferences;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.updatePreference;
import static opencontacts.open.com.opencontacts.utils.Common.hasItBeen;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Log;

import java.util.Date;

import opencontacts.open.com.opencontacts.BuildConfig;
import opencontacts.open.com.opencontacts.R;

public class SharedPreferencesUtils {
    public static final String IS_DARK_THEME_ACTIVE_PREFERENCES_KEY = "IS_DARK_THEME_ACTIVE_PREFERENCES_KEY";//also hard coded in xml
    public static final String DEFAULT_SOCIAL_COUNTRY_CODE_PREFERENCES_KEY = "DEFAULT_SOCIAL_COUNTRY_CODE";
    public static final String CALLER_ID_X_POSITION_ON_SCREEN_PREFERENCE_KEY = "CALLER_ID_X_POSITION_ON_SCREEN";
    public static final String CALLER_ID_Y_POSITION_ON_SCREEN_PREFERENCE_KEY = "CALLER_ID_Y_POSITION_ON_SCREEN";
    public static final String SOCIAL_INTEGRATION_ENABLED_PREFERENCE_KEY = "SOCIAL_INTEGRATION_ENABLED";//also hard coded in xml
    public static final String ADDRESSBOOK_URL_SHARED_PREFS_KEY = "ADDRESSBOOK_URL";
    public static final String BASE_SYNC_URL_SHARED_PREFS_KEY = "BASE_SYNC_URL";
    public static final String CARD_DAV_SERVER_TYPE_SHARED_PREFS_KEY = "CARD_DAV_SERVER_TYPE";
    public static final String PREFTIMEFORMAT_12_HOURS_SHARED_PREF_KEY = "preftimeformat12hours";//also hard coded in xml
    public static final String SYNC_TOKEN_SHARED_PREF_KEY = "sync_token";
    public static final String T9_SEARCH_ENABLED_SHARED_PREF_KEY = "t9searchenabled";//also hard coded in xml
    public static final String T9_PINYIN_ENABLED_SHARED_PREF_KEY = "T9_PINYIN_ENABLED";//also hard coded in xml
    public static final String LAST_CALL_LOG_READ_TIMESTAMP_SHARED_PREF_KEY = "preference_last_call_log_saved_date";
    public static final String COMMON_SHARED_PREFS_FILE_NAME = "OpenContacts";
    public static final String SIM_PREFERENCE_SHARED_PREF_KEY = "defaultCallingSim";
    public static final String EXPORT_CONTACTS_EVERY_WEEK_SHARED_PREF_KEY = "exportContactsEveryWeek";
    public static final String LAST_EXPORT_TIME_STAMP = "lastExportTimeStamp";
    public static final int WEEKS_TIME_IN_HOURS = 24 * 7;
    public static final String ENCRYPTING_CONTACTS_EXPORT_KEY = "encryptingContactsExportKey";
    public static final String SORT_USING_FIRST_NAME = "sortUsingFirstName";
    public static final String LOCK_TO_PORTRAIT = "lockToPortrait";
    public static final String SINGLE_CONTACT_WIDGET_TO_CONTACT_MAPPING = "singleContactWidgetToContactMapping";
    public static final String SHOULD_ASK_FOR_PERMISSIONS = "SHOULD_ASK_FOR_PERMISSIONS";
    public static final String LAST_DEFAULT_TAB_LAUNCH_TIME_SHARED_PREF_KEY = "LAST_DEFAULT_TAB_LAUNCH_TIME";
    public static final String DEFAULT_TAB_SHARED_PREF_KEY = "DEFAULT_TAB";
    public static final String TOGGLE_CONTACT_ACTIONS = "TOGGLE_CONTACT_ACTIONS";
    public static final String DEFAULT_SIM_SELECTION_SYSTEM_DEFAULT = "-2";
    public static final String DEFAULT_SIM_SELECTION_ALWAYS_ASK = "-1";
    public static final String SHOULD_USE_SYSTEM_PHONE_APP = "SHOULD_USE_SYSTEM_PHONE_APP";
    public static final String SHORTCUTS_ADDED_IN_VERSION_SHARED_PREF_KEY = "SHORTCUTS_ADDED_IN_VERSION";
    public static final String KEYBOARD_RESIZE_VIEWS_SHARED_PREF_KEY = "KEYBOARD_RESIZE_VIEWS";
    public static final String BOTTOM_MENU_OPEN_DEFAULT_SHARED_PREF_KEY = "BOTTOM_MENU_OPEN_DEFAULT";
    public static final String LAST_VISITED_GROUP_SHARED_PREF_KEY = "LAST_VISITED_GROUP";
    public static final String DEFAULT_SOCIAL_APP= "default_social_app";
    public static final String TELEGRAM = "Telegram";
    public static final String SIGNAL = "Signal";
    public static final String WHATSAPP = "Whatsapp";

    public static final String SHOULD_AUTO_CANCEL_MISSED_CALL_NOTIF_SHARED_PREF_KEY = "SHOULD_AUTO_CANCEL_MISSED_CALL_NOTIF";
    public static final String SHOULD_SHOW_BOTTOM_MENU_SHARED_PREF_KEY = "SHOULD_SHOW_BOTTOM_MENU";
    public static final String ENABLE_CALL_FILTERING_SHARED_PREF_KEY = "enableCallFiltering";
    public static final String CALL_FILTER_REJECT_CALLS_SHARED_PREF_KEY = "rejectCalls";



    public static void saveCallerIdLocationOnScreen(int x, int y, Context context) {
        Log.i("G&S","Modificato-getAppsSharedPreferences");
        context.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE)
            .edit()
            .putInt(CALLER_ID_X_POSITION_ON_SCREEN_PREFERENCE_KEY, x)
            .putInt(CALLER_ID_Y_POSITION_ON_SCREEN_PREFERENCE_KEY, y)
            .apply();
    }


    public static void enableSocialappIntegration(String selectedCountryCodeWithPlus, Context context) {
        Log.i("G&S","Modificato-getAppsSharedPreferences");
        context.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE)
            .edit()
            .putString(DEFAULT_SOCIAL_COUNTRY_CODE_PREFERENCES_KEY, selectedCountryCodeWithPlus)
            .putBoolean(SOCIAL_INTEGRATION_ENABLED_PREFERENCE_KEY, true)
            .apply();
    }

    public static void disableSocialIntegration(Context context) {
        Log.i("G&S","Modificato-getAppsSharedPreferences");
        context.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(SOCIAL_INTEGRATION_ENABLED_PREFERENCE_KEY, false)
            .apply();
    }


    public static void setSharedPreferencesChangeListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener, Context context) {
        Log.i("G&S","Modificato-getAppsSharedPreferences");
        context.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

}
