package opencontacts.open.com.opencontacts;

import static android.content.Context.MODE_PRIVATE;
import static android.graphics.Color.TRANSPARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static opencontacts.open.com.opencontacts.activities.CallLogGroupDetailsActivity.getIntentToShowCallLogEntries;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.dpToPixels;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getASpaceOfHeight;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getThemeAttributeColor;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.handleLongClickWith;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.shareContact;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.getTimestampPattern;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.shareContactAsText;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.PREFTIMEFORMAT_12_HOURS_SHARED_PREF_KEY;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.SOCIAL_INTEGRATION_ENABLED_PREFERENCE_KEY;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.setSharedPreferencesChangeListener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.CallLog;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.underscore.Consumer;
import com.github.underscore.U;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import opencontacts.open.com.opencontacts.components.ImageButtonWithTint;
import opencontacts.open.com.opencontacts.components.TintedDrawablesStore;
import opencontacts.open.com.opencontacts.data.datastore.CallLogDataStore;
import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.domain.GroupedCallLogEntry;
import opencontacts.open.com.opencontacts.interfaces.DataStoreChangeListener;
import opencontacts.open.com.opencontacts.interfaces.EditNumberBeforeCallHandler;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.CallLogGroupingUtil;
import opencontacts.open.com.opencontacts.utils.Common;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

/**
 * Created by sultanm on 7/31/17.
 */

public class CallLogListView extends RelativeLayout implements DataStoreChangeListener<CallLogEntry> {
    private boolean inSelectionMode;
    private String UNKNOWN;
    Context context;
    private EditNumberBeforeCallHandler editNumberBeforeCallHandler;
    ArrayAdapter<GroupedCallLogEntry> adapter;
    private boolean isSocialAppIntegrationEnabled;
    //android has weakref to this listener and gets garbage collected hence we should have it here.
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    private SimpleDateFormat timeStampFormat;
    private LinkedHashMap<String, Consumer<GroupedCallLogEntry>> longClickOptionsAndTheirActions;
    private ListView listView;
    private HashSet<GroupedCallLogEntry> selectedEntries;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Runnable onEnteringMultiSelectMode;
    private Runnable onExitingMultiSelectMode;


    public CallLogListView(final Context context, EditNumberBeforeCallHandler editNumberBeforeCallHandler) {
        super(context);
        this.context = context;
        this.UNKNOWN = context.getString(R.string.unknown);
        this.editNumberBeforeCallHandler = editNumberBeforeCallHandler;
        listView = new ListView(context);
        listView.setId(android.R.id.list);
        listView.setFastScrollEnabled(true);
        addView(getSwipeRefreshLayout(context));
        prepareLongClickActions();
        Log.i("G&S","Modificato");
        boolean shouldToggleContactActions = getBoolean(SharedPreferencesUtils.TOGGLE_CONTACT_ACTIONS, false, context);
        Log.i("G&S","Modificato");Log.i("G&S","Modificato2");
        isSocialAppIntegrationEnabled = context.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE)
            .getBoolean(SOCIAL_INTEGRATION_ENABLED_PREFERENCE_KEY, false);
        timeStampFormat = getTimestampPattern(context);
        List<CallLogEntry> callLogEntries = new ArrayList<>();
        inSelectionMode = false;

        final OnClickListener callContact = v -> {
            if (inSelectionMode) return;
            CallLogEntry callLogEntry = getLatestCallLogEntry((View) v.getParent());
            Log.i("G&S","Modificato");
            AndroidUtils.call(callLogEntry.phoneNumber, context);
        };

        final OnClickListener socialAppContact = v -> {
            if (inSelectionMode) return;
            CallLogEntry callLogEntry = getLatestCallLogEntry((View) v.getParent());
            Log.i("G&S","Modificato");
            AndroidUtils.openSocialApp(callLogEntry.phoneNumber, context);
        };

        final OnLongClickListener socialAppLongClick = v -> {
            if (inSelectionMode) return false;
            CallLogEntry callLogEntry = getLatestCallLogEntry((View) v.getParent());
            Log.i("G&S","Modificato");
            AndroidUtils.onSocialLongPress(callLogEntry.phoneNumber, context);
            return true;
        };

        final OnClickListener messageContact = v -> {
            if (inSelectionMode) return;
            CallLogEntry callLogEntry = getLatestCallLogEntry((View) v.getParent());
            Log.i("G&S","Modificato");
            AndroidUtils.message(callLogEntry.phoneNumber, context);
        };

        final OnClickListener selectionModeTap = v -> {
            GroupedCallLogEntry groupedCallLogEntry = ((GroupedCallLogEntry) v.getTag());
            if (selectedEntries.contains(groupedCallLogEntry))
                selectedEntries.remove(groupedCallLogEntry);
            else selectedEntries.add(groupedCallLogEntry);
            if (selectedEntries.isEmpty()) exitSelectionMode();
            else adapter.notifyDataSetChanged();
        };

