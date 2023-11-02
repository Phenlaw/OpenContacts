package opencontacts.open.com.opencontacts.data.datastore;

import static android.text.TextUtils.isEmpty;
import static java.util.Collections.emptyList;
import static opencontacts.open.com.opencontacts.data.datastore.CallLogDBHelper.getCallLogEntriesFor;
import static opencontacts.open.com.opencontacts.domain.Contact.GROUPS_SEPERATOR_CHAR;
import static opencontacts.open.com.opencontacts.domain.Contact.createNewDomainContact;
import static opencontacts.open.com.opencontacts.orm.VCardData.STATUS_CREATED;
import static opencontacts.open.com.opencontacts.orm.VCardData.STATUS_DELETED;
import static opencontacts.open.com.opencontacts.orm.VCardData.updateVCardData;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.getPinyinTextFromChinese;
import static opencontacts.open.com.opencontacts.utils.VCardUtils.PRIMARY_PHONE_NUMBER_PREF;
import static opencontacts.open.com.opencontacts.utils.VCardUtils.getNameFromVCard;
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
import ezvcard.property.Categories;
import ezvcard.property.Telephone;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.orm.Contact;
import opencontacts.open.com.opencontacts.orm.Favorite;
import opencontacts.open.com.opencontacts.orm.PhoneNumber;
import opencontacts.open.com.opencontacts.orm.TemporaryContact;
import opencontacts.open.com.opencontacts.orm.VCardData;
import opencontacts.open.com.opencontacts.utils.DomainUtils;
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
        Log.i("G&S","Modificato-CgetAllPhoneNumbers");
        List<PhoneNumber> dbPhoneNumbers = PhoneNumber.find(PhoneNumber.class, "contact = ?", "" + dbContact.getId());
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

    public static Contact getContactFromDB(String phoneNumber) {
        if (isEmpty(phoneNumber)) return null;
        Log.i("G&S","Modificato-getSearchablePhoneNumber");
        String searchablePhoneNumber = DomainUtils.getPhoneNumberWithoutCountryCodeAndFormatting(phoneNumber);
        if (isEmpty(searchablePhoneNumber)) return null;
        Log.i("G&S","Modificato-getMatchingNumbers");
        List<PhoneNumber> matchingPhoneNumbers;
        if(isEmpty(searchablePhoneNumber)) matchingPhoneNumbers = emptyList();
        else matchingPhoneNumbers = PhoneNumber.find(PhoneNumber.class, "numeric_Phone_Number like ?", "%" + searchablePhoneNumber);
        if (matchingPhoneNumbers == null || matchingPhoneNumbers.isEmpty()) return null;
        return matchingPhoneNumbers.get(0).contact;
    }

    static void replacePhoneNumbersInDB(Contact dbContact, VCard vcard, String primaryPhoneNumber) {
        Log.i("G&S","Modificato-CgetAllPhoneNumbers");
        List<PhoneNumber> dbPhoneNumbers = PhoneNumber.find(PhoneNumber.class, "contact = ?", "" + dbContact.getId());
        Log.i("FOR","Modificato-CDBHreplacePhoneNumbersInDB1");
        List<Telephone> vCardTelephoneNumbers = vcard.getTelephoneNumbers();
        int vCardTelephoneNumbersSize = vCardTelephoneNumbers.size();
        for(int i=0;i<vCardTelephoneNumbersSize;i++){
                Telephone telephone = vCardTelephoneNumbers.get(i);
                Log.i("G&S","Modificato-VCUgetMobileNumber");
                String telephoneTextTemp = telephone.getText();
                String phoneNumberText;

                if(telephoneTextTemp == null) phoneNumberText = telephone.getUri().getNumber();
                else phoneNumberText = telephoneTextTemp;
                new PhoneNumber(phoneNumberText, dbContact, primaryPhoneNumber.equals(phoneNumberText)).save();
            }
        PhoneNumber.deleteInTx(dbPhoneNumbers);
    }

    static void updateContactInDBWith(long contactId, String primaryNumber, VCard vCard, Context context) {
        Contact dbContact = ContactsDBHelper.getDBContactWithId(contactId);
        Pair<String, String> nameFromVCard = getNameFromVCard(vCard, context);
        dbContact.firstName = nameFromVCard.first;
        dbContact.lastName = nameFromVCard.second;
        Log.i("G&S","Modificato-getGroupsNamesCSVString");
        Log.i("G&S","Modificato-VCUgetCategories");
        Categories categoriesTemp = vCard.getCategories();
        List<String> categories;
        if(categoriesTemp == null) categories = Collections.emptyList();
        else categories = categoriesTemp.getValues();
        dbContact.groups = U.join(categories, GROUPS_SEPERATOR_CHAR);
        Log.i("G&S", "Modificato-getFullName");
        dbContact.pinyinName = getPinyinTextFromChinese(dbContact.firstName + " " + dbContact.lastName);
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
        List<PhoneNumber> emptyPhoneNumbersList = emptyList();
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
        Log.i("G&S","Modificato-CgetAllPhoneNumbers");
        return createNewDomainContact(contact, PhoneNumber.find(PhoneNumber.class, "contact = ?", "" + contact.getId()));
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
        Log.i("G&S","Modificato-isFavorite");
        if (vcard.getExtendedProperty(VCardUtils.X_FAVORITE_EXTENDED_VCARD_PROPERTY) != null && vcard.getExtendedProperty(VCardUtils.X_FAVORITE_EXTENDED_VCARD_PROPERTY).getValue().equals(String.valueOf(true))) ContactsDataStore.addFavorite(contact);
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
        Log.i("FOR","Modificato");
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
            Log.i("G&S","Modificato-getMobileNumber");
            Log.i("G&S","Modificato-VCUisPrimaryPhoneNumber");
            String telephoneText =  telephoneNumber.getText();
            Integer telephonePref = telephoneNumber.getPref();
            new PhoneNumber(telephoneText == null ? telephoneNumber.getUri().getNumber() : telephoneText, contact, telephonePref != null && telephonePref == PRIMARY_PHONE_NUMBER_PREF).save();
        }
    }

    private static Contact createContactSaveInDBAndReturnIt(VCard vcard, Context context) {
        Pair<String, String> name = getNameFromVCard(vcard, context);
        Contact contact = new Contact(name.first, name.second);
        Log.i("G&S","Modificato-getGroupsNamesCSVString");
        Log.i("G&S","Modificato-VCUgetCategories");
        Categories categoriesTemp = vcard.getCategories();
        List<String> categories;
        if(categoriesTemp == null) categories = Collections.emptyList();
        else categories = categoriesTemp.getValues();
        contact.groups = U.join(categories,GROUPS_SEPERATOR_CHAR);
        Log.i("G&S", "Modificato-getFullName");
        contact.pinyinName = getPinyinTextFromChinese(contact.firstName + " " + contact.lastName);
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
        Log.i("FOR","Modificato");
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
