package opencontacts.open.com.opencontacts.data.datastore;

import static ezvcard.Ezvcard.write;
import static opencontacts.open.com.opencontacts.data.datastore.ContactsDBHelper.getContact;
import static opencontacts.open.com.opencontacts.data.datastore.ContactsDBHelper.replacePhoneNumbersInDB;
import static opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore.updateContact;
import static opencontacts.open.com.opencontacts.orm.VCardData.STATUS_NONE;
import static opencontacts.open.com.opencontacts.orm.VCardData.STATUS_UPDATED;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.getPinyinTextFromChinese;
import static opencontacts.open.com.opencontacts.utils.VCardUtils.getNameFromVCard;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import java.io.IOException;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import opencontacts.open.com.opencontacts.orm.Contact;
import opencontacts.open.com.opencontacts.orm.VCardData;
import opencontacts.open.com.opencontacts.utils.Triplet;
import opencontacts.open.com.opencontacts.utils.VCardUtils;

public class ContactsSyncHelper {
    public static void replaceContactWithServers(Triplet<String, String, VCard> hrefEtagAndVCard, VCardData vCardData, Context context) {
        updateContact(vCardData.contact.getId(), "", hrefEtagAndVCard.z, context);
        VCardData updatedVCardData = VCardData.getVCardData(vCardData.contact.getId());
        updatedVCardData.href = hrefEtagAndVCard.x;
        updatedVCardData.status = STATUS_NONE;
        updatedVCardData.save();
    }

    public static void merge(Triplet<String, String, VCard> hrefEtagAndVCard, VCardData vCardData, Context context) {
        VCard vCardInDb = null;
        try {
            vCardInDb = new VCardReader(vCardData.vcardDataAsString).readNext();
        } catch (IOException e) {
            e.printStackTrace();
        }

        VCard mergedCard = VCardUtils.mergeVCards(vCardInDb, hrefEtagAndVCard.z, context);
        Contact dbContact = ContactsDBHelper.getDBContactWithId(vCardData.contact.getId());
        Pair<String, String> nameFromVCard = getNameFromVCard(mergedCard, context);
        dbContact.firstName = nameFromVCard.first;
        dbContact.lastName = nameFromVCard.second;
        Log.i("G&S", "Modificato");
        dbContact.pinyinName = getPinyinTextFromChinese(dbContact.firstName + " " + dbContact.lastName);
        dbContact.save();
        replacePhoneNumbersInDB(dbContact, hrefEtagAndVCard.z, getContact(vCardData.contact.getId()).primaryPhoneNumber.phoneNumber);
        Log.i("G&S","Modificato");
        vCardData.vcardDataAsString = write(mergedCard).caretEncoding(true).go();
        vCardData.etag = hrefEtagAndVCard.y;
        vCardData.href = hrefEtagAndVCard.x;
        vCardData.uid = hrefEtagAndVCard.z.getUid().getValue();
        vCardData.status = STATUS_UPDATED;
        vCardData.save();
    }
}