        final OnClickListener showContactDetails = v -> {
            CallLogEntry callLogEntry = getLatestCallLogEntry(v);
            Log.i("G&S","Modificato");
            long contactId = callLogEntry.contactId;
            if (contactId == -1)
                return;
            Contact contact = ContactsDataStore.getContactWithId(contactId);
            if (contact == null)
                return;
            Intent showContactDetails1 = AndroidUtils.getIntentToShowContactDetails(contactId, CallLogListView.this.context);
            context.startActivity(showContactDetails1);
        };

        final OnLongClickListener callLogEntryLongClickListener = v -> {
            GroupedCallLogEntry groupedCallLogEntry = (GroupedCallLogEntry) v.getTag();
            CallLogEntry callLogEntry = groupedCallLogEntry.latestCallLogEntry;
            List<String> longClickOptions = new ArrayList<>(Arrays.asList(longClickOptionsAndTheirActions.keySet().toArray(new String[0])));
            if (callLogEntry.contactId != -1) {
                longClickOptions.remove(context.getString(R.string.add_contact));
            }
            else {
                longClickOptions.remove(context.getString(R.string.share_menu_item));
                longClickOptions.remove(context.getString(R.string.share_as_text));
            }
            String[] dynamicListOfLongClickActions = longClickOptions.toArray(new String[0]);
            handleLongClickWith(longClickOptionsAndTheirActions, dynamicListOfLongClickActions, groupedCallLogEntry, context);
            return true;
        };

