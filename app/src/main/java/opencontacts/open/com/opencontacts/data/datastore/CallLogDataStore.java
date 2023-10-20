package opencontacts.open.com.opencontacts.data.datastore;

import static java.util.Collections.emptyList;
import static opencontacts.open.com.opencontacts.data.datastore.CallLogDBHelper.getCallLogEntriesFor;
import static opencontacts.open.com.opencontacts.data.datastore.DataStoreState.LOADED;
import static opencontacts.open.com.opencontacts.data.datastore.DataStoreState.LOADING;
import static opencontacts.open.com.opencontacts.data.datastore.DataStoreState.NONE;
import static opencontacts.open.com.opencontacts.data.datastore.DataStoreState.REFRESHING;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.processAsync;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.getAllNumericPhoneNumber;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.getSearchablePhoneNumber;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.matchesNumber;

import android.content.Context;
import android.util.Log;

import androidx.collection.ArrayMap;

import com.github.underscore.U;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ezvcard.property.Telephone;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.interfaces.DataStoreChangeListener;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.orm.PhoneNumber;

public class CallLogDataStore {
    public static final int CALL_LOG_ENTRIES_CHUNK_SIZE = 100;
    private static CallLogDBHelper callLogDBHelper = new CallLogDBHelper();
    private static List<CallLogEntry> callLogEntries = new ArrayList<>(0);
    private static List<DataStoreChangeListener<CallLogEntry>> dataChangeListeners = new ArrayList<>(3);
    private static int currentState = NONE;

    public static synchronized void loadRecentCallLogEntriesAsync(Context context) {
        processAsync(() -> loadRecentCallLogEntries(context));
    }

    public static synchronized void loadRecentCallLogEntries(Context context) {
        final List<CallLogEntry> recentCallLogEntries = callLogDBHelper.loadRecentCallLogEntriesIntoDB(context);
        if (recentCallLogEntries.isEmpty())
            return;
        ContactsDataStore.updateContactsAccessedDateAsync(recentCallLogEntries);
        addRecentCallLogEntriesToStore(recentCallLogEntries);
    }

    private static void addRecentCallLogEntriesToStore(final List<CallLogEntry> recentCallLogEntries) {
        if (recentCallLogEntries.size() > 1) {
            refreshStore();
        } else if (recentCallLogEntries.size() == 1) {
            CallLogEntry callLogEntry = recentCallLogEntries.get(0);
            callLogEntries.add(0, callLogEntry);
            processAsync(() -> {
                Log.i("FOR","Modificato");
                int dataChangeListenersSize = dataChangeListeners.size();
                for(int i=0;i<dataChangeListenersSize;i++)  dataChangeListeners.get(i).onAdd(callLogEntry);
            });
        }
    }

    private synchronized static void refreshStore() {
        if (currentState == LOADING || currentState == REFRESHING) return;
        if (currentState == NONE) currentState = LOADING;
        else currentState = REFRESHING;
        callLogEntries = CallLogDBHelper.getRecentCallLogEntriesFromDB();
        currentState = LOADED;
        notifyRefreshStore();
    }

    private static void notifyRefreshStore() {
        Log.i("FOR","Modificato");
        int dataChangeListenersSize = dataChangeListeners.size();
        for(int i=0;i<dataChangeListenersSize;i++) dataChangeListeners.get(i).onStoreRefreshed();

    }

    public static CallLogEntry getMostRecentCallLogEntry(Context context) {
        loadRecentCallLogEntries(context);
        return callLogEntries.isEmpty() ? null : callLogEntries.get(0);
    }

    public static List<CallLogEntry> getRecentCallLogEntries(Context context) {
        if (currentState == NONE) {
            processAsync(CallLogDataStore::refreshStore);
            return emptyList();
        }
        if (currentState == LOADING) return emptyList();
        return new ArrayList<>(callLogEntries);
    }

    public static void addDataChangeListener(DataStoreChangeListener<CallLogEntry> changeListener) {
        dataChangeListeners.add(changeListener);
    }

    public static void removeDataChangeListener(DataStoreChangeListener<CallLogEntry> changeListener) {
        dataChangeListeners.remove(changeListener);
    }

