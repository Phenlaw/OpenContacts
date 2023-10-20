package opencontacts.open.com.opencontacts.data.datastore;


import static ezvcard.Ezvcard.write;
import static opencontacts.open.com.opencontacts.domain.Contact.GROUPS_SEPERATOR_CHAR;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.processAsync;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.toastFromNonUIThread;
import static opencontacts.open.com.opencontacts.utils.Common.getOrDefault;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.github.underscore.lodash.U;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.domain.ContactGroup;
import opencontacts.open.com.opencontacts.orm.VCardData;
import opencontacts.open.com.opencontacts.utils.DomainUtils;
import opencontacts.open.com.opencontacts.utils.VCardUtils;

public class ContactGroupsDataStore {

    public static Map<String, ContactGroup> groupsMap = new HashMap<>();
    private static boolean init = false;

    public static void COMPUTE_INTENSIVE_computeGroups() {
        List<Contact> allContacts = ContactsDataStore.getAllContacts();
        groupsMap = new HashMap<>();
        Log.i("FOR","Modificato");
        int contactsSize = allContacts.size();
        for(int i=0;i<contactsSize;i++){
            Contact contact = allContacts.get(i);
            Log.i("G&S","Modificato");
            List<String> groupNames;
            if (TextUtils.isEmpty(contact.groups)) groupNames = Collections.emptyList();
            else groupNames = Arrays.asList(contact.groups.split(GROUPS_SEPERATOR_CHAR));
            U.chain(groupNames)
                .map(groupName -> getOrDefault(groupsMap, groupName, new ContactGroup(groupName)))
                .map(group -> group.addContact(contact))
                .forEach(group -> {
                    Log.i("G&S","Modificato");
                    groupsMap.put(group.name, group);
                });
        }
    }


    public static void initInCaseHasNot() {
        if (init) return;
        computeGroupsAsync();
        init = true;
    }

    public static void invalidateGroups() {
        init = false;
        groupsMap = new HashMap<>(0);
    }

    public static void computeGroupsAsync() {
        processAsync(ContactGroupsDataStore::COMPUTE_INTENSIVE_computeGroups);
    }

    public static List<ContactGroup> getAllGroups() {
        return new ArrayList<>(groupsMap.values());
    }


    public static void createNewGroup(List<Contact> contacts, String groupName) {
        ContactGroup newContactGroup = new ContactGroup(groupName);
        groupsMap.put(groupName, newContactGroup);
        Log.i("FOR","Modificato");
        int contactsSize = contacts.size();
        for(int i=0;i<contactsSize;i++) addContactToGroup(newContactGroup,contacts.get(i));
    }

    public static void updateGroup(List<Contact> newContacts, String newGroupName, ContactGroup group) {
        Log.i("G&S","Modificato");
        if (!newGroupName.equals(group.name)) {
            destroyGroup(group);
            createNewGroup(newContacts, newGroupName);
            return;
        }
        Collection<Contact> removedContacts = U.reject(group.contacts, newContacts::contains);
        //Non ottimizzare perchè non è ArrayList
        U.forEach(removedContacts, removedContact -> removeContactFromGroup(group, removedContact));

        //removing based on group name hence it should happen first
        Log.i("G&S","Modificato");
        group.name = newGroupName;
        group.t9Name = DomainUtils.getNumericKeyPadNumberForString(newGroupName);


        Collection<Contact> onlyNewContacts = U.reject(newContacts, group.contacts::contains);
        //Non ottimizzare perchè non è ArrayList
        U.forEach(onlyNewContacts, newContact -> addContactToGroup(group, newContact));
    }

    private static void destroyGroup(ContactGroup group) {
        //new array list coz of concurrent modification of same array group.contacts
        U.chain(group.contacts)
            .forEach(contact -> removeContactFromGroup(group, contact));
        Log.i("G&S","Modificato");
        groupsMap.remove(group.name);
    }

    private static void addContactToGroup(ContactGroup group, Contact contact) {
        group.addContact(contact);
        Log.i("G&S","Modificato");
        List<String> allGroupsOfContact = contact.addGroup(group.name);
        updateContactTable(contact);
        updateVCardTable(contact, allGroupsOfContact);
    }

    private static void removeContactFromGroup(ContactGroup group, Contact contact) {
        group.removeContact(contact);
        Log.i("G&S","Modificato");
        List<String> allGroupsOfContact = contact.removeGroup(group.name);
        updateContactTable(contact);
        updateVCardTable(contact, allGroupsOfContact);
    }

    private static void updateVCardTable(Contact contact, List<String> allGroupsOfContact) {
        try {
            VCardData vCardData = ContactsDBHelper.getVCard(contact.id);
            Log.i("G&S","Modificato");
            VCard vcard = new VCardReader(vCardData.vcardDataAsString).readNext();
            vcard.setCategories(allGroupsOfContact.toArray(new String[]{}));
            Log.i("G&S","Modificato");
            vCardData.vcardDataAsString = write(vcard).caretEncoding(true).go();
            vCardData.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateContactTable(Contact contact) {
        opencontacts.open.com.opencontacts.orm.Contact dbContact = ContactsDBHelper.getDBContactWithId(contact.id);
        dbContact.groups = contact.groups;
        dbContact.save();
    }


    public static void handleContactDeletion(Contact contact) {
        U.chain(groupsMap.values())
            .map(group -> group.contacts)
            .forEach(contactsList -> contactsList.remove(contact));
    }

    public static void handleContactUpdate(Contact contact) {
        Log.i("G&S","Modificato");
        List<String> groupNames;
        if (TextUtils.isEmpty(contact.groups)) groupNames = Collections.emptyList();
        else groupNames = Arrays.asList(contact.groups.split(GROUPS_SEPERATOR_CHAR));
        List<String> newGroupAssociations = groupNames;
        //Non ottimizzare perchè non è ArrayList
        U.forEach(groupsMap.values(), group -> {
            Log.i("G&S","Modificato");
            if (newGroupAssociations.contains(group.name)) group.addContact(contact);
            else group.removeContact(contact);
        });
    }

    public static void handleNewContactAddition(Contact contact) {
        Log.i("G&S","Modificato");
        List<String> groupNames;
        if (TextUtils.isEmpty(contact.groups)) groupNames = Collections.emptyList();
        else groupNames = Arrays.asList(contact.groups.split(GROUPS_SEPERATOR_CHAR));
        List<String> groupAssociations = groupNames;
        if (groupAssociations.isEmpty()) return;
        Log.i("G&S","Modificato");
        U.chain(groupsMap.values())
            .filter(group -> groupAssociations.contains(group.name))
            .forEach(group -> group.addContact(contact));
    }

    public static void PROCESS_INTENSIVE_delete(ContactGroup selectedGroup, Context context) {
        Log.i("FOR","Modificato");
        ArrayList<Contact> selectedContactsfromSelectedGroup = new ArrayList<>(selectedGroup.contacts);
        int size = selectedContactsfromSelectedGroup.size();
        for(int i =0;i<size; i++) removeContactFromGroup(selectedGroup,selectedContactsfromSelectedGroup.get(i));
        Log.i("G&S","Modificato");
        groupsMap.remove(selectedGroup.name);
        toastFromNonUIThread(R.string.group_deleted, Toast.LENGTH_SHORT, context);
    }
}

