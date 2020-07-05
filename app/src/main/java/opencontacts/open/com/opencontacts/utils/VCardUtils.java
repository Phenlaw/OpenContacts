package opencontacts.open.com.opencontacts.utils;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.github.underscore.U;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import ezvcard.property.Address;
import ezvcard.property.Birthday;
import ezvcard.property.Email;
import ezvcard.property.FormattedName;
import ezvcard.property.Note;
import ezvcard.property.SimpleProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.TextProperty;
import ezvcard.property.Uid;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
import opencontacts.open.com.opencontacts.R;

import static ezvcard.util.StringUtils.join;
import static opencontacts.open.com.opencontacts.utils.Common.getEmptyStringIfNull;
import static opencontacts.open.com.opencontacts.utils.Common.getPartsThatAreNotPresentCaseInSensitive;

public class VCardUtils {

    private static String noNameString;

    @NonNull
    public static Pair<String, String> getNameFromVCard(VCard vcard, Context context) {
        if(noNameString == null) noNameString = context.getString(R.string.noname);
        Pair<String, String> name;
        StructuredName structuredName = vcard.getStructuredName();
        FormattedName formattedName = vcard.getFormattedName();
        if (structuredName == null)
            if (formattedName == null) {
                name = new Pair<>(noNameString, "");
            } else name = new Pair<>(formattedName.getValue(), "");
        else name = getNameFromStructureNameOfVcard(structuredName);
        return name;
    }

    private static Pair<String, String> getNameFromStructureNameOfVcard(StructuredName structuredName) {
        List<String> additionalNames = structuredName.getAdditionalNames();
        String lastName = getEmptyStringIfNull(structuredName.getFamily());
        if (additionalNames.size() > 0) {
            StringBuilder nameBuffer = new StringBuilder();
            for (String additionalName : additionalNames)
                nameBuffer.append(additionalName).append(" ");
            lastName = nameBuffer.append(lastName).toString();
        }
        return new Pair<>(getEmptyStringIfNull(structuredName.getGiven()), lastName);
    }

    public static String getMobileNumber(Telephone telephone){
        String telephoneText = telephone.getText();
        return telephoneText == null ? telephone.getUri().getNumber() : telephoneText;
    }

    public static void setFormattedNameIfNotPresent(VCard vcard) {
        if(vcard.getFormattedName() != null) return;
        StructuredName structuredName = vcard.getStructuredName();
        if(structuredName == null) vcard.setFormattedName("");
        else vcard.setFormattedName(structuredName.getGiven() + " "  + structuredName.getFamily());
    }

    public static void setUidIfNotPresent(VCard vCard, String uid) {
        Uid existingUid = vCard.getUid();
        if(existingUid == null) vCard.setUid(new Uid(uid));
    }

    public static VCard mergeVCards(VCard secondaryVcard, VCard primaryVCard, Context context) {
        VCard mergedCard = null;

        try {
            mergedCard = new VCardReader(primaryVCard.write()).readNext();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mergedCard.getTelephoneNumbers().addAll(getExtraVCardProperties(primaryVCard, secondaryVcard, Telephone.class));
        mergedCard.getAddresses().addAll(getExtraVCardProperties(primaryVCard, secondaryVcard, Address.class));
        mergedCard.getNotes().addAll(getExtraVCardTextProperties(primaryVCard, secondaryVcard, Note.class));
        mergedCard.getEmails().addAll(getExtraVCardTextProperties(primaryVCard, secondaryVcard, Email.class));
        mergedCard.getUrls().addAll(getExtraVCardTextProperties(primaryVCard, secondaryVcard, Url.class));
        mergedCard.getBirthdays().addAll(getExtraVCardProperties(primaryVCard, secondaryVcard, Birthday.class));

        StructuredName finalName = getMergedStructuredName(secondaryVcard, primaryVCard, context);
        mergedCard.setStructuredName(finalName);
        Uid uid = primaryVCard.getUid();
        setUidIfNotPresent(mergedCard, uid == null ? UUID.randomUUID().toString() : uid.toString());
        return mergedCard;
    }

    private static <T extends TextProperty> List<T> getExtraVCardTextProperties(VCard primaryVCard, VCard secondaryVCard, Class<T> propertyClass){
        List<T> propertiesInPrimaryVCardCard = primaryVCard.getProperties(propertyClass);
        List<String> propertiesInPrimaryVCardAsStrings = U.map(propertiesInPrimaryVCardCard, SimpleProperty::getValue);
        return U.chain(secondaryVCard.getProperties(propertyClass))
                .reject(property -> propertiesInPrimaryVCardAsStrings.contains(property.getValue()))
                .value();
    }

    private static <T extends VCardProperty> List<T> getExtraVCardProperties(VCard primaryVCard, VCard secondaryVCard, Class<T> propertyClass){
        List<T> propertiesInPrimaryVCardCard = primaryVCard.getProperties(propertyClass);
        return U.chain(secondaryVCard.getProperties(propertyClass))
                .reject(propertiesInPrimaryVCardCard::contains)
                .value();
    }

    @NonNull
    private static StructuredName getMergedStructuredName(VCard contact1, VCard contact2, Context context) {
        Pair<String, String> nameFromContact2 = getNameFromVCard(contact2, context);
        Pair<String, String> nameFromContact1 = getNameFromVCard(contact1, context);
        StringBuffer firstNameOfContact2 = new StringBuffer(nameFromContact2.first);
        StringBuffer lastNameOfContact2 = new StringBuffer(nameFromContact2.second);

        firstNameOfContact2.append(" ").append(join(getPartsThatAreNotPresentCaseInSensitive(firstNameOfContact2 + " " + lastNameOfContact2, nameFromContact1.first), " "));
        lastNameOfContact2.append(" ").append(join(getPartsThatAreNotPresentCaseInSensitive(firstNameOfContact2 + " " + lastNameOfContact2, nameFromContact1.second), " "));

        StructuredName finalStructuredName = new StructuredName();
        finalStructuredName.setGiven(firstNameOfContact2.toString());
        finalStructuredName.setFamily(lastNameOfContact2.toString());
        return finalStructuredName;
    }

    public static VCard getVCardFromString(String vcardAsString) throws IOException {
        return new VCardReader(vcardAsString).readNext();
    }

    public static VCard mergeVCardStrings(String primaryVCardString, String secondaryVCardString, Context context) throws IOException {
        return mergeVCards(getVCardFromString(secondaryVCardString), getVCardFromString(primaryVCardString), context);
    }
}
