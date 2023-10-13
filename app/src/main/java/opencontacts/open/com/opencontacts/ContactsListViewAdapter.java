package opencontacts.open.com.opencontacts;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.OnLongClickListener;
import static android.view.View.VISIBLE;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;
import static opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils.SOCIAL_INTEGRATION_ENABLED_PREFERENCE_KEY;

import android.content.Context;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

import opencontacts.open.com.opencontacts.components.ImageButtonWithTint;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

public class ContactsListViewAdapter extends ArrayAdapter<Contact> {
    private boolean shouldToggleContactActions;
    private ContactsListActionsListener contactsListActionsListener;
    private LayoutInflater layoutInflater;
    public ContactsListFilter contactsListFilter;
    private boolean socialAppIntegrationEnabled;

    public ContactsListViewAdapter(@NonNull Context context, int resource, ContactsListFilter.AllContactsHolder allContactsHolder) {
        super(context, resource, new ArrayList<>(allContactsHolder.getContacts()));
        init(context);
        createContactsListFilter(allContactsHolder);
    }

    public ContactsListViewAdapter(@NonNull Context context) {
        super(context, R.layout.contact, new ArrayList<>());
        init(context);
    }

    private void init(@NonNull Context context) {
        layoutInflater = LayoutInflater.from(context);
        Log.i("G&S","Modificato");Log.i("G&S","Modificato2");
        socialAppIntegrationEnabled = context.getSharedPreferences(SharedPreferencesUtils.COMMON_SHARED_PREFS_FILE_NAME, MODE_PRIVATE)
            .getBoolean(SOCIAL_INTEGRATION_ENABLED_PREFERENCE_KEY, false);
        Log.i("G&S","Modificato");
        shouldToggleContactActions = getBoolean(SharedPreferencesUtils.TOGGLE_CONTACT_ACTIONS, false, context);
    }

    public void createContactsListFilter(ContactsListFilter.AllContactsHolder allContactsHolder) {
        Log.i("G&S","Modificato");
        contactsListFilter = getBoolean(SharedPreferencesUtils.T9_SEARCH_ENABLED_SHARED_PREF_KEY, true, getContext()) ? new ContactsListT9Filter(this, allContactsHolder)
            : new ContactsListTextFilter(this, allContactsHolder);
    }

    private final OnLongClickListener onLongClicked = v -> {
        if (contactsListActionsListener == null)
            return false;
        Contact contact = (Contact) v.getTag();
        contactsListActionsListener.onLongClick(contact);
        return true;
    };

    private final OnClickListener callContact = v -> {
        if (contactsListActionsListener == null)
            return;
        Contact contact = (Contact) ((View) v.getParent()).getTag();
        contactsListActionsListener.onCallClicked(contact);
    };
    private final OnClickListener messageContact = v -> {
        if (contactsListActionsListener == null)
            return;
        Contact contact = (Contact) ((View) v.getParent()).getTag();
        contactsListActionsListener.onMessageClicked(contact);
    };
    private final OnClickListener showContactDetails = v -> {
        if (contactsListActionsListener == null)
            return;
        Contact contact = (Contact) v.getTag();
        contactsListActionsListener.onShowDetails(contact);
    };
    private final OnClickListener openSocialApp = v -> {
        if (contactsListActionsListener == null)
            return;
        Contact contact = (Contact) ((View) v.getParent()).getTag();
        contactsListActionsListener.onSocialAppClicked(contact);
    };
    private final OnLongClickListener socialLongClicked = v -> {
        if (contactsListActionsListener == null)
            return false;
        Contact contact = (Contact) ((View) v.getParent()).getTag();
        contactsListActionsListener.onSocialLongClicked(contact);
        return true;
    };

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = getItem(position);
        if (convertView == null)
            convertView = layoutInflater.inflate(R.layout.contact, parent, false);
        ((TextView) convertView.findViewById(R.id.textview_full_name)).setText(contact.name);
        ((TextView) convertView.findViewById(R.id.textview_phone_number)).setText(contact.primaryPhoneNumber.phoneNumber);
        ImageButtonWithTint actionButton1 = convertView.findViewById(R.id.button_action1);
        ImageButtonWithTint actionButton2 = convertView.findViewById(R.id.button_action2);
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
        View socialIcon = convertView.findViewById(R.id.button_social);
        if (socialAppIntegrationEnabled) {
            socialIcon.setOnClickListener(openSocialApp);
            socialIcon.setOnLongClickListener(socialLongClicked);
            socialIcon.setVisibility(VISIBLE);
        } else socialIcon.setVisibility(GONE);
        convertView.setTag(contact);
        convertView.setOnClickListener(showContactDetails);
        convertView.setOnLongClickListener(onLongClicked);
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return contactsListFilter;
    }

    public void setContactsListActionsListener(ContactsListActionsListener contactsListActionsListener) {
        this.contactsListActionsListener = contactsListActionsListener;
    }

    public interface ContactsListActionsListener {
        void onCallClicked(Contact contact);

        void onMessageClicked(Contact contact);

        void onShowDetails(Contact contact);

        void onSocialAppClicked(Contact contact);

        void onSocialLongClicked(Contact contact);

        void onLongClick(Contact contact);
    }
}
