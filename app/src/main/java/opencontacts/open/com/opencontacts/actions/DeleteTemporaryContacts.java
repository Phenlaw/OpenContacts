package opencontacts.open.com.opencontacts.actions;

import static java.util.Calendar.DAY_OF_MONTH;
import static opencontacts.open.com.opencontacts.utils.Common.getCalendarInstanceAt;
import static opencontacts.open.com.opencontacts.utils.Common.hasItBeen;

import android.content.Context;
import android.util.Log;

import com.github.underscore.U;

import java.util.List;

import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.orm.TemporaryContact;

public class DeleteTemporaryContacts implements ContactsHouseKeepingAction{

    @Override
    public void perform(List<Contact> contacts, Context context) {
        Log.i("FOR","Modificato-DTCperform1");
        int size= ContactsDataStore.getTemporaryContactDetails().size();
        List<TemporaryContact> contactDetails = ContactsDataStore.getTemporaryContactDetails();
        for(int i=0;i<size;i++){
            if(hasItBeen(30, DAY_OF_MONTH, contactDetails.get(i).markedTemporaryOn.getTime())) ContactsDataStore.removeContact(contactDetails.get(i).contact.getId());
        }
    }
}