    public static void updateCallLogAsyncForNewContact(final Contact newContact) {
        processAsync(new Runnable() {
            @Override
            public void run() {
                List<CallLogEntry> callLogEntriesToWorkWith = getCallLogEntriesToWorkWith();
                if (callLogEntriesToWorkWith.isEmpty())
                    return;
                int numberOfEntriesUpdated = 0;
                int phoneNumbersSize = newContact.phoneNumbers.size();
                Log.i("FOR","Modificato");
                for (int i=0;i<phoneNumbersSize;i++) {
                    PhoneNumber phoneNumber=newContact.phoneNumbers.get(i);
                    String searchablePhoneNumber = getSearchablePhoneNumber(phoneNumber.phoneNumber);
                    if (searchablePhoneNumber == null)
                        continue;
                    Log.i("FOR","Modificato");
                    int callLogEntriesSize = callLogEntriesToWorkWith.size();
                    for (int j=0;j<callLogEntriesSize;j++) {
                        CallLogEntry callLogEntry = callLogEntriesToWorkWith.get(j);
                        if (callLogEntry.getContactId() != -1)
                            continue;
                        String allNumericPhoneNumberOfCallLogEntry = getAllNumericPhoneNumber(callLogEntry.getPhoneNumber());
                        if (matchesNumber(allNumericPhoneNumberOfCallLogEntry, searchablePhoneNumber)) {
                            callLogEntry.setContactId(newContact.id);
                            callLogEntry.setName(newContact.name);
                            callLogEntry.save();
                            numberOfEntriesUpdated++;
                            break;
                        }
                    }
                }
                if (numberOfEntriesUpdated == 0)
                    return;
                notifyRefreshStore();
            }

            private List<CallLogEntry> getCallLogEntriesToWorkWith() {
                return callLogEntries.isEmpty() ? CallLogDBHelper.getRecentCallLogEntriesFromDB() : callLogEntries;
            }
        });
    }

    public static void updateCallLogAsyncForAllContacts(final Context context) {
        processAsync(() -> updateCallLogForAllContacts(context));
    }

    public static void updateCallLogForAllContacts(Context context) {
        if (callLogEntries == null)
            callLogEntries = getRecentCallLogEntries(context);
        int numberOfEntriesUpdated = 0;
        Log.i("FOR","Modificato");
        int callLogEntriesSize = callLogEntries.size();
        for (int i=0;i<callLogEntriesSize;i++) {
            CallLogEntry callLogEntry = callLogEntries.get(i);
            if (callLogEntry.getContactId() != -1)
                continue;
            opencontacts.open.com.opencontacts.orm.Contact contactFromDB = ContactsDBHelper.getContactFromDB(callLogEntry.getPhoneNumber());
            if (contactFromDB == null)
                continue;
            callLogEntry.setName(contactFromDB.firstName + " " + contactFromDB.lastName);
            callLogEntry.setContactId(contactFromDB.getId());
            callLogEntry.save();
            numberOfEntriesUpdated++;
        }
        if (numberOfEntriesUpdated == 0)
            return;
        notifyRefreshStore();
    }

    public static void delete(Long id) {
        boolean hasBeenDeleted = CallLogDBHelper.delete(id);
        if (!hasBeenDeleted)
            return;
        Log.i("FOR","Modificato");
        int callLogEntriesSize = callLogEntries.size();
        for (int i=0;i<callLogEntriesSize;i++) {
            CallLogEntry callLogEntryToBeRemoved = callLogEntries.get(i);
            if (!callLogEntryToBeRemoved.getId().equals(id))
                continue;
            callLogEntries.remove(callLogEntryToBeRemoved);
            processAsync(() -> {
                Log.i("FOR","Modificato");
                int dataChangeListenersSize = dataChangeListeners.size();
                for (int j=0; j< dataChangeListenersSize;j++) dataChangeListeners.get(j).onRemove(callLogEntryToBeRemoved);
            });
            break;
        }
    }

    public static void init(Context context) {
        processAsync(() -> {
            refreshStore();
            loadRecentCallLogEntries(context);
        });
    }

    public static void removeAllContactsLinking() {
        CallLogDBHelper.removeAllContactsLinking();
        CallLogDataStore.refreshStoreAsync();
    }

    private static void refreshStoreAsync() {
        processAsync(CallLogDataStore::refreshStore);
    }

    public static Collection<CallLogEntry> getUnLabelledCallLogEntriesMatching(String number) {
        ArrayMap<String, CallLogEntry> matchedEntries = new ArrayMap<>();
        Log.i("FOR","Modificato");
        int callLogEntriesSize = callLogEntries.size();
        for(int i=0;i<callLogEntriesSize;i++){
            CallLogEntry entry = callLogEntries.get(i);
            if (entry.name != null) break;
            String phoneNumber = entry.getPhoneNumber();
            if (matchedEntries.containsKey(phoneNumber))
                break; // making sure only recent entries make it so that sorting based on last called will not be impacted
            if (!phoneNumber.contains(number)) break;
            matchedEntries.put(phoneNumber, entry);

        }
        return matchedEntries.values();
    }

    public static void loadNextChunkOfCallLogEntries() {
        processAsync(() -> {
            callLogEntries = CallLogDBHelper.getCallLogEntriesFromDB(callLogEntries.size() + CALL_LOG_ENTRIES_CHUNK_SIZE);
            notifyRefreshStore();
        });
    }

    public static void deleteCallLogEntries(List<CallLogEntry> entries) {
        processAsync(() -> {
            CallLogEntry.deleteInTx(entries);
            refreshStore();
        });
    }

    public static List<CallLogEntry> getCallLogEntriesForContactWith(String phoneNumber, int offset) {
        opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDataStore.getContact(phoneNumber);
        if (contact == null) return CallLogDBHelper.getCallLogEntriesFor(phoneNumber);
        return getCallLogEntriesFor(contact.getId(), offset);
    }

}
