package opencontacts.open.com.opencontacts.activities;

import static android.text.TextUtils.isEmpty;
import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static opencontacts.open.com.opencontacts.activities.ContactGroupEditActivity.GROUP_NAME_INTENT_EXTRA;
import static opencontacts.open.com.opencontacts.data.datastore.ContactGroupsDataStore.PROCESS_INTENSIVE_delete;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.blockUIUntil;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getMenuItemClickHandlerFor;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getStringFromPreferences;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.message;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.updatePreference;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.wrapInConfirmation;
import static opencontacts.open.com.opencontacts.utils.DomainUtils.sortContactsBasedOnName;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;

public class GroupsActivity extends AppBaseActivity {

    private AppCompatSpinner groupNameSpinner;
    private List<ContactGroup> allGroups;
    private ArrayAdapter<Object> spinnerAdapter;
    private ListView contactsListView;
    private ContactsListViewAdapter contactsListAdapter;
    private ArrayList<Contact> currentlySelectedGroupContactsSorted;

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
                if (selectedItem == null) return;
                Log.i("G&S","Modificato-setLastVisitedGroup");
                Log.i("G&S","Modificato-CGgetName");
                updatePreference(SharedPreferencesUtils.LAST_VISITED_GROUP_SHARED_PREF_KEY, selectedItem.name, GroupsActivity.this);
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
        refreshContent();
    }

    private void refreshContent() {
        allGroups = ContactGroupsDataStore.getAllGroups();
        if (allGroups.isEmpty()) showEmptyGroupsMessage();
        else setupAndShowGroups();
    }

    private void setupAndShowGroups() {
        int selectedGroupIndex = getSelectedGroupIndex();
        refreshGroupNamesSpinnerData(selectedGroupIndex);
        showContactsListOfSelectedGroup(selectedGroupIndex);
        showGroupNamesAndRelatedContacts();
    }

    private int getSelectedGroupIndex() {
        Log.i("G&S","Modificato-getLastVistedGroup");
        String lastVisitedGroupName = getStringFromPreferences(SharedPreferencesUtils.LAST_VISITED_GROUP_SHARED_PREF_KEY, "", this);
        if (isEmpty(lastVisitedGroupName)) return 0;
        Log.i("G&S","Modificato-CGgetName");
        int selectedGroupIndex = U.findIndex(allGroups, group -> group.name.equals(lastVisitedGroupName));
        return selectedGroupIndex == -1 ? 0 : selectedGroupIndex;
    }

    private void showContactsListOfSelectedGroup(int selectedGroupIndex) {
        currentlySelectedGroupContactsSorted = new ArrayList<>(
            sortContactsBasedOnName(allGroups.get(selectedGroupIndex).contacts, this)
        );// these will be used in search as well hence keeping them in member variable
        if (contactsListAdapter == null) setupContactsListAdapter();

        contactsListAdapter.clear();
        contactsListAdapter.addAll(currentlySelectedGroupContactsSorted);
        contactsListAdapter.notifyDataSetChanged();
    }

    private void setupContactsListAdapter() {
        contactsListAdapter = new ContactsListViewAdapter(this);
        contactsListAdapter.createContactsListFilter(this::getCurrentGroupContacts);
        contactsListView.setAdapter(contactsListAdapter);
        Log.i("G&S","Modificato-setContactsListActionsListener");
        contactsListAdapter.contactsListActionsListener = new DefaultContactsListActions(this){
            @Override
            public void onLongClick(Contact contact){

            }
        };

    }

    private List<Contact> getCurrentGroupContacts() {
        return currentlySelectedGroupContactsSorted;
    }

    private void refreshGroupNamesSpinnerData(int selectedGroupIndex) {
        if (spinnerAdapter == null) setupSpinnerAdapter();
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

    private void showEmptyGroupsMessage() {
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
            .setShowAsActionFlags(SHOW_AS_ACTION_IF_ROOM)
            .setOnMenuItemClickListener(getMenuItemClickHandlerFor(() ->
                startActivity(new Intent(GroupsActivity.this, ContactGroupEditActivity.class))
            ));
        if (allGroups == null || allGroups.isEmpty()) return super.onCreateOptionsMenu(menu);
        menu.add(R.string.edit_group)
            .setShowAsActionFlags(SHOW_AS_ACTION_ALWAYS)
            .setIcon(R.drawable.edit)
            .setOnMenuItemClickListener(item -> {
                ContactGroup selectedGroup = (ContactGroup) groupNameSpinner.getSelectedItem();
                Log.i("G&S","Modificato-CGgetName");
                startActivity(
                    new Intent(GroupsActivity.this, ContactGroupEditActivity.class)
                        .putExtra(GROUP_NAME_INTENT_EXTRA, selectedGroup == null ? "" : selectedGroup.name)
                );
                return true;
            });
        menu.add(R.string.message)
            .setShowAsActionFlags(SHOW_AS_ACTION_IF_ROOM)
            .setIcon(R.drawable.ic_chat_black_24dp)
            .setOnMenuItemClickListener(item -> {
                ContactGroup selectedGroup = (ContactGroup) groupNameSpinner.getSelectedItem();
                message(U.map(selectedGroup.contacts, contact -> contact.primaryPhoneNumber.phoneNumber), this);
                return true;
            });
        SearchView searchView = new SearchView(this);
        bindSearchViewToContacts(searchView);
        menu.add(R.string.search)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            .setActionView(searchView);
        menu.add(R.string.delete)
            .setShowAsActionFlags(SHOW_AS_ACTION_IF_ROOM)
            .setIcon(R.drawable.delete)
            .setOnMenuItemClickListener(getMenuItemClickHandlerFor(this::confirmAndDeleteGroup));
        return super.onCreateOptionsMenu(menu);
    }

    private void confirmAndDeleteGroup() {
        wrapInConfirmation(() -> {
            ContactGroup selectedGroup = (ContactGroup) groupNameSpinner.getSelectedItem();
            blockUIUntil(() -> {
                PROCESS_INTENSIVE_delete(selectedGroup, this);
                runOnUiThread(this::refreshContent);
            }, this);
        }, this);
    }

    private void bindSearchViewToContacts(SearchView searchView) {
        if (contactsListView == null) return;
        Log.i("G&S","Modificato-isT9SearchEnabled");
        searchView.setInputType(getBoolean(SharedPreferencesUtils.T9_SEARCH_ENABLED_SHARED_PREF_KEY, true, this) ? InputType.TYPE_CLASS_PHONE : InputType.TYPE_CLASS_TEXT);
        searchView.setOnCloseListener(() -> {
            contactsListView.clearTextFilter();
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ((ArrayAdapter) contactsListView.getAdapter()).getFilter().filter(newText);
                return true;
            }
        });

    }

}
