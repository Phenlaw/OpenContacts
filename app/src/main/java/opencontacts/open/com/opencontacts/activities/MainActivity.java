package opencontacts.open.com.opencontacts.activities;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.util.Calendar.MINUTE;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getBoolean;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getLong;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getMenuItemClickHandlerFor;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getNumberToDial;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getStringFromPreferences;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.getThemeAttributeColor;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.hasPermission;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.isAddContactIntent;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.isValidDialIntent;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.runOnMainDelayed;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.setColorFilterUsingColor;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.updatePreference;
import static opencontacts.open.com.opencontacts.utils.AndroidUtils.wrapInConfirmation;
import static opencontacts.open.com.opencontacts.utils.Common.hasItBeen;
import static opencontacts.open.com.opencontacts.utils.domain.AppShortcuts.TAB_INDEX_INTENT_EXTRA;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import opencontacts.open.com.opencontacts.CardDavSyncActivity;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.actions.ExportMenuItemClickHandler;
import opencontacts.open.com.opencontacts.data.datastore.CallLogDataStore;
import opencontacts.open.com.opencontacts.fragments.AppBaseFragment;
import opencontacts.open.com.opencontacts.fragments.CallLogFragment;
import opencontacts.open.com.opencontacts.fragments.ContactsFragment;
import opencontacts.open.com.opencontacts.fragments.DialerFragment;
import opencontacts.open.com.opencontacts.interfaces.SelectableTab;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.DomainUtils;
import opencontacts.open.com.opencontacts.utils.SharedPreferencesUtils;
import pro.midev.expandedmenulibrary.ExpandedMenuItem;
import pro.midev.expandedmenulibrary.ExpandedMenuView;


