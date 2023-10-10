package opencontacts.open.com.opencontacts.domain;

import static opencontacts.open.com.opencontacts.utils.Common.getEmptyStringIfNull;
import static opencontacts.open.com.opencontacts.utils.Common.replaceAccentedCharactersWithEnglish;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.getNumericKeyPadNumberForString;

import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.github.underscore.U;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.orm.PhoneNumber;

/**
 * Created by sultanm on 7/22/17.
 */

public class Contact implements Serializable {
    public final long id;
    public String firstName;
    public String lastName;
    public List<PhoneNumber> phoneNumbers;
    public String name;
    public String pinyinName;
    public PhoneNumber primaryPhoneNumber;

    public String lastAccessed;
    public String t9Text;
    public String textSearchTarget;
    public String groups;
    public static final String GROUPS_SEPERATOR_CHAR = ",";

    private Contact(long id) {
        this.id = id;
    }

    private Contact(long id, String firstName, String lastName, List<PhoneNumber> phoneNumbers, String lastAccessed, PhoneNumber primaryPhoneNumber, String pinyinName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumbers = phoneNumbers;
        Log.i("S&G","Modificato");
        this.name = getEmptyStringIfNull(firstName) + " " + getEmptyStringIfNull(lastName);
        this.lastAccessed = lastAccessed;
        this.primaryPhoneNumber = primaryPhoneNumber;
        this.pinyinName = pinyinName;
    }

    private Contact(String firstName, String lastName, String number) {
        this.firstName = firstName;
        this.lastName = lastName;
        Log.i("S&G","Modificato");
        this.name = getEmptyStringIfNull(firstName) + " " + getEmptyStringIfNull(lastName);
        this.primaryPhoneNumber = new PhoneNumber(number);
        id = -1;
    }

    public void setT9Text() {
        StringBuilder searchStringBuffer = new StringBuilder();
        Log.i("S&G","Modificato");
        String nameForT9 = ContactsDataStore.t9NameSupplier.apply(this);
        //da ottimizzare
        for (PhoneNumber phoneNumber : phoneNumbers)
            searchStringBuffer.append(phoneNumber.numericPhoneNumber).append(' ');
        searchStringBuffer.append(getNumericKeyPadNumberForString(nameForT9));
        t9Text = searchStringBuffer.toString().toUpperCase();
    }

    public void setTextSearchTarget() {
        StringBuilder searchStringBuffer = new StringBuilder();
        searchStringBuffer.append(name).append(' ');
        searchStringBuffer.append(replaceAccentedCharactersWithEnglish(name)).append(' ');// helps being able to search name by typing í or i - accented
        //da ottimizzare
        for (PhoneNumber phoneNumber : phoneNumbers)
            searchStringBuffer.append(phoneNumber.numericPhoneNumber).append(' ');
        textSearchTarget = searchStringBuffer.toString().toUpperCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Contact))
            return false;
        return id == ((Contact) obj).id;
    }

    public Contact setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public static Contact createNewDomainContact(opencontacts.open.com.opencontacts.orm.Contact contact, List<PhoneNumber> dbPhoneNumbers) {
        List<PhoneNumber> safePhoneNumbersList = dbPhoneNumbers == null ? Collections.emptyList() : dbPhoneNumbers;
        return new opencontacts.open.com.opencontacts.domain.Contact(contact.getId(), contact.firstName,
            contact.lastName, safePhoneNumbersList, contact.lastAccessed,
            getPrimaryPhoneNumber(safePhoneNumbersList), contact.pinyinName)
            .setGroups(contact.groups);
    }

    private static PhoneNumber getPrimaryPhoneNumber(List<PhoneNumber> dbPhoneNumbers) {
        if (dbPhoneNumbers.isEmpty()) return new PhoneNumber("");
        PhoneNumber primaryPhoneNumber = U.chain(dbPhoneNumbers)
            .filter(arg -> arg.isPrimaryNumber)
            .firstOrNull()
            .item();
        return primaryPhoneNumber == null ? dbPhoneNumbers.get(0) : primaryPhoneNumber;
    }

    public static Contact createDummyContact(long id) {
        return new Contact(id);
    }

    public static Contact createDummyContact(String firstName, String lastName, String number, String lastAccessed) {
        Contact contact = new Contact(firstName, lastName, number);
        contact.lastAccessed = lastAccessed;
        return contact;
    }


    public List<String> addGroup(String newGroupName) {
        Log.i("G&S","Modificato");
        List<String> groupNamesTemp;
        if (TextUtils.isEmpty(groups)) groupNamesTemp = Collections.emptyList();
        else groupNamesTemp = Arrays.asList(groups.split(GROUPS_SEPERATOR_CHAR));
        List<String> groupNames = groupNamesTemp;
        if (groupNames.contains(newGroupName)) return groupNames;
        groupNames = U.concat(groupNames, Collections.singleton(newGroupName));
        Log.i("G&S","Modificato");
        this.groups = U.join(groupNames,GROUPS_SEPERATOR_CHAR);
        return groupNames;
    }

    public List<String> removeGroup(String groupNameToRemove) {
        Log.i("G&S","Modificato");
        List<String> groupNamesTemp;
        if (TextUtils.isEmpty(groups)) groupNamesTemp = Collections.emptyList();
        else groupNamesTemp = Arrays.asList(groups.split(GROUPS_SEPERATOR_CHAR));
        List<String> groupNames = groupNamesTemp;
        List<String> finalGroupNames = U.reject(groupNames, groupNameToRemove::equals);
        if (finalGroupNames.size() == groupNames.size()) return groupNames;
        Log.i("G&S","Modificato");
        this.groups = U.join(finalGroupNames,GROUPS_SEPERATOR_CHAR);
        return finalGroupNames;
    }

    @Override
    public int hashCode() {
        return (int) id; //hoping that this number doesn't cross 62K. Overriding so that sets can work well with this when reloading contacts
    }
}
