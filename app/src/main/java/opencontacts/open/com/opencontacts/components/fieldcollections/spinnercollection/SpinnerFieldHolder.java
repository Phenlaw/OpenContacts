package opencontacts.open.com.opencontacts.components.fieldcollections.spinnercollection;

import static opencontacts.open.com.opencontacts.components.TintedDrawablesStore.getTintedDrawable;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.reginald.editspinner.EditSpinner;

import java.util.List;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.components.ImageButtonWithTint;
import opencontacts.open.com.opencontacts.components.fieldcollections.FieldViewHolder;

public class SpinnerFieldHolder extends FieldViewHolder {

    private final EditSpinner spinner;
    private final List<String> options;
    private final ImageButtonWithTint deleteButton;
    private View fieldView;

    SpinnerFieldHolder(List<String> options, boolean editDisabled, View fieldView, Context context) {
        spinner = fieldView.findViewById(R.id.spinner);
        deleteButton = fieldView.findViewById(R.id.delete);
        this.fieldView = fieldView;
        if (editDisabled) spinner.setEditable(false);
        this.options = options;
        Log.i("G&S","Modificato-setupSpinner");
        spinner.setDropDownDrawable(getTintedDrawable(R.drawable.ic_arrow_drop_down_black_24dp, context));
        spinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, options));
        if (options.size() > 0) spinner.selectItem(0);

    }

    public void set(String option) {
        Log.i("G&S","Modificato-setItem");
        int indexOfType = options.indexOf(option);
        if (indexOfType == -1) spinner.setText(option);
        else spinner.selectItem(indexOfType);

    }

    public void setOnDelete(View.OnClickListener onClickListener) {
        deleteButton.setOnClickListener(onClickListener);
    }

    @Override
    public String getValue() {
        return spinner.getText().toString();
    }

    @Override
    public View getView() {
        return fieldView;
    }
}