        adapter = new ArrayAdapter<GroupedCallLogEntry>(CallLogListView.this.context, R.layout.grouped_call_log_entry, CallLogGroupingUtil.group(callLogEntries)) {
            private LayoutInflater layoutInflater = LayoutInflater.from(CallLogListView.this.context);

            @NonNull
            @Override
            public View getView(int position, View reusableView, ViewGroup parent) {
                GroupedCallLogEntry groupedCallLogEntry = getItem(position);
                CallLogEntry callLogEntry = groupedCallLogEntry.latestCallLogEntry;
                if (reusableView == null)
                    reusableView = layoutInflater.inflate(R.layout.grouped_call_log_entry, parent, false);
                Log.i("G&S","Modificato");
                Log.i("G&S","Modificato");
                Log.i("G&S","Modificato");
                ((TextView) reusableView.findViewById(R.id.textview_full_name)).setText(callLogEntry.contactId == -1 ? UNKNOWN : callLogEntry.name);
                ((TextView) reusableView.findViewById(R.id.textview_phone_number)).setText(callLogEntry.phoneNumber);
                setCallAndMessageActions(reusableView);

                View socialAppIcon = reusableView.findViewById(R.id.button_social);
                if (isSocialAppIntegrationEnabled) {
                    socialAppIcon.setOnClickListener(socialAppContact);
                    socialAppIcon.setOnLongClickListener(socialAppLongClick);
                    socialAppIcon.setVisibility(VISIBLE);
                } else socialAppIcon.setVisibility(GONE);
                Log.i("G&S","Modificato");
                if (callLogEntry.callType.equals(String.valueOf(CallLog.Calls.INCOMING_TYPE)))
                    ((ImageView) reusableView.findViewById(R.id.image_view_call_type)).setImageResource(R.drawable.ic_call_received_black_24dp);
                else if (callLogEntry.callType.equals(String.valueOf(CallLog.Calls.OUTGOING_TYPE)))
                    ((ImageView) reusableView.findViewById(R.id.image_view_call_type)).setImageResource(R.drawable.ic_call_made_black_24dp);
                else if (callLogEntry.callType.equals(String.valueOf(CallLog.Calls.MISSED_TYPE)))
                    ((ImageView) reusableView.findViewById(R.id.image_view_call_type)).setImageResource(R.drawable.ic_call_missed_outgoing_black_24dp);
                Log.i("G&S","Modificato");
                ((TextView) reusableView.findViewById(R.id.text_view_duration)).setText(Common.getDurationInMinsAndSecs(Integer.valueOf(callLogEntry.duration)));
                Log.i("G&S","Modificato");
                ((TextView) reusableView.findViewById(R.id.text_view_sim)).setText(String.valueOf(callLogEntry.simId));
                Log.i("G&S","Modificato");
                String timeStampOfCall = timeStampFormat.format(new Date(Long.parseLong(callLogEntry.date)));
                ((TextView) reusableView.findViewById(R.id.text_view_timestamp)).setText(timeStampOfCall);

                List<CallLogEntry> callLogEntriesInGroup = groupedCallLogEntry.callLogEntries;
                AppCompatTextView callRepeatCount = reusableView.findViewById(R.id.call_repeat_count);
                int groupSize = callLogEntriesInGroup.size();
                if (groupSize == 1) callRepeatCount.setVisibility(GONE);
                else {
                    callRepeatCount.setText(context.getString(R.string.call_repeat_text, groupSize));
                    callRepeatCount.setVisibility(VISIBLE);
                }

                reusableView.setTag(groupedCallLogEntry);
                if (inSelectionMode) {
                    reusableView.setOnClickListener(selectionModeTap);
                    reusableView.setOnLongClickListener(null);
                    if (selectedEntries.contains(groupedCallLogEntry)) {
                        Log.i("G&S","Modificato");
                        reusableView.setBackgroundColor(getThemeAttributeColor(android.R.attr.colorMultiSelectHighlight, context));
                    }
                    else reusableView.setBackgroundColor(TRANSPARENT);
                } else {
                    reusableView.setOnClickListener(showContactDetails);
                    reusableView.setBackgroundColor(TRANSPARENT);
                    reusableView.setOnLongClickListener(callLogEntryLongClickListener);
                }
                return reusableView;
            }

            private void setCallAndMessageActions(View reusableView) {
                ImageButtonWithTint actionButton1 = reusableView.findViewById(R.id.button_action1);
                ImageButtonWithTint actionButton2 = reusableView.findViewById(R.id.button_action2);
                if (shouldToggleContactActions) {
                    actionButton1.setOnClickListener(messageContact);
                    actionButton1.setImageResource(R.drawable.ic_chat_black_24dp);
                    actionButton2.setOnClickListener(callContact);
                    actionButton2.setImageResource(R.drawable.ic_call_black_24dp);
                } else {
                    actionButton1.setOnClickListener(callContact);
                    actionButton1.setImageResource(R.drawable.ic_call_black_24dp);
                    actionButton2.setOnClickListener(messageContact);
                    actionButton2.setImageResource(R.drawable.ic_chat_black_24dp);
                }
            }
        };
        listView.setAdapter(adapter);
        CallLogDataStore.addDataChangeListener(this);
        reload();
        //android has weakref to this listener and gets garbage collected hence we should have it here.
        sharedPreferenceChangeListener = (sharedPreferences, key) -> {
            if (!SOCIAL_INTEGRATION_ENABLED_PREFERENCE_KEY.equals(key)
                && !PREFTIMEFORMAT_12_HOURS_SHARED_PREF_KEY.equals(key)
            ) return;
            Log.i("G&S","Modificato");Log.i("G&S","Modificato2");
            isSocialAppIntegrationEnabled =context.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE)
                .getBoolean(SOCIAL_INTEGRATION_ENABLED_PREFERENCE_KEY, false);
            timeStampFormat = getTimestampPattern(context);
            adapter.notifyDataSetChanged();
        };
        setSharedPreferencesChangeListener(sharedPreferenceChangeListener, context);
        listView.addFooterView(getFooterView());
    }

    private View getFooterView() {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(getViewMoreButton());
        linearLayout.addView(getASpaceOfHeight(10, 56, context)); //56 is height of bottom menu, 10 is arbitrary
        return linearLayout;
    }

    @NonNull
    private SwipeRefreshLayout getSwipeRefreshLayout(Context context) {
        swipeRefreshLayout = new SwipeRefreshLayout(context);
        swipeRefreshLayout.addView(listView);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (listView.getCount() == 0)
                reload();
            else
                CallLogDataStore.loadRecentCallLogEntriesAsync(context);
            swipeRefreshLayout.setRefreshing(false);
        });
        return swipeRefreshLayout;
    }

    @NonNull
    private AppCompatButton getViewMoreButton() {
        AppCompatButton viewMoreButton = new AppCompatButton(getContext());
        viewMoreButton.setText(R.string.view_more);
        viewMoreButton.setOnClickListener(v -> CallLogDataStore.loadNextChunkOfCallLogEntries());
        return viewMoreButton;
    }

    private void prepareLongClickActions() {
        longClickOptionsAndTheirActions = new LinkedHashMap<>();
        longClickOptionsAndTheirActions.put(context.getString(R.string.delete_multiple), groupedCallLogEntry -> {
            enterSelectionMode();
            selectedEntries.add(groupedCallLogEntry);
            adapter.notifyDataSetChanged();
        });
        Log.i("G&S","Modificato");
        longClickOptionsAndTheirActions.put(context.getString(R.string.copy_to_clipboard), groupedCallLogEntry -> {
            AndroidUtils.copyToClipboard(groupedCallLogEntry.latestCallLogEntry.phoneNumber, context);
            Toast.makeText(context, R.string.copied_phonenumber_to_clipboard, Toast.LENGTH_SHORT).show();
        });
        Log.i("G&S","Modificato");
        longClickOptionsAndTheirActions.put(context.getString(R.string.add_contact), groupedCallLogEntry -> {
            AndroidUtils.getAlertDialogToAddContact(groupedCallLogEntry.latestCallLogEntry.phoneNumber, context).show();
        });
        Log.i("G&S","Modificato");
        longClickOptionsAndTheirActions.put(context.getString(R.string.edit_before_call), groupedCallLogEntry -> {
            this.editNumberBeforeCallHandler.setNumber(groupedCallLogEntry.latestCallLogEntry.phoneNumber);
        });
        longClickOptionsAndTheirActions.put(context.getString(R.string.delete), groupedCallLogEntry -> {
            CallLogDataStore.delete(groupedCallLogEntry.latestCallLogEntry.getId());
        });
        Log.i("G&S","Modificato");
        longClickOptionsAndTheirActions.put(context.getString(R.string.show_details), groupedCallLogEntry -> {
            context.startActivity(getIntentToShowCallLogEntries(groupedCallLogEntry.latestCallLogEntry.phoneNumber, context));
        });
        longClickOptionsAndTheirActions.put(context.getString(R.string.share_menu_item), groupedCallLogEntry -> {
            shareContact(groupedCallLogEntry.latestCallLogEntry.contactId, context);
        });
        longClickOptionsAndTheirActions.put(context.getString(R.string.share_as_text), groupedCallLogEntry -> {
            shareContactAsText(groupedCallLogEntry.latestCallLogEntry.contactId, context);
        });
    }

    private CallLogEntry getLatestCallLogEntry(View v) {
        return ((GroupedCallLogEntry) v.getTag()).latestCallLogEntry;
    }

    @Override
    public void onUpdate(CallLogEntry callLogEntry) {
        reload();
    }

    @Override
    public void onRemove(CallLogEntry callLogEntry) {
        reload();
    }

    @Override
    public void onAdd(final CallLogEntry callLogEntry) {
        reload();
    }

    @Override
    public void onStoreRefreshed() {
        reload();
    }

    public void reload() {
        final List<GroupedCallLogEntry> groupedCallLogEntries = CallLogGroupingUtil.group(CallLogDataStore.getRecentCallLogEntries(context));
        this.post(() -> {
            adapter.clear();
            adapter.addAll(groupedCallLogEntries);
            adapter.notifyDataSetChanged();
        });
    }

    public void onDestroy() {
        CallLogDataStore.removeDataChangeListener(this);
    }

    public void setEditNumberBeforeCallHandler(EditNumberBeforeCallHandler editNumberBeforeCallHandler) {
        this.editNumberBeforeCallHandler = editNumberBeforeCallHandler;
    }

    public void enterSelectionMode() {
        inSelectionMode = true;
        if (selectedEntries == null) selectedEntries = new HashSet<>(0);
        else selectedEntries.clear();
        addDeleteFABButton();
        swipeRefreshLayout.setEnabled(false);
        if (onEnteringMultiSelectMode != null) onEnteringMultiSelectMode.run();
    }

    public void exitSelectionMode() {
        if (!inSelectionMode) return;
        inSelectionMode = false;
        removeView(findViewById(R.id.delete));
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setEnabled(true);
        if (onExitingMultiSelectMode != null) onExitingMultiSelectMode.run();
    }

    private void addDeleteFABButton() {
        FloatingActionButton deleteMultipleContacts = new FloatingActionButton(getContext());
        deleteMultipleContacts.setImageDrawable(TintedDrawablesStore.getTintedDrawable(R.drawable.delete, getContext()));
        deleteMultipleContacts.setId(R.id.delete);
        RelativeLayout.LayoutParams deleteFABLayoutParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        deleteFABLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        deleteFABLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        deleteFABLayoutParams.rightMargin = (int) dpToPixels(12);
        addView(deleteMultipleContacts, deleteFABLayoutParams);
        deleteMultipleContacts.setTranslationY(-dpToPixels(16)); //plain old relative layout align parent bottom and bottom margin
        deleteMultipleContacts.setOnClickListener(v -> {
            deleteSelection();
            exitSelectionMode();
        });
    }

    private void deleteSelection() {
        List<CallLogEntry> individualCallLogEntries = U.chain(selectedEntries)
            .map(groupedCallLogEntry -> groupedCallLogEntry.callLogEntries)
            .flatten()
            .value();
        CallLogDataStore.deleteCallLogEntries(individualCallLogEntries);
        selectedEntries.clear();
    }

    public void setOnEnteringMultiSelectMode(Runnable onEnteringMultiSelectMode) {
        this.onEnteringMultiSelectMode = onEnteringMultiSelectMode;
    }

    public void setOnExitingMultiSelectMode(Runnable onExitingMultiSelectMode) {
        this.onExitingMultiSelectMode = onExitingMultiSelectMode;
    }

    public boolean isInSelectionMode() {
        return inSelectionMode;
    }

}
