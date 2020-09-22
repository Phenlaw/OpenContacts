package opencontacts.open.com.opencontacts.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.underscore.U;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.ContactsListViewAdapter;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.actions.DefaultContactsListActions;
import opencontacts.open.com.opencontacts.data.datastore.ContactGroupsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.domain.ContactGroup;

import static android.text.TextUtils.isEmpty;
import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static opencontacts.open.com.opencontacts.activities.ContactGroupEditActivity.GROUP_NAME_INTENT_EXTRA;

public class GroupsActivity extends AppBaseActivity {

    private AppCompatSpinner groupNameSpinner;
    private List<ContactGroup> allGroups;
    private ArrayAdapter<Object> spinnerAdapter;
    private ListView contactsListView;
    private String selectedGroupName;
    private ContactsListViewAdapter contactsListAdapter;

    @Override
    int getLayoutResource() {
        return R.layout.activity_groups;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        groupNameSpinner = findViewById(R.id.group_name);
        groupNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ContactGroup selectedItem = (ContactGroup) groupNameSpinner.getSelectedItem();
                if(selectedItem == null) return;
                selectedGroupName = selectedItem.getName();
                showContactsListOfSelectedGroup(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        contactsListView = findViewById(R.id.contacts_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        allGroups = ContactGroupsDataStore.getAllGroups();
        if(allGroups.isEmpty()) showEmptyGroupsMessage();
        else setupAndShowGroups();
    }

    private void setupAndShowGroups() {
        int selectedGroupIndex = getSelectedGroupIndex();
        refreshGroupNamesSpinnerData(selectedGroupIndex);
        showContactsListOfSelectedGroup(selectedGroupIndex);
        showGroupNamesAndRelatedContacts();
    }

    private int getSelectedGroupIndex() {
        if(isEmpty(selectedGroupName)) return 0;
        int selectedGroupIndex = U.findIndex(allGroups, group -> group.getName().equals(selectedGroupName));
        return selectedGroupIndex == -1 ? 0 : selectedGroupIndex;
    }

    private void showContactsListOfSelectedGroup(int selectedGroupIndex) {
        if(contactsListAdapter == null) setupContactsListAdapter();

        contactsListAdapter.clear();
        contactsListAdapter.addAll(new ArrayList<>(allGroups.get(selectedGroupIndex).contacts));
        contactsListAdapter.notifyDataSetChanged();
    }

    private void setupContactsListAdapter() {
        contactsListAdapter = new ContactsListViewAdapter(this);
        contactsListView.setAdapter(contactsListAdapter);
        contactsListAdapter.setContactsListActionsListener(new DefaultContactsListActions(this){
            @Override
            public void onLongClick(Contact contact) { }
        });
    }

    private void refreshGroupNamesSpinnerData(int selectedGroupIndex) {
        if(spinnerAdapter == null) setupSpinnerAdapter();
        else {
            spinnerAdapter.clear();
            spinnerAdapter.addAll(allGroups);
        }
        invalidateOptionsMenu(); //this will show the edit option
        groupNameSpinner.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();
        groupNameSpinner.setSelection(selectedGroupIndex);
    }

    private void setupSpinnerAdapter() {
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(allGroups));
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
    }

    private void showEmptyGroupsMessage(){
        findViewById(R.id.empty_groups_textview).setVisibility(VISIBLE);
        groupNameSpinner.setVisibility(GONE);
        setTitle(R.string.groups);
    }

    private void showGroupNamesAndRelatedContacts() {
        findViewById(R.id.empty_groups_textview).setVisibility(GONE);
        groupNameSpinner.setVisibility(VISIBLE);
        contactsListView.setVisibility(VISIBLE);
        setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.add_group)
                .setIcon(R.drawable.ic_add_24dp)
                .setShowAsActionFlags(SHOW_AS_ACTION_ALWAYS)
                .setOnMenuItemClickListener(item -> {
                    startActivity(new Intent(GroupsActivity.this, ContactGroupEditActivity.class));
                    return true;
                });
        if(allGroups == null || allGroups.isEmpty()) return super.onCreateOptionsMenu(menu);
        menu.add(R.string.edit_group)
                .setShowAsActionFlags(SHOW_AS_ACTION_ALWAYS)
                .setIcon(R.drawable.edit)
                .setOnMenuItemClickListener(item -> {
                    ContactGroup selectedGroup = (ContactGroup) groupNameSpinner.getSelectedItem();
                    startActivity(
                            new Intent(GroupsActivity.this, ContactGroupEditActivity.class)
                                    .putExtra(GROUP_NAME_INTENT_EXTRA, selectedGroup == null ? "" : selectedGroup.getName())
                    );
                    return true;
                });
        return super.onCreateOptionsMenu(menu);
    }

}