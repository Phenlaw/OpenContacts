package opencontacts.open.com.opencontacts.data.datastore;

import static android.text.TextUtils.isEmpty;
import static opencontacts.open.com.opencontacts.data.datastore.CallLogDBHelper.getCallLogEntriesFor;
import static opencontacts.open.com.opencontacts.domain.Contact.createNewDomainContact;
import static opencontacts.open.com.opencontacts.domain.Contact.getGroupsNamesCSVString;
import static opencontacts.open.com.opencontacts.orm.PhoneNumber.getMatchingNumbers;
import static opencontacts.open.com.opencontacts.orm.VCardData.STATUS_CREATED;
import static opencontacts.open.com.opencontacts.orm.VCardData.STATUS_DELETED;
import static opencontacts.open.com.opencontacts.orm.VCardData.updateVCardData;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.getPinyinTextFromChinese;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.getSearchablePhoneNumber;
import static opencontacts.open.com.opencontacts.utils.VCardUtils.getCategories;
import static opencontacts.open.com.opencontacts.utils.VCardUtils.getMobileNumber;
import static opencontacts.open.com.opencontacts.utils.VCardUtils.getNameFromVCard;
import static opencontacts.open.com.opencontacts.utils.VCardUtils.isFavorite;
import static opencontacts.open.com.opencontacts.utils.VCardUtils.isPrimaryPhoneNumber;
import static opencontacts.open.com.opencontacts.utils.VCardUtils.markPrimaryPhoneNumberInVCard;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import com.github.underscore.U;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ezvcard.VCard;
import ezvcard.property.Telephone;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.orm.Contact;
import opencontacts.open.com.opencontacts.orm.Favorite;
import opencontacts.open.com.opencontacts.orm.PhoneNumber;
import opencontacts.open.com.opencontacts.orm.TemporaryContact;
import opencontacts.open.com.opencontacts.orm.VCardData;
import opencontacts.open.com.opencontacts.utils.Triplet;
import opencontacts.open.com.opencontacts.utils.VCardUtils;

/**
 * Created by sultanm on 7/17/17.
 */

public class ContactsDBHelper {

    static Contact getDBContactWithId(Long id) {
        return Contact.findById(Contact.class, id);
    }

    static void deleteContactInDB(Long contactId) {
        Contact dbContact = Contact.findById(Contact.class, contactId);
        if (dbContact == null)
            return;
        List<PhoneNumber> dbPhoneNumbers = dbContact.getAllPhoneNumbers();
        Log.i("FOR","Modificato-CDBHdeleteContactInDB1");
        int dbPhoneNumbersSize = dbPhoneNumbers.size();
        for(int i=0;i<dbPhoneNumbersSize;i++) dbPhoneNumbers.get(i).delete();
        List<CallLogEntry> callLogEntries = getCallLogEntriesFor(contactId);
        Log.i("FOR","Modificato-CDBHdeleteContactInDB2");
        int callLogEntriesSize = callLogEntries.size();
        for(int i=0;i<callLogEntriesSize;i++){
            CallLogEntry callLogEntry = callLogEntries.get(i);
            callLogEntry.setId((long) -1);
            callLogEntry.save();
        }
        updateVCardDataForDeletion(VCardData.getVCardData(contactId));
        dbContact.delete();
    }

    private static void updateVCardDataForDeletion(VCardData vCardData) {
        if (vCardData.status == STATUS_CREATED) {
            vCardData.delete();
            return;
        }
        vCardData.status = STATUS_DELETED;
        vCardData.vcardDataAsString = null;
        vCardData.save();
    }

    static Contact getContactFromDB(String phoneNumber) {
        if (isEmpty(phoneNumber)) return null;
        String searchablePhoneNumber = getSearchablePhoneNumber(phoneNumber);
        if (isEmpty(searchablePhoneNumber)) return null;
        List<PhoneNumber> matchingPhoneNumbers = getMatchingNumbers(searchablePhoneNumber);
        if (matchingPhoneNumbers.isEmpty()) return null;
        return matchingPhoneNumbers.get(0).contact;
    }

    static void replacePhoneNumbersInDB(Contact dbContact, VCard vcard, String primaryPhoneNumber) {
        List<PhoneNumber> dbPhoneNumbers = dbContact.getAllPhoneNumbers();
        Log.i("FOR","Modificato-CDBHreplacePhoneNumbersInDB1");
        List<Telephone> vCardTelephoneNumbers = vcard.getTelephoneNumbers();
        int vCardTelephoneNumbersSize = vCardTelephoneNumbers.size();
        for(int i=0;i<vCardTelephoneNumbersSize;i++){
            Telephone telephone = vCardTelephoneNumbers.get(i);
            String phoneNumberText = VCardUtils.getMobileNumber(telephone);
            new PhoneNumber(phoneNumberText, dbContact, primaryPhoneNumber.equals(phoneNumberText)).save();
        }
        PhoneNumber.deleteInTx(dbPhoneNumbers);
    }

    static void updateContactInDBWith(long contactId, String primaryNumber, VCard vCard, Context context) {
        Contact dbContact = ContactsDBHelper.getDBContactWithId(contactId);
        Pair<String, String> nameFromVCard = getNameFromVCard(vCard, context);
        dbContact.firstName = nameFromVCard.first;
        dbContact.lastName = nameFromVCard.second;
        dbContact.groups = getGroupsNamesCSVString(getCategories(vCard));
        dbContact.pinyinName = getPinyinTextFromChinese(dbContact.getFullName());
        dbContact.save();
        replacePhoneNumbersInDB(dbContact, vCard, primaryNumber);
        updateVCardData(vCard, dbContact.getId(), context);
    }

    static List<opencontacts.open.com.opencontacts.domain.Contact> getAllContactsFromDB() {
        List<PhoneNumber> dbPhoneNumbers = PhoneNumber.listAll(PhoneNumber.class);
        HashMap<Long, opencontacts.open.com.opencontacts.domain.Contact> contactsMap = new HashMap<>();
        opencontacts.open.com.opencontacts.domain.Contact tempContact;
        Log.i("FOR","Modificato-CDBHgetAllContactsFromDB1");
        int dbPhoneNumbersSize = dbPhoneNumbers.size();
        for (int i=0;i<dbPhoneNumbersSize;i++) {
            PhoneNumber dbPhoneNumber = dbPhoneNumbers.get(i);
            tempContact = contactsMap.get(dbPhoneNumber.contact.getId());
            if (tempContact == null) {
                tempContact = createNewDomainContact(dbPhoneNumber.contact, Collections.singletonList(dbPhoneNumber));
                contactsMap.put(tempContact.id, tempContact);
            } else {
                tempContact.phoneNumbers = U.concat(tempContact.phoneNumbers, Collections.singletonList(dbPhoneNumber));
                if (dbPhoneNumber.isPrimaryNumber)
                    tempContact.primaryPhoneNumber = dbPhoneNumber;
            }

        }
        //for contacts without phone numbers
        List<PhoneNumber> emptyPhoneNumbersList = Collections.emptyList();
        new U.Chain<>(Contact.listAll(Contact.class))
            .filter(ormContact -> !contactsMap.containsKey(ormContact.getId()))
            .forEach(ormContact -> contactsMap.put(ormContact.getId(), createNewDomainContact(ormContact, emptyPhoneNumbersList)));

        return new ArrayList<>(contactsMap.values());
    }

    static opencontacts.open.com.opencontacts.domain.Contact getContact(long id) {
        if (id == -1)
            return null;
        opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getDBContactWithId(id);
        if (contact == null)
            return null;
        return createNewDomainContact(contact, contact.getAllPhoneNumbers());
    }

    static void togglePrimaryNumber(String mobileNumber, opencontacts.open.com.opencontacts.domain.Contact contact) {
        List<PhoneNumber> allDbPhoneNumbersOfContact = PhoneNumber.find(PhoneNumber.class, "contact = ?", contact.id + "");
        if (allDbPhoneNumbersOfContact == null)
            return;
        Log.i("FOR","Modificato-CDBHtogglePrimaryNumber1");
        int allDbPhoneNumbersOfContactSize = allDbPhoneNumbersOfContact.size();
        for(int i=0;i<allDbPhoneNumbersOfContactSize;i++) {
            PhoneNumber dbPhoneNumber = allDbPhoneNumbersOfContact.get(i);
            if (dbPhoneNumber.phoneNumber.equals(mobileNumber)) {
                dbPhoneNumber.isPrimaryNumber = !dbPhoneNumber.isPrimaryNumber;
            } else
                dbPhoneNumber.isPrimaryNumber = false;
        }
        PhoneNumber.saveInTx(allDbPhoneNumbersOfContact);
        markPrimaryPhoneNumberInVCard(contact, getVCard(contact.id).vcardDataAsString);
    }

    static void updateLastAccessed(long contactId, String callTimeStamp) {
        opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getDBContactWithId(contactId);
        if (callTimeStamp.equals(contact.lastAccessed))
            return;
        contact.lastAccessed = callTimeStamp;
        contact.save();
    }

