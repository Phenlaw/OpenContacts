package opencontacts.open.com.opencontacts;

import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.DomainUtils;

public class ContactsListT9Filter extends ContactsListFilter {
    public ContactsListT9Filter(ArrayAdapter<Contact> adapter, AllContactsHolder allContactsHolder) {
        super(adapter, allContactsHolder);
    }

    public void updateMap(Contact contact) {
        contact.setT9Text();
    }

    public void createDataMapping(List<Contact> contacts) {
        List<Contact> threadSafeContacts = new ArrayList<>(contacts);
        Log.i("FOR","Modificato-CLT9FcreateDataMapping1");
        int threadSafeContactsSize = threadSafeContacts.size();
        for (int i =0;i<threadSafeContactsSize;i++) {
            threadSafeContacts.get(i).setT9Text();
        }
    }

    public List<Contact> filter(CharSequence t9Text, List<Contact> contacts) {
        return DomainUtils.filterContactsBasedOnT9Text(t9Text, contacts);
    }
}
