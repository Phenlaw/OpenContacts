package opencontacts.open.com.opencontacts.activities;


import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import opencontacts.open.com.opencontacts.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

import com.github.underscore.U;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@LargeTest
@RunWith(AndroidJUnit4.class)

public class Inserisci_Ricerca_Chiama_TEST {
    List<String> names = Arrays.asList("Gabriele","Ciro","Ciro","Marco","Carolina","Maria","Mario","Genoveffa","Attilio","Bernardo","Carlo","Dario","Emanuele","Fabio","Giacomo","Giuseppe","Iole","Luca","Michele","Nicola","Orazio","Pasquale","Rino","Salvatore","Tiziano","Ubaldo","Vincenzo","Vincenza","Gabriella");
    List<String> lastnames = Arrays.asList("Merola","Listone","Esposito","Rossi","Russo","Rizzo","Rizzoli","Pisani","Adamo","Boccia","Cutolo","DiSpazio","Fortini","Gambardella","Imola","Luciani","Molina","Navona","Oresti","Piccirillo","Savona","Tortolano","Università","Ventrone","Zarra","Martucci","Leonetti","Brizzi","Nuzzo","Vittoria");

    List<String> mails = Arrays.asList("g.merola95@gmail.com","cirolistone@gmail.com","ciroesposito@hotmail.it","eabrown@me.com",
    "alastair@optonline.net",
    "oster@sbcglobal.net",
    "barjam@verizon.net" ,
    "mxiao@live.com",
    "dvdotnet@mac.com"  ,
    "smartfart@mac.com" ,
    "pkilab@mac.com"  ,
    "cliffordj@yahoo.com"  ,
    "bolow@yahoo.com" ,
    "gospodin@yahoo.ca"   ,
    "drezet@me.com", "jfreedma@gmail.com",
    "rhialto@me.com",
    "choset@mac.com" ,
    "chance@me.com"   ,
    "mahbub@yahoo.com" ,
    "danny@mac.com",
    "godeke@me.com" ,
    "singh@msn.com"  ,
   " frode@msn.com"   ,
    "ninenine@msn.com" ,
    "jshearer@yahoo.com",
    "majordick@me.com","fozzanapoli@napoli.it","wlajuve@hotmail.it","messithebest@virgilio.com");
    List<String> numbers = Arrays.asList("3408447935","3408447934","3408447933","3408447932","3408447931","3408447930","3408447929","3408447928","3408447927","3408447926","3408447925","3408447924","3408447923","3408447922","3408447921","3408447920","3408447919","3408447918","3408447917","3408447916","3408447915","3408447914","3408447913","3408447912","3408447911","3408447910","3408447909","3408447908","3408447907","3408447906");
    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.CALL_PHONE",
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_PHONE_STATE",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.POST_NOTIFICATIONS");


    @Test
    public void inserisci_Ricerca_Chiama_TEST() throws InterruptedException {
        ViewInteraction appCompatButton = onView(
            allOf(withId(android.R.id.button3), withText("Okay"),
                childAtPosition(
                    childAtPosition(
                        withId(pro.midev.expandedmenulibrary.R.id.buttonPanel),
                        0),
                    0)));
        try {
            appCompatButton.perform(scrollTo(), click());
        } catch (NoMatchingViewException e) {
        // View is not in hierarchy
    }
        ViewInteraction appCompatButton2 = onView(
            allOf(withId(android.R.id.button3), withText("Okay"),
                childAtPosition(
                    childAtPosition(
                        withId(pro.midev.expandedmenulibrary.R.id.buttonPanel),
                        0),
                    0)));
        try {
            appCompatButton2.perform(scrollTo(), click());
        } catch (NoMatchingViewException e) {
        // View is not in hierarchy

        }
        ViewInteraction button = onView(
            allOf(withId(R.id.start_button), withText("Start"),
                childAtPosition(
                    allOf(withId(R.id.activity_tabbed),
                        childAtPosition(
                            withId(android.R.id.content),
                            0)),
                    1),
                isDisplayed()));
        try{
            button.perform(click());
        } catch (NoMatchingViewException e) {
            // View is not in hierarchy
        }
        int i=0;
        for(String name: names){
                    String lastname = lastnames.get(i);
                    String mail = mails.get(i);
                    String number = numbers.get(i);

                    ViewInteraction actionMenuItemView = onView(
                        allOf(withId(R.id.button_new)));
                    actionMenuItemView.perform(click());
                    ViewInteraction textInputEditText = onView(
                        allOf(withId(R.id.editFirstName)));
                    textInputEditText.perform(scrollTo(), replaceText(name), closeSoftKeyboard());
                    ViewInteraction textInputEditText2 = onView(
                        allOf(withId(R.id.editLastName)));
                    textInputEditText2.perform(scrollTo(), replaceText(lastname), closeSoftKeyboard());

                    ViewInteraction textInputEditText3 = onView(
                        allOf(withId(R.id.edit_field),
                            isDisplayed()));

                    textInputEditText3.perform(replaceText(number), closeSoftKeyboard());


                    ViewInteraction textInputEditText4 = onView(
                        allOf(withId(R.id.edit_field_mail),
                            isDisplayed()));
                    textInputEditText4.perform(replaceText(mail), closeSoftKeyboard());

                    ViewInteraction actionMenuItemView2 = onView(
                        allOf(withContentDescription(R.string.save),
                            childAtPosition(
                                childAtPosition(
                                    withId(R.id.toolbar),
                                    2),
                                0),
                            isDisplayed()));
                    actionMenuItemView2.perform(click());
                 i++;
                }

        ViewInteraction tabView = onView(
allOf(withContentDescription(R.string.contacts),
childAtPosition(
childAtPosition(
withId(R.id.tab_layout),
0),
1),
isDisplayed()));
        tabView.perform(click());

        ViewInteraction actionMenuItemView3 = onView(
            allOf(withId(R.id.action_search), withContentDescription(R.string.search),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        1),
                    0),
                isDisplayed()));
        actionMenuItemView3.perform(click());

        ViewInteraction searchAutoComplete = onView(
            allOf(withId(pro.midev.expandedmenulibrary.R.id.search_src_text),
                childAtPosition(
                    allOf(withId(pro.midev.expandedmenulibrary.R.id.search_plate),
                        childAtPosition(
                            withId(pro.midev.expandedmenulibrary.R.id.search_edit_frame),
                            1)),
                    0),
                isDisplayed()));
        searchAutoComplete.perform(replaceText("3408447935"), closeSoftKeyboard());

        ViewInteraction relativeLayout = onView(
            allOf(withId(R.id.rl__listContact)));


        relativeLayout.perform(click());

        pressBack();
        ViewInteraction collapseButton = onView(
            allOf(withContentDescription("Collapse")));
        collapseButton.perform(click());

        ViewInteraction moreOptions = onView(
            allOf(withContentDescription("More options")));
        moreOptions.perform(click());

        ViewInteraction groupButton = onView(
            allOf(withId(R.id.title), withText(R.string.groups),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.content),
                        0),
                    0),
                isDisplayed()));
        groupButton.perform(click());

        ViewInteraction addGroupButton = onView(
            allOf(withContentDescription(R.string.add_group),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        2),
                    0),
                isDisplayed()));
        addGroupButton.perform(click());

        ViewInteraction appCompatCheckedTextView = onView(
            allOf(withId(R.id.contact_name), withText("Attilio Adamo")));
        appCompatCheckedTextView.perform(click());

        ViewInteraction appCompatCheckedTextView2 = onView(
            allOf(withId(R.id.contact_name), withText("Bernardo Boccia")));
        appCompatCheckedTextView2.perform(click());

        ViewInteraction appCompatCheckedTextView3 = onView(
            allOf(withId(R.id.contact_name), withText("Carlo Cutolo")));
        appCompatCheckedTextView3.perform(click());

        ViewInteraction appCompatCheckedTextView4 = onView(
            allOf(withId(R.id.contact_name), withText("Carolina Russo")));
        appCompatCheckedTextView4.perform(click());

        ViewInteraction appCompatCheckedTextView5 = onView(
            allOf(withId(R.id.contact_name), withText("Ciro Esposito")));
        appCompatCheckedTextView5.perform(click());


        ViewInteraction textInputEditText = onView(
            allOf(withHint(R.string.group_name)));
        textInputEditText.perform(replaceText("GruppoA"), closeSoftKeyboard());

        ViewInteraction saveButton = onView(
            allOf(withContentDescription(R.string.save),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        2),
                    0),
                isDisplayed()));
        saveButton.perform(click());


        pressBack();


        ViewInteraction moreOptions1 = onView(
            allOf(withContentDescription("More options")));

        moreOptions1.perform(click());


        ViewInteraction groupButton2 = onView(
            allOf(withId(R.id.title), withText(R.string.groups),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.content),
                        0),
                    0),
                isDisplayed()));
        groupButton2.perform(click());

        ViewInteraction moreOptions2 = onView(
            allOf(withContentDescription("More options")));
        moreOptions2.perform(click());

        ViewInteraction addGruopButton1 = onView(
            allOf(withText(R.string.add_group)));
        addGruopButton1.perform(click());

        ViewInteraction textInputEditText2 = onView(
            allOf(withHint(R.string.group_name)));
        textInputEditText2.perform(replaceText("GruppoB"), closeSoftKeyboard());

        ViewInteraction appCompatCheckedTextView6 = onView(
            allOf(withId(R.id.contact_name), withText("Ciro Listone")));
        appCompatCheckedTextView6.perform(scrollTo(),click());

        ViewInteraction appCompatCheckedTextView7 = onView(
            allOf(withId(R.id.contact_name), withText("Dario DiSpazio")));
        appCompatCheckedTextView7.perform(scrollTo(),click());

        ViewInteraction appCompatCheckedTextView8 = onView(
            allOf(withId(R.id.contact_name), withText("Emanuele Fortini")));
        appCompatCheckedTextView8.perform(scrollTo(),click());

        ViewInteraction appCompatCheckedTextView9 = onView(
            allOf(withId(R.id.contact_name), withText("Fabio Gambardella")));
        appCompatCheckedTextView9.perform(scrollTo(),click());


        ViewInteraction saveButton1 = onView(
            allOf(withContentDescription(R.string.save),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        2),
                    0),
                isDisplayed()));
        saveButton1.perform(click());


        ViewInteraction groupNamePopup = onView(
            allOf(withId(R.id.group_name),
                childAtPosition(
                    allOf(withId(R.id.toolbar),
                        childAtPosition(
                            withId(R.id.app_bar_layout),
                            0)),
                    1),
                isDisplayed()));
        groupNamePopup.perform(click());

        DataInteraction selectGroup = onData(anything())
            .inAdapterView(childAtPosition(
                withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                0))
            .atPosition(1);
        selectGroup.perform(click());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup)parent).getChildAt(position));
            }
        };
    }
    }
