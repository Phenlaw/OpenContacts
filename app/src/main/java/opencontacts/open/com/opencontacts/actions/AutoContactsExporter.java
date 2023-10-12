package opencontacts.open.com.opencontacts.actions;

import static android.widget.Toast.LENGTH_LONG;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.hasPermission;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.toastFromNonUIThread;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.hasItBeenAWeekSinceLastExportOfContacts;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.markAutoExportComplete;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import java.util.List;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.DomainUtils;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

public class AutoContactsExporter implements ContactsHouseKeepingAction {

    @Override
    public void perform(List<Contact> contacts, Context context) {
        Log.i("G&S","Modificato");
        if (!(getBoolean(SharedPreferencesUtils.EXPORT_CONTACTS_EVERY_WEEK_SHARED_PREF_KEY, true, context) && hasItBeenAWeekSinceLastExportOfContacts(context))) return;
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) return;
        try {
            DomainUtils.exportAllContacts(context);
            markAutoExportComplete(context);
        } catch (Exception e) {
            e.printStackTrace();
            toastFromNonUIThread(R.string.failed_exporting_contacts, LENGTH_LONG, context);
        }
    }
}