    public static VCardData getVCard(long contactId) {
        return VCardData.getVCardData(contactId);
    }

    public static void deleteAllContactsAndRelatedStuff() {
        Contact.deleteAll(Contact.class);
        PhoneNumber.deleteAll(PhoneNumber.class);
        VCardData.deleteAll(VCardData.class);
        Favorite.deleteAll(Favorite.class);
        CallLogDataStore.removeAllContactsLinking();
    }

    public static Contact addContact(VCard vcard, Context context) {
        Contact contact = createContactSaveInDBAndReturnIt(vcard, context);
        createMobileNumbersAndSaveInDB(vcard, contact);
        createVCardDataAndSaveInDB(vcard, contact);
        addToFavoritesInCaseIs(vcard, contact);
        return contact;
    }

    private static void addToFavoritesInCaseIs(VCard vcard, Contact contact) {
        if (isFavorite(vcard)) ContactsDataStore.addFavorite(contact);
    }

    public static Contact addContact(Triplet<String, String, VCard> hrefEtagAndVCard, Context context) {
        Contact contact = createContactSaveInDBAndReturnIt(hrefEtagAndVCard.z, context);
        createMobileNumbersAndSaveInDB(hrefEtagAndVCard.z, contact);
        createVCardDataAndSaveInDB(hrefEtagAndVCard, contact);
        addToFavoritesInCaseIs(hrefEtagAndVCard.z, contact);
        return contact;
    }

    private static void createVCardDataAndSaveInDB(VCard vcard, Contact contact) {
        new VCardData(contact,
            vcard,
            vcard.getUid() == null ? UUID.randomUUID().toString() : vcard.getUid().getValue(),
            STATUS_CREATED,
            null
        ).save();
    }

    private static void createVCardDataAndSaveInDB(Triplet<String, String, VCard> hrefEtagAndVCard, Contact contact) {
        new VCardData(contact,
            hrefEtagAndVCard.z,
            hrefEtagAndVCard.z.getUid() == null ? UUID.randomUUID().toString() : hrefEtagAndVCard.z.getUid().getValue(),
            STATUS_CREATED,
            hrefEtagAndVCard.y,
            hrefEtagAndVCard.x
        ).save();
    }

    private static void createMobileNumbersAndSaveInDB(VCard vcard, Contact contact) {
        Log.i("FOR","Modificato-CDBHcreateMobileNumbersAndSaveInDB1");
        List<Telephone> vcardTelephoneNumbers = vcard.getTelephoneNumbers();
        int vCardTelephoneNumbersSize = vcardTelephoneNumbers.size();
        for (int i=0;i<vCardTelephoneNumbersSize;i++) {
            Telephone telephoneNumber = vcardTelephoneNumbers.get(i);
            try {//try block here to check if telephoneNumber.getUri is null. Do not want to check a lot of null combos. so try catch would help
                if (isEmpty(telephoneNumber.getText()) && isEmpty(telephoneNumber.getUri().getNumber()))
                    continue;
            } catch (Exception e) {
                continue;
            }
            new PhoneNumber(getMobileNumber(telephoneNumber), contact, isPrimaryPhoneNumber(telephoneNumber)).save();
        }
    }

    private static Contact createContactSaveInDBAndReturnIt(VCard vcard, Context context) {
        Pair<String, String> name = getNameFromVCard(vcard, context);
        Contact contact = new Contact(name.first, name.second);
        contact.groups = getGroupsNamesCSVString(getCategories(vcard));
        contact.pinyinName = getPinyinTextFromChinese(contact.getFullName());
        contact.save();
        return contact;
    }

    public static List<TemporaryContact> getTemporaryContactDetails() {
        return TemporaryContact.listAll(TemporaryContact.class);
    }

    public static void markContactAsTemporary(long id) {
        new TemporaryContact(getDBContactWithId(id), new Date()).save();
    }

    public static void unmarkContactAsTemporary(long id) {
        Log.i("FOR","Modificato-CDBHunmarkContactAsTemporary1");
        List<TemporaryContact> temporaryContactList =TemporaryContact.find(TemporaryContact.class, "contact = ?", "" + id);
        int temporaryContactListSize = temporaryContactList.size();
        for(int i=0;i<temporaryContactListSize;i++) temporaryContactList.get(i).delete();

    }

    public static boolean isTemporary(long id){
        return !TemporaryContact.find(TemporaryContact.class, "contact = ?", "" + id).isEmpty();
    }

    public static TemporaryContact getTemporaryContactDetails(long id) {
        return TemporaryContact.find(TemporaryContact.class, "contact = ?", "" + id).get(0);
    }
}
