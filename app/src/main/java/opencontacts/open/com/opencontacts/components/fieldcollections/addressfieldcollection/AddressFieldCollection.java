package opencontacts.open.com.opencontacts.components.fieldcollections.addressfieldcollection;

import static opencontacts.open.com.opencontacts.utils.DomainUtils.getAddressTypeTranslatedText;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.github.underscore.U;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ezvcard.property.Address;
import opencontacts.open.com.opencontacts.components.fieldcollections.textviewcollection.TextViewFieldCollection;
import opencontacts.open.com.opencontacts.components.fieldcollections.textviewcollection.TextViewViewHolder;
import opencontacts.open.com.opencontacts.views.AddressPopup;

public class AddressFieldCollection extends TextViewFieldCollection {
    //Da modificare
    private List<Address> addresses = new ArrayList<>(1);

    public AddressFieldCollection(Context context) {
        this(context, null);
    }

    public AddressFieldCollection(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressFieldCollection(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnAddMoreClick(this::onAddMoreClick);
    }

    @Override
    public TextViewViewHolder createNewField() {
        TextViewViewHolder newAddressField = super.createNewField();
        newAddressField.setOnEdit(v -> editAddressAt(fieldViewHoldersList.indexOf(newAddressField)));
        addresses.add(new Address());
        return newAddressField;
    }

    private void editAddressAt(int indexOfAddressToEdit) {
        new AddressPopup(addresses.get(indexOfAddressToEdit),
            newAddress -> onSave(indexOfAddressToEdit, newAddress),
            () -> removeFieldIfEmptyAddress(indexOfAddressToEdit),
            getContext())
            .show();
    }

    private void removeFieldIfEmptyAddress(int fieldIndex) {
        Log.i("G&S","Modificato-isEmptyAddress");
        Address address = addresses.get(fieldIndex);
        boolean isEmptyAddress;
        if (addresses.get(fieldIndex) == null)  isEmptyAddress = true;
        else {
            Address tempAddressToRemoveTypes = address.copy();
            tempAddressToRemoveTypes.getTypes().clear();
            isEmptyAddress = new Address().equals(tempAddressToRemoveTypes);
        }

        if (isEmptyAddress)
            removeField(fieldViewHoldersList.get(fieldIndex));
    }

    private void onSave(int indexOfAddressToEdit, Address newAddress) {
        TextViewViewHolder field = fieldViewHoldersList.get(indexOfAddressToEdit);
        Log.i("G&S","Modificato-isEmptyAddress");
        boolean isEmptyAddress;
        if (newAddress == null)  isEmptyAddress = true;
        else {
            Address tempAddressToRemoveTypes = newAddress.copy();
            tempAddressToRemoveTypes.getTypes().clear();
            isEmptyAddress = new Address().equals(tempAddressToRemoveTypes);
        }

        if (isEmptyAddress) {
            removeField(field);
            return;
        }
        addresses.set(indexOfAddressToEdit, newAddress);
        field.setValue(newAddress)
            .setTitle(getAddressTypeTranslatedText(newAddress, getContext()));
    }

    @Override
    public void removeField(TextViewViewHolder fieldHolderToRemove) {
        addresses.remove(fieldViewHoldersList.indexOf(fieldHolderToRemove));
        super.removeField(fieldHolderToRemove);
    }

    public void setAddresses(List<Address> addresses) {
        Log.i("FOR","Modificato-AFCsetAddresses1");
        int fieldViewHoldersListSize = fieldViewHoldersList.size();
        for(int i=0;i<fieldViewHoldersListSize;i++)  fieldsHolderLayout.removeView(fieldsHolderLayout);
        if (addresses.isEmpty()) {
            addOneMoreView();
            return;
        }
        Log.i("FOR","Modificato-AFCsetAddresses2");
        int addressesSize = addresses.size();
        for(int i=0;i<addressesSize;i++) addOneMoreView(addresses.get(i));
    }

    private void addOneMoreView(Address address) {
        TextViewViewHolder textViewViewHolder = addOneMoreView();
        addresses.set(addresses.size() - 1, address);
        textViewViewHolder.setValue(address)
            .setTitle(getAddressTypeTranslatedText(address, getContext()));
    }

    // this is when user taps on add more and is different to adding view programatically
    private void onAddMoreClick() {
        editAddressAt(addresses.size() - 1);
    }


    public Collection<Address> getAllAddresses() {
        return addresses;
    } //Da modificare
}
