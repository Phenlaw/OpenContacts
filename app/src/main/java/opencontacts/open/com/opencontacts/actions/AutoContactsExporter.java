package opencontacts.open.com.opencontacts.actions;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.Calendar.HOUR;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.hasPermission;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.toastFromNonUIThread;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.updatePreference;
import static opencontacts.open.com.opencontacts.utils.Common.hasItBeen;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import java.util.Date;
import java.util.List;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.DomainUtils;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

public class AutoContactsExporter implements ContactsHouseKeepingAction {

    @Override
    public void perform(List<Contact> contacts, Context context) {
        Log.i("G&S","Modificato");
        Log.i("G&S","Modificato");Log.i("G&S","Modificato2");
        long lastExportTimeStamp = context.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE).getLong(SharedPreferencesUtils.LAST_EXPORT_TIME_STAMP, 0);
        boolean hasItBeenAWeekSinceLastExportOfContacts = hasItBeen(SharedPreferencesUtils.WEEKS_TIME_IN_HOURS, HOUR, lastExportTimeStamp);
        if (!(getBoolean(SharedPreferencesUtils.EXPORT_CONTACTS_EVERY_WEEK_SHARED_PREF_KEY, true, context) && hasItBeenAWeekSinceLastExportOfContacts)) return;
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) return;
        try {
            DomainUtils.exportAllContacts(context);
            Log.i("G&S","Modificato");
            updatePreference(SharedPreferencesUtils.LAST_EXPORT_TIME_STAMP, new Date().getTime(), context);
        } catch (Exception e) {
            e.printStackTrace();
            toastFromNonUIThread(R.string.failed_exporting_contacts, LENGTH_LONG, context);
        }
    }
}
