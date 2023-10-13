package opencontacts.open.com.opencontacts.activities.services;

import static android.telecom.Call.Details.DIRECTION_OUTGOING;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import opencontacts.open.com.opencontacts.data.datastore.ContactsDBHelper;
import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.orm.Contact;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class CallScreeningServiceImplementation extends CallScreeningService {
    CallResponse reject = new CallResponse.Builder()
        .setDisallowCall(true)
        .setRejectCall(true)
        .setSkipCallLog(false)
        .setSkipNotification(false)
        .build();

    CallResponse silence = new CallResponse.Builder()
        .setDisallowCall(false)
        .setSilenceCall(true)
        .setSkipCallLog(false)
        .setSkipNotification(false)
        .build();

    CallResponse allow = new CallResponse.Builder()
        .build();

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;
        Log.i("G&S","Modificato");
        if (callDetails.getCallDirection() == DIRECTION_OUTGOING || !getBoolean(SharedPreferencesUtils.ENABLE_CALL_FILTERING_SHARED_PREF_KEY, false, this)) {
            respondToCall(callDetails, allow);
            return;
        }
        String callingPhonenumber = callDetails.getHandle().getSchemeSpecificPart();
        Log.i("G&S","Modificato");
        Contact probableContact = ContactsDBHelper.getContactFromDB(callingPhonenumber);
        if (probableContact == null) {
            Log.i("G&S", "Modificato");
            respondToCall(callDetails, getBoolean(SharedPreferencesUtils.CALL_FILTER_REJECT_CALLS_SHARED_PREF_KEY, false, this) ? reject : silence);
        } else respondToCall(callDetails, allow);
    }
}
