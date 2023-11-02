package opencontacts.open.com.opencontacts.views;

import static java.util.Arrays.asList;
import static opencontacts.open.com.opencontacts.components.TintedDrawablesStore.getTintedDrawable;
import static opencontacts.open.com.opencontacts.utils.Common.getOrDefault;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.getAddressTypeTranslatedText;

import android.content.Context;

import com.github.underscore.U;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.util.Consumer;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.reginald.editspinner.EditSpinner;

import java.util.List;

import ezvcard.parameter.AddressType;
import ezvcard.property.Address;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.utils.DomainUtils;

public class AddressPopup {

    private final Address address;
    private final Consumer<Address> onSave;
    private Runnable onDismiss;
    private final View inflatedView;
    private Context context;
    private TextInputEditText poBoxTextInput;
    private TextInputEditText addressTextInput;
    private TextInputEditText extendedAddressTextInput;
    private TextInputEditText postalCodeTextInput;
    private TextInputEditText cityTextInput;
    private TextInputEditText stateTextInput;
    private TextInputEditText countryTextInput;
    private EditSpinner addressTypeSpinner;
    private List<String> types;

    public AddressPopup(Address address, Consumer<Address> onSave, Runnable onDismiss, Context context) {
        this.address = address.copy();
        this.onSave = onSave;
        this.onDismiss = onDismiss;
        inflatedView = LayoutInflater.from(context).inflate(R.layout.view_address_edit_popup, null);
        this.context = context;
        linkAllFields();
        fillAddressIntoFields();
    }

    private void fillAddressIntoFields() {
        poBoxTextInput.setText(address.getPoBox());
        addressTextInput.setText(address.getStreetAddress());
        extendedAddressTextInput.setText(address.getExtendedAddress());
        postalCodeTextInput.setText(address.getPostalCode());
        cityTextInput.setText(address.getLocality());
        stateTextInput.setText(address.getRegion());
        countryTextInput.setText(address.getCountry());
        Log.i("G&S","Modificato-setItem");
        int indexOfType = types.indexOf(getAddressTypeTranslatedText(address, context));
        if (indexOfType == -1) addressTypeSpinner.setText(getAddressTypeTranslatedText(address, context));
        else addressTypeSpinner.selectItem(indexOfType);

    }

    private void computeAddressAndCallBack() {
        computeNewAddressFromFields();
        onSave.accept(address);
    }

    private void computeNewAddressFromFields() {
        if (!poBoxTextInput.getText().toString().equals(address.getPoBox()))
            address.setPoBox(poBoxTextInput.getText().toString());
        if (!addressTextInput.getText().toString().equals(address.getStreetAddress()))
            address.setStreetAddress(addressTextInput.getText().toString());
        if (!extendedAddressTextInput.getText().toString().equals(address.getExtendedAddress()))
            address.setExtendedAddress(extendedAddressTextInput.getText().toString());
        if (!postalCodeTextInput.getText().toString().equals(address.getPostalCode()))
            address.setPostalCode(postalCodeTextInput.getText().toString());
        if (!cityTextInput.getText().toString().equals(address.getLocality()))
            address.setLocality(cityTextInput.getText().toString());
        if (!stateTextInput.getText().toString().equals(address.getRegion()))
            address.setRegion(stateTextInput.getText().toString());
        if (!countryTextInput.getText().toString().equals(address.getCountry()))
            address.setCountry(countryTextInput.getText().toString());

        updateAddressType();
    }

    private void updateAddressType() {
        Log.i("G&S","Modificato-DUgetAddressType");
        if (DomainUtils.translatedTextToAddressType == null)
            DomainUtils.translatedTextToAddressType = U.toMap(U.invert(DomainUtils.getAddressTypeToTranslatedTextMap(context)));
        AddressType at = getOrDefault(DomainUtils.translatedTextToAddressType, addressTypeSpinner.getText().toString(), AddressType.get(addressTypeSpinner.getText().toString()));
        AddressType addressType = at;
        List<AddressType> existingAddressTypes = address.getTypes();
        if (existingAddressTypes.isEmpty()) existingAddressTypes.add(addressType);
        else existingAddressTypes.set(0, addressType);
    }

    public void show() {
        new AlertDialog.Builder(context)
            .setView(inflatedView)
            .setPositiveButton(R.string.okay, (dialog, which) -> computeAddressAndCallBack())
            .setNegativeButton(R.string.cancel, (dialog, which) -> onDismiss.run())
            .setOnCancelListener(dialog -> onDismiss.run())
            .setCancelable(true)
            .show();
    }

    private void linkAllFields() {
        poBoxTextInput = inflatedView.findViewById(R.id.post_office_box);
        addressTextInput = inflatedView.findViewById(R.id.street_address);
        extendedAddressTextInput = inflatedView.findViewById(R.id.extended_address);
        postalCodeTextInput = inflatedView.findViewById(R.id.postal_code);
        cityTextInput = inflatedView.findViewById(R.id.city);
        stateTextInput = inflatedView.findViewById(R.id.state);
        countryTextInput = inflatedView.findViewById(R.id.country);
        addressTypeSpinner = inflatedView.findViewById(R.id.address_types);

        setupAddressTypeSpinner();
    }

    private void setupAddressTypeSpinner() {
        types = asList(context.getResources().getStringArray(R.array.address_types));
        Log.i("G&S", "Modificato-setupSpinner");
        addressTypeSpinner.setDropDownDrawable(getTintedDrawable(R.drawable.ic_arrow_drop_down_black_24dp, context));
        addressTypeSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, types));
        if (types.size() > 0) addressTypeSpinner.selectItem(0);
    }
}
