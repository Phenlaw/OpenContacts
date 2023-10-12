package opencontacts.open.com.opencontacts.data.datastore;

import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.widget.Toast.LENGTH_LONG;
import static com.orm.SugarRecord.find;
import static com.orm.SugarRecord.findWithQuery;
import static java.util.Collections.emptyList;
import static opencontacts.open.com.opencontacts.data.datastore.CallLogDataStore.CALL_LOG_ENTRIES_CHUNK_SIZE;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.hasPermission;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.toastFromNonUIThread;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.getLastSavedCallLogDate;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.setLastSavedCallLogDate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import androidx.core.app.ActivityCompat;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.github.underscore.U;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;


/**
 * Created by sultanm on 8/5/17.
 */

class CallLogDBHelper {
    private Map<String, Integer> simsInfo = null;

    public static void removeAllContactsLinking() {
        //da ottimizzare FORSE
        U.forEach(getRecentCallLogEntriesFromDB(), callLogEntry -> {
            callLogEntry.name = null;
            callLogEntry.contactId = -1;
            callLogEntry.save();
        });
    }

    private void createSimsInfo(Context context) {
        simsInfo = new HashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = context.getSystemService(TelecomManager.class);
            if (telecomManager == null || !hasPermission(Manifest.permission.READ_PHONE_STATE, context))
                return;
            //added permission check above using util intellij wasn't able to identify it
            @SuppressLint("MissingPermission") List<PhoneAccountHandle> callCapablePhoneAccounts = telecomManager.getCallCapablePhoneAccounts();
            if (callCapablePhoneAccounts.size() < 2) return;
            //da ottimizzare FORSE
            U.forEachIndexed(callCapablePhoneAccounts, (index, phoneAccount) -> simsInfo.put(phoneAccount.getId(), index + 1));
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            @SuppressLint("MissingPermission") List<SubscriptionInfo> activeSubscriptionInfoList = ((SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)).getActiveSubscriptionInfoList();
            if (activeSubscriptionInfoList == null)
                return;
            //da ottimizzare
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                simsInfo.put(subscriptionInfo.getIccId(), subscriptionInfo.getSimSlotIndex() + 1);
            }
        }
    }

    public List<CallLogEntry> loadRecentCallLogEntriesIntoDB(Context context) {
        try {
            List<CallLogEntry> callLogEntries = getRecentCallLogEntries(context);
            CallLogEntry.saveInTx(callLogEntries);
            return callLogEntries;
        } catch (Exception e) {
            e.printStackTrace();
            toastFromNonUIThread(R.string.failed_fetching_recent_calllog, LENGTH_LONG, context);
            return emptyList();
        }
    }

    private List<CallLogEntry> getRecentCallLogEntries(final Context context) throws Exception { // throwing exception coz anything can happen here while fetching call log from system.
        if (ActivityCompat.checkSelfPermission(context, READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return new ArrayList<>(0);
        }
        Cursor c;
        String mobileNumberInvolvedInCall, dateOfCall, durationOfCall, callType, subscriptionIdForCall;
        ArrayList<CallLogEntry> callLogEntries = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {//TODO: refactor below two if else blocks 90% same
            createSimsInfo(context);
            c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.PHONE_ACCOUNT_ID}, CallLog.Calls.DATE + " > ?", new String[]{getLastSavedCallLogDate(context)}, CallLog.Calls.DATE + " DESC");
            if (c.getCount() == 0)
                return callLogEntries;
            int columnIndexForNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
            int columnIndexForDuration = c.getColumnIndex(CallLog.Calls.DURATION);
            int columnIndexForDate = c.getColumnIndex(CallLog.Calls.DATE);
            int columnIndexForCallType = c.getColumnIndex(CallLog.Calls.TYPE);
            int columnIndexForSubscriptionId = c.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID);
            while (c.moveToNext()) {
                mobileNumberInvolvedInCall = c.getString(columnIndexForNumber);// for  number
                durationOfCall = c.getString(columnIndexForDuration);// for duration
                dateOfCall = c.getString(columnIndexForDate);
                callType = c.getString(columnIndexForCallType);// for call type, Incoming or out going
                subscriptionIdForCall = c.getString(columnIndexForSubscriptionId);

                if(mobileNumberInvolvedInCall == null) continue;
                Log.i("G&S","Modificato");
                opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getContactFromDB(mobileNumberInvolvedInCall);
                if (contact == null)
                    callLogEntries.add(new CallLogEntry(null, (long) -1, mobileNumberInvolvedInCall, durationOfCall, callType, dateOfCall, getSimIdOrDefault(subscriptionIdForCall)));
                else {
                    Log.i("G&S", "Modificato");
                    callLogEntries.add(new CallLogEntry(contact.firstName + " " + contact.lastName, contact.getId(), mobileNumberInvolvedInCall, durationOfCall, callType, dateOfCall, getSimIdOrDefault(subscriptionIdForCall)));
                }
            }
            c.moveToFirst();
            setLastSavedCallLogDate(c.getString(columnIndexForDate), context);
            c.close();
        } else {
            c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE}, CallLog.Calls.DATE + " > ?", new String[]{getLastSavedCallLogDate(context)}, CallLog.Calls.DATE + " DESC");
            if (c.getCount() == 0)
                return callLogEntries;
            int columnIndexForNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
            int columnIndexForDuration = c.getColumnIndex(CallLog.Calls.DURATION);
            int columnIndexForDate = c.getColumnIndex(CallLog.Calls.DATE);
            int columnIndexForCallType = c.getColumnIndex(CallLog.Calls.TYPE);
            while (c.moveToNext()) {
                mobileNumberInvolvedInCall = c.getString(columnIndexForNumber);// for  number
                durationOfCall = c.getString(columnIndexForDuration);// for duration
                dateOfCall = c.getString(columnIndexForDate);
                callType = c.getString(columnIndexForCallType);// for call type, Incoming or out going

                if(mobileNumberInvolvedInCall == null) continue;
                Log.i("G&S","Modificato");
                opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getContactFromDB(mobileNumberInvolvedInCall);
                if (contact == null)
                    callLogEntries.add(new CallLogEntry(null, (long) -1, mobileNumberInvolvedInCall, durationOfCall, callType, dateOfCall, 1));
                else {
                    Log.i("G&S", "Modificato");
                    callLogEntries.add(new CallLogEntry(contact.firstName + " " + contact.lastName, contact.getId(), mobileNumberInvolvedInCall, durationOfCall, callType, dateOfCall, 1));
                }
            }
            c.moveToFirst();
            setLastSavedCallLogDate(c.getString(columnIndexForDate), context);
            c.close();
        }
        return callLogEntries;
    }

    private int getSimIdOrDefault(String subscriptionIdForCall) {
        Integer simId = simsInfo.get(subscriptionIdForCall);
        return simId == null ? 1 : simId;
    }

    public static List<CallLogEntry> getRecentCallLogEntriesFromDB() {
        return CallLogEntry.find(CallLogEntry.class, null, null, null, "date desc", String.valueOf(CALL_LOG_ENTRIES_CHUNK_SIZE));
    }

    public static List<CallLogEntry> getCallLogEntriesFromDB(int size) {
        int validSize = size > 0 ? size : CALL_LOG_ENTRIES_CHUNK_SIZE;
        return CallLogEntry.find(CallLogEntry.class, null, null, null, "date desc", String.valueOf(validSize));
    }

    public static boolean delete(Long id) {
        CallLogEntry callLogEntryToBeDeleted = CallLogEntry.findById(CallLogEntry.class, id);
        return callLogEntryToBeDeleted != null && callLogEntryToBeDeleted.delete();
    }

    static List<CallLogEntry> getCallLogEntriesFor(long contactId) {
        return find(CallLogEntry.class, "contact_Id = ?", new String[]{"" + contactId}, null, "date desc", null);
    }

    static List<CallLogEntry> getCallLogEntriesFor(long contactId, int offset) {
        return findWithQuery(CallLogEntry.class, "select * from call_log_entry where contact_id= ? order by date desc limit ? offset ?", String.valueOf(contactId), String.valueOf(CALL_LOG_ENTRIES_CHUNK_SIZE), String.valueOf(offset));
    }

    static List<CallLogEntry> getCallLogEntriesFor(String phoneNumber) {
        return find(CallLogEntry.class, "phone_Number = ?", new String[]{phoneNumber}, null, "date desc", null);
    }
}
