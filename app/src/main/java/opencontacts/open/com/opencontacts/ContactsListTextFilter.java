package opencontacts.open.com.opencontacts;

import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;

public class ContactsListTextFilter extends ContactsListFilter {
    public ContactsListTextFilter(ArrayAdapter<Contact> adapter, AllContactsHolder allContactsHolder) {
        super(adapter, allContactsHolder);
    }

    @Override
    public void updateMap(Contact contact) {
        contact.setTextSearchTarget();
    }

    @Override
    public void createDataMapping(List<Contact> contacts) {
        List<Contact> threadSafeContacts = new ArrayList<>(contacts);
        Log.i("FOR","Modificato");
        int threadSafeContactsSize = threadSafeContacts.size();
        for (int i =0;i<threadSafeContactsSize;i++) {
            threadSafeContacts.get(i).setTextSearchTarget();
        }
    }

    @Override
    public List<Contact> filter(CharSequence searchText, List<Contact> contacts) {
        ArrayList<Contact> filteredContacts = new ArrayList<>();
        Log.i("FOR","Modificato");
        int contactsSize = contacts.size();
        for (int i =0;i<contactsSize;i++) {
            Contact contact = contacts.get(i);
            if (contact.textSearchTarget == null) {
                contact.setTextSearchTarget();
            }
            if (contact.textSearchTarget.contains(searchText.toString().toUpperCase())) {
                filteredContacts.add(contact);
            }
        }
        return filteredContacts;
    }

}