public class MainActivity extends AppBaseActivity {
    public static final int CALLLOG_TAB_INDEX = 0;
    public static final int CONTACTS_TAB_INDEX = 1;
    public static final int DIALER_TAB_INDEX = 2;
    public static final String INTENT_EXTRA_LONG_CONTACT_ID = "contact_id";
    private static final int PREFERENCES_ACTIVITY_RESULT = 773;
    private static final int IMPORT_FILE_CHOOSER_RESULT = 467;
    private ViewPager viewPager;
    private SearchView searchView;
    private CallLogFragment callLogFragment;
    private ContactsFragment contactsFragment;
    private DialerFragment dialerFragment;
    private MenuItem searchItem;
    private ExpandedMenuView bottomMenu;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_FILE_CHOOSER_RESULT) {
            if (data == null) return;
            startActivity(
                new Intent(this, ImportVcardActivity.class)
                    .setData(data.getData())
            );
            return;
        }
        if (requestCode == PREFERENCES_ACTIVITY_RESULT) recreate();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private boolean handleIntent(Intent intent) {
        if (isValidDialIntent(intent)) showDialerWithNumber(getNumberToDial(intent));
        else if (isAddContactIntent(intent)) AndroidUtils.getAlertDialogToAddContact(intent.getStringExtra(ContactsContract.Intents.Insert.PHONE), this).show();
        else if (isTabSpecified(intent)) gotoTabSpecified(intent);
        else return false;
        return true;
    }

    private void showDialerWithNumber(String number) {
        runOnMainDelayed(() -> {
            viewPager.setCurrentItem(DIALER_TAB_INDEX);
            dialerFragment.setNumber(number);
        }, 500);
    }

    private boolean isTabSpecified(Intent intent) {
        return intent.getIntExtra(TAB_INDEX_INTENT_EXTRA, -1) != -1;
    }

    private void gotoTabSpecified(Intent intent) {
        int tabIndexToShow = intent.getIntExtra(TAB_INDEX_INTENT_EXTRA, -1);
        runOnMainDelayed(() -> viewPager.setCurrentItem(tabIndexToShow), 300);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        Log.i("G&S","Modificato");
        boolean shouldLaunchDefaultTab = hasItBeen(5, MINUTE, getLong(SharedPreferencesUtils.LAST_DEFAULT_TAB_LAUNCH_TIME_SHARED_PREF_KEY, 0, this));
        updatePreference(SharedPreferencesUtils.LAST_DEFAULT_TAB_LAUNCH_TIME_SHARED_PREF_KEY, new Date().getTime(), this);
        if (shouldLaunchDefaultTab) gotoDefaultTab();
    }

    private void gotoDefaultTab() {
        // post delayed as view pager is prioritising the fragment launched first as fragment 0 in the list
        // affecting the fragments order etc resulting in cast exception when recreating activity while reusing fragments
        runOnMainDelayed(() -> {
            if (viewPager == null) return;
            Log.i("G&S","Modificato");
            viewPager.setCurrentItem(Integer.parseInt(getStringFromPreferences(SharedPreferencesUtils.DEFAULT_TAB_SHARED_PREF_KEY, "0", this)));
        }, 100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("G&S","Modificato");
        if (getBoolean(SharedPreferencesUtils.SHOULD_ASK_FOR_PERMISSIONS, true, this)) {
            AndroidUtils.askForPermissionsIfNotGranted(this);
            View startButton = findViewById(R.id.start_button);
            startButton.setVisibility(VISIBLE);
            startButton.setOnClickListener(x -> this.recreate());
            Log.i("G&S","Modificato");
            updatePreference(SharedPreferencesUtils.SHOULD_ASK_FOR_PERMISSIONS, false, this);
            return;
        } else {
            setupTabs();
            setupBottomMenu();
            Log.i("G&S","Modificato");
            if (getBoolean(SharedPreferencesUtils.KEYBOARD_RESIZE_VIEWS_SHARED_PREF_KEY, false, this))
                getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE);
            if (handleIntent(getIntent())) ;
            else gotoDefaultTab();
        }
        Log.i("G&S","Modificato");
        updatePreference(SharedPreferencesUtils.SHOULD_ASK_FOR_PERMISSIONS, false, this);
    }

    private void setupBottomMenu() {
        bottomMenu = findViewById(R.id.bottom_menu);
        Log.i("G&S","Modificato");
        if (!getBoolean(SharedPreferencesUtils.SHOULD_SHOW_BOTTOM_MENU_SHARED_PREF_KEY, true, this)) {
            bottomMenu.setVisibility(GONE);
            bottomMenu = null;
            return;
        }
        Log.i("G&S","Modificato");
        ExpandedMenuItem searchItem = new ExpandedMenuItem(R.drawable.ic_search_black_24dp, "Search", getThemeAttributeColor(android.R.attr.textColorPrimary, this));
        Log.i("G&S","Modificato");
        ExpandedMenuItem groupItem = new ExpandedMenuItem(R.drawable.ic_group_merge_contacts_24dp, "Groups", getThemeAttributeColor(android.R.attr.textColorPrimary, this));
        Log.i("G&S","Modificato");
        ExpandedMenuItem dialpadItem = new ExpandedMenuItem(R.drawable.dial_pad, "Dial", getThemeAttributeColor(android.R.attr.textColorPrimary, this));
        Log.i("G&S","Modificato");
        ExpandedMenuItem addContactItem = new ExpandedMenuItem(R.drawable.ic_add_circle_outline_24dp, "Add contact", getThemeAttributeColor(android.R.attr.textColorPrimary, this));
        bottomMenu.setIcons(searchItem, groupItem, addContactItem, dialpadItem);
        bottomMenu.setOnItemClickListener(i -> {
            switch (i) {
                case 0:
                    searchContacts();
                    break;
                case 1:
                    launchGroupsActivity();
                    break;
                case 2:
                    launchAddContact();
                    break;
                case 3:
                    viewPager.setCurrentItem(DIALER_TAB_INDEX);
                    break;
            }
        });
        Log.i("G&S","Modificato");
        if (getBoolean(SharedPreferencesUtils.BOTTOM_MENU_OPEN_DEFAULT_SHARED_PREF_KEY, true, this)) bottomMenu.expandMenu();
        else bottomMenu.collapseMenu();
    }

    @Override
    int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        setMenuItemsListeners(menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setMenuItemsListeners(Menu menu) {
        menu.findItem(R.id.button_new).setOnMenuItemClickListener(getMenuItemClickHandlerFor(this::launchAddContact));
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnSearchClickListener(v -> viewPager.setCurrentItem(CONTACTS_TAB_INDEX));
        menu.findItem(R.id.action_sync).setOnMenuItemClickListener(getMenuItemClickHandlerFor(() ->
            startActivity(new Intent(this, CardDavSyncActivity.class))
        ));
        menu.findItem(R.id.action_import).setOnMenuItemClickListener(getMenuItemClickHandlerFor(this::importContacts));
        menu.findItem(R.id.action_merge).setOnMenuItemClickListener(getMenuItemClickHandlerFor(() ->
            startActivity(new Intent(this, MergeContactsActivity.class))
        ));

        if (contactsFragment != null)
            contactsFragment.configureSearchInMenu(searchView);
        menu.findItem(R.id.action_export).setOnMenuItemClickListener(item -> {
            if (!hasPermission(WRITE_EXTERNAL_STORAGE, this)) {
                Toast.makeText(this, R.string.grant_storage_permisson_detail, Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 123);
                }
                return true;
            }
            return new ExportMenuItemClickHandler(this).onMenuItemClick(item);
        });
        menu.findItem(R.id.action_about).setOnMenuItemClickListener(getMenuItemClickHandlerFor(() ->
            startActivity(new Intent(MainActivity.this, AboutActivity.class))
        ));

        menu.findItem(R.id.action_groups).setOnMenuItemClickListener(getMenuItemClickHandlerFor(this::launchGroupsActivity));
        menu.findItem(R.id.action_help).setOnMenuItemClickListener(getMenuItemClickHandlerFor(() ->
            startActivity(new Intent(MainActivity.this, HelpActivity.class))
        ));

        menu.findItem(R.id.action_preferences).setOnMenuItemClickListener(getMenuItemClickHandlerFor(() ->
            startActivityForResult(new Intent(MainActivity.this, PreferencesActivity.class), PREFERENCES_ACTIVITY_RESULT)
        ));

        menu.findItem(R.id.action_resync).setOnMenuItemClickListener(getMenuItemClickHandlerFor(() ->
            CallLogDataStore.updateCallLogAsyncForAllContacts(MainActivity.this)
        ));
        menu.findItem(R.id.action_whats_new).setOnMenuItemClickListener(getMenuItemClickHandlerFor(() ->
            AndroidUtils.goToUrl(getString(R.string.gitlab_repo_tags_url), MainActivity.this)
        ));
        menu.findItem(R.id.action_export_call_log).setOnMenuItemClickListener(item -> {
            Toast.makeText(this, R.string.started_exporting_call_log, LENGTH_SHORT).show();
            try {
                DomainUtils.exportCallLog(this);
                Toast.makeText(this, R.string.exported_call_log_successfully, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.failed_exporting_call_log, Toast.LENGTH_LONG).show();
            }
            return true;
        });
        menu.findItem(R.id.action_delete_all_contacts).setOnMenuItemClickListener(getMenuItemClickHandlerFor(() ->
            wrapInConfirmation(() -> DomainUtils.deleteAllContacts(this), this)
        ));
    }

    private void launchAddContact() {
        Intent addContact = new Intent(MainActivity.this, EditContactActivity.class);
        addContact.putExtra(EditContactActivity.INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, true);
        startActivity(addContact);
    }

    private void launchGroupsActivity() {
        startActivity(new Intent(MainActivity.this, GroupsActivity.class));
    }

    private void searchContacts() {
        if (searchItem.isActionViewExpanded()) searchItem.collapseActionView();
        searchItem.expandActionView();
    }

    @Override
    public void onBackPressed() {
        AppBaseFragment currentFragment = getCurrentFragment();
        if (currentFragment == null) {
            super.onBackPressed();
            return;
        }
        if (currentFragment.handleBackPress()) return;
        super.onBackPressed();
    }

    private AppBaseFragment getCurrentFragment() {
        return (AppBaseFragment) ((FragmentPagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem());
    }

    private void importContacts() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .setType("*/*"),
                    IMPORT_FILE_CHOOSER_RESULT);
            } else startActivityForResult(
                new Intent(Intent.ACTION_PICK),
                IMPORT_FILE_CHOOSER_RESULT);
        } catch (Exception e) {
            makeText(this, R.string.no_app_found_for_action_open_document, LENGTH_SHORT);
        }
    }

    private void refresh() {
        CallLogDataStore.loadRecentCallLogEntriesAsync(MainActivity.this);
    }

    private void setupTabs() {
        viewPager = findViewById(R.id.view_pager);
        List<Fragment> fragmentsList = getSupportFragmentManager().getFragments();
        if (!fragmentsList.isEmpty()) {
            callLogFragment = (CallLogFragment) fragmentsList.get(0);
            contactsFragment = (ContactsFragment) fragmentsList.get(1);
            dialerFragment = (DialerFragment) fragmentsList.get(2);
        } else {
            callLogFragment = new CallLogFragment();
            contactsFragment = new ContactsFragment();
            dialerFragment = new DialerFragment();
        }
        callLogFragment.setEditNumberBeforeCallHandler(number -> {
            dialerFragment.setNumber(number);
            viewPager.setCurrentItem(DIALER_TAB_INDEX);
        });
        final List<SelectableTab> fragments = new ArrayList<>(Arrays.asList(callLogFragment, contactsFragment, dialerFragment));
        final List<String> tabTitles = Arrays.asList(getString(R.string.calllog), getString(R.string.contacts), "");

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitles.get(position);
            }

            @Override
            public Fragment getItem(int position) {
                return (Fragment) fragments.get(position);
            }
        };
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.setOffscreenPageLimit(3); //crazy shit with viewPager in case used with tablayout

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        Pair<Drawable, Drawable> dialerTabDrawables = getDialerTabDrawables();
        tabLayout.getTabAt(DIALER_TAB_INDEX).setIcon(dialerTabDrawables.first);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                fragments.get(tab.getPosition()).onSelect();
                if (tab.getPosition() == DIALER_TAB_INDEX) {
                    tab.setIcon(dialerTabDrawables.second);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                fragments.get(tab.getPosition()).onUnSelect();
                if (tab.getPosition() == DIALER_TAB_INDEX) {
                    tab.setIcon(dialerTabDrawables.first);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private Pair<Drawable, Drawable> getDialerTabDrawables() {
        int tabSelectedColor = getThemeAttributeColor(android.R.attr.textColorPrimary, MainActivity.this);
        int tabUnSelectedColor = getThemeAttributeColor(android.R.attr.textColorSecondary, MainActivity.this);
        Drawable dialpadIconOnUnSelect = ContextCompat.getDrawable(this, R.drawable.dial_pad).mutate();
        Drawable dialpadIconOnSelect = ContextCompat.getDrawable(this, R.drawable.dial_pad).mutate();
        setColorFilterUsingColor(dialpadIconOnSelect, tabSelectedColor);
        setColorFilterUsingColor(dialpadIconOnUnSelect, tabUnSelectedColor);
        return Pair.create(dialpadIconOnUnSelect, dialpadIconOnSelect);
    }

    public void collapseSearchView() {
        if (searchItem != null)
            searchItem.collapseActionView(); // happens when app hasn't even got menu items callback
    }

    public void hideBottomMenu() {
        if (bottomMenu == null) return;
        bottomMenu.setVisibility(GONE);
    }

    public void showBottomMenu() {
        Log.i("G&S","Modificato");
        if (bottomMenu == null || !getBoolean(SharedPreferencesUtils.SHOULD_SHOW_BOTTOM_MENU_SHARED_PREF_KEY, true, this)) return;
        bottomMenu.setVisibility(VISIBLE);
    }
}
