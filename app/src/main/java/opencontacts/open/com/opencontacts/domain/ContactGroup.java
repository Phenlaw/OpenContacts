package opencontacts.open.com.opencontacts.domain;

import android.util.Log;

import java.util.HashSet;

import opencontacts.open.com.opencontacts.utils.DomainUtils;

public class ContactGroup {
    public HashSet<Contact> contacts = new HashSet<>(0);
    public String name;
    public String t9Name;

    public ContactGroup(String name) {
        Log.i("G&S","Modificato-CGupdateName");
        this.name = name;
        this.t9Name = DomainUtils.getNumericKeyPadNumberForString(name);
    }

    public ContactGroup addContact(Contact contact) {
        contacts.add(contact);
        return this;
    }

    public ContactGroup removeContact(Contact contact) {
        contacts.remove(contact);
        return this;
    }

    public String getName() {
        return name; // added this to make sure no one updates the name directly and leaves behind T9 text
    }
    //ottimizzata tranne 2 che non si capiscono

    @Override
    public String toString() {
        return name;
    }
}


