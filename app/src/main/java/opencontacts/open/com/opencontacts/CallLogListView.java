package opencontacts.open.com.opencontacts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import opencontacts.open.com.opencontacts.data.datastore.CallLogDataStore;
import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.interfaces.DataStoreChangeListener;
import opencontacts.open.com.opencontacts.interfaces.EditNumberBeforeCallHandler;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.Common;

import static opencontacts.open.com.opencontacts.utils.AndroidUtils.PREFTIMEFORMAT_12_HOURS_SHARED_PREF_KEY;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.WHATSAPP_INTEGRATION_ENABLED_PREFERENCE_KEY;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.is12HoursPreferedTimeFormat;

/**
 * Created by sultanm on 7/31/17.
 */

public class CallLogListView extends ListView implements DataStoreChangeListener<CallLogEntry> {
    private String UNKNOWN;
    Context context;
    private EditNumberBeforeCallHandler editNumberBeforeCallHandler;
    ArrayAdapter<CallLogEntry> adapter;
    private boolean isWhatsappIntegrationEnabled;
    //android has weakref to this listener and gets garbage collected hence we should have it here.
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    private SimpleDateFormat timeStampFormat;


    public CallLogListView(final Context context, EditNumberBeforeCallHandler editNumberBeforeCallHandler) {
        super(context);
        this.context = context;
        this.UNKNOWN = context.getString(R.string.unknown);
        this.editNumberBeforeCallHandler = editNumberBeforeCallHandler;
        isWhatsappIntegrationEnabled = AndroidUtils.isWhatsappIntegrationEnabled(context);
        timeStampFormat = getTimestampPattern(context);
        List<CallLogEntry> callLogEntries = new ArrayList<>();

        final OnClickListener callContact = v -> {
            CallLogEntry callLogEntry = (CallLogEntry) v.getTag();
            AndroidUtils.call(callLogEntry.getPhoneNumber(), context);
        };

        final OnClickListener whatsappContact = v -> {
            CallLogEntry callLogEntry = (CallLogEntry) ((View)v.getParent()).getTag();
            AndroidUtils.whatsapp(callLogEntry.getPhoneNumber(), context);
        };

        final OnClickListener messageContact = v -> {
            CallLogEntry callLogEntry = (CallLogEntry) ((View)v.getParent()).getTag();
            AndroidUtils.message(callLogEntry.getPhoneNumber(), context);
        };
        final OnClickListener addContact = v -> {
            final CallLogEntry callLogEntry = (CallLogEntry) ((View)v.getParent()).getTag();
            AndroidUtils.getAlertDialogToAddContact(callLogEntry.getPhoneNumber(), context).show();
        };
        final OnClickListener showContactDetails = v -> {
            CallLogEntry callLogEntry = (CallLogEntry) ((View)v.getParent()).getTag();
            long contactId = callLogEntry.getContactId();
            if(contactId == -1)
                return;
            Contact contact = ContactsDataStore.getContactWithId(contactId);
            if(contact == null)
                return;
            Intent showContactDetails1 = AndroidUtils.getIntentToShowContactDetails(contactId, CallLogListView.this.context);
            context.startActivity(showContactDetails1);
        };

        final OnLongClickListener callLogEntryLongClickListener = v -> {
            CallLogEntry callLogEntry = (CallLogEntry) v.getTag();
            new AlertDialog.Builder(context)
                    .setItems(new String[]{context.getString(R.string.copy_to_clipboard), context.getString(R.string.edit_before_call), context.getString(R.string.delete)}, (dialog, which) -> {
                        switch(which){
                            case 0:
                                AndroidUtils.copyToClipboard(callLogEntry.getPhoneNumber(), context);
                                Toast.makeText(context, R.string.copied_phonenumber_to_clipboard, Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                this.editNumberBeforeCallHandler.setNumber(callLogEntry.getPhoneNumber());
                                break;
                            case 2:
                                CallLogDataStore.delete(callLogEntry.getId());
                        }
                    }).show();

            return true;
        };

        adapter = new ArrayAdapter<CallLogEntry>(CallLogListView.this.context, R.layout.call_log_entry, callLogEntries){
            private LayoutInflater layoutInflater = LayoutInflater.from(CallLogListView.this.context);
            @NonNull
            @Override
            public View getView(int position, View reusableView, ViewGroup parent) {
                CallLogEntry callLogEntry = getItem(position);
                if(reusableView == null)
                    reusableView = layoutInflater.inflate(R.layout.call_log_entry, parent, false);
                ((TextView) reusableView.findViewById(R.id.textview_full_name)).setText(callLogEntry.getContactId() == -1 ? UNKNOWN : callLogEntry.getName());
                ((TextView) reusableView.findViewById(R.id.textview_phone_number)).setText(callLogEntry.getPhoneNumber());
                (reusableView.findViewById(R.id.button_message)).setOnClickListener(messageContact);
                View whatsappIcon = reusableView.findViewById(R.id.button_whatsapp);
                if(isWhatsappIntegrationEnabled){
                    whatsappIcon.setOnClickListener(whatsappContact);
                    whatsappIcon.setVisibility(VISIBLE);
                }
                else whatsappIcon.setVisibility(GONE);
                if(callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.INCOMING_TYPE)))
                    ((ImageView)reusableView.findViewById(R.id.image_view_call_type)).setImageResource(R.drawable.ic_call_received_black_24dp);
                else if(callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.OUTGOING_TYPE)))
                    ((ImageView)reusableView.findViewById(R.id.image_view_call_type)).setImageResource(R.drawable.ic_call_made_black_24dp);
                else if(callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.MISSED_TYPE)))
                    ((ImageView)reusableView.findViewById(R.id.image_view_call_type)).setImageResource(R.drawable.ic_call_missed_outgoing_black_24dp);
                ((TextView)reusableView.findViewById(R.id.text_view_duration)).setText(Common.getDurationInMinsAndSecs(Integer.valueOf(callLogEntry.getDuration())));
                ((TextView)reusableView.findViewById(R.id.text_view_sim)).setText(String.valueOf(callLogEntry.getSimId()));
                String timeStampOfCall = timeStampFormat.format(new Date(Long.parseLong(callLogEntry.getDate())));
                ((TextView)reusableView.findViewById(R.id.text_view_timestamp)).setText(timeStampOfCall);
                View addButton = reusableView.findViewById(R.id.image_button_add_contact);
                View infoButton = reusableView.findViewById(R.id.button_info);

                if(callLogEntry.getContactId() == -1){
                    addButton.setOnClickListener(addContact);
                    addButton.setVisibility(View.VISIBLE);
                    infoButton.setVisibility(View.INVISIBLE);
                }
                else{
                    addButton.setVisibility(View.INVISIBLE);
                    infoButton.setVisibility(View.VISIBLE);
                    infoButton.setOnClickListener(showContactDetails);
                }
                reusableView.setTag(callLogEntry);
                reusableView.setOnClickListener(callContact);
                reusableView.setOnLongClickListener(callLogEntryLongClickListener);
                return reusableView;
            }
        };
        this.setAdapter(adapter);
        CallLogDataStore.addDataChangeListener(this);
        reload();
        //android has weakref to this listener and gets garbage collected hence we should have it here.
        sharedPreferenceChangeListener = (sharedPreferences, key) -> {
            if (!WHATSAPP_INTEGRATION_ENABLED_PREFERENCE_KEY.equals(key)
                    && !PREFTIMEFORMAT_12_HOURS_SHARED_PREF_KEY.equals(key)
                    ) return;
            isWhatsappIntegrationEnabled = AndroidUtils.isWhatsappIntegrationEnabled(context);
            timeStampFormat = getTimestampPattern(context);
            adapter.notifyDataSetChanged();
        };
        AndroidUtils.setSharedPreferencesChangeListener(sharedPreferenceChangeListener, context);
    }

    @NonNull
    private SimpleDateFormat getTimestampPattern(Context context) {
        return new SimpleDateFormat(is12HoursPreferedTimeFormat(context) ? "dd/MM  hh:mm a" : "dd/MM HH:mm", Locale.getDefault());
    }

    @Override
    public void onUpdate(CallLogEntry callLogEntry) {
    }

    @Override
    public void onRemove(CallLogEntry callLogEntry) {
        this.post(() -> {
            adapter.remove(callLogEntry);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onAdd(final CallLogEntry callLogEntry) {
        this.post(() -> {
            adapter.insert(callLogEntry, 0);
            adapter.notifyDataSetChanged();
        });

    }

    @Override
    public void onStoreRefreshed() {
        reload();
    }

    public void reload(){
        final List<CallLogEntry> callLogEntries = CallLogDataStore.getRecent100CallLogEntries(context);
        this.post(() -> {
            adapter.clear();
            adapter.addAll(callLogEntries);
            adapter.notifyDataSetChanged();
        });
    }

    public void onDestroy(){
        CallLogDataStore.removeDataChangeListener(this);
    }
}
