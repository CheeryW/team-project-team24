package com.cse110team24.walkwalkrevolution;


import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AcceptWalkEspressoTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void acceptWalkEspressoTest() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.enter_gmail_address), isDisplayed()));
        appCompatEditText.perform(replaceText("satta@gmail.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.enter_password), isDisplayed()));
        appCompatEditText2.perform(replaceText("dogfood"), closeSoftKeyboard());

        pressBack();

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.et_height_feet), isDisplayed()));
        appCompatEditText3.perform(replaceText("5"), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.et_height_remainder_inches), isDisplayed()));
        appCompatEditText4.perform(replaceText("9"), closeSoftKeyboard());

        pressBack();

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btn_height_finish), withText("Login"), isDisplayed()));
        appCompatButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.action_team), withContentDescription("Team"), isDisplayed()));
        bottomNavigationItemView.perform(click());

        ViewInteraction bottomNavigationItemView2 = onView(
                allOf(withId(R.id.action_team), withContentDescription("Team"), isDisplayed()));
        bottomNavigationItemView2.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.btn_scheduled_walks), withText("Scheduled and Proposed Walks"), isDisplayed()));
        appCompatButton2.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.schedule_propose_btn_accept), withText("Accept"), isDisplayed()));
        appCompatButton3.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView = onView(
                allOf(withId(R.id.schedule_propose_tv_proposed_by_prompt), withText("Proposed by:"), isDisplayed()));
        textView.check(matches(withText("Proposed by:")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.schedule_propose_tv_proposed_by_display), withText("Ival Momoh"), isDisplayed()));
        textView2.check(matches(withText("Ival Momoh")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.schedule_propose_tv_walk_name_prompt), withText("Name of walk:"), isDisplayed()));
        textView3.check(matches(withText("Name of walk:")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.schedule_propose_tv_walk_name_display), withText("Catwalk"), isDisplayed()));
        textView4.check(matches(withText("Catwalk")));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.schedule_propose_tv_starting_loc), withText("Starting location:"), isDisplayed()));
        textView5.check(matches(withText("Starting location:")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.schedule_propose_tv_starting_loc_display), withText("(unspecified)"), isDisplayed()));
        textView6.check(matches(withText("(unspecified)")));

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.schedule_propose_tv_walk_date), withText("Proposed Date and Time:"), isDisplayed()));
        textView7.check(matches(withText("Proposed Date and Time:")));

        ViewInteraction textView8 = onView(
                allOf(withId(R.id.schedule_propose_tv_walk_date_display), withText("04/21/2020 at 12:00 PM"), isDisplayed()));
        textView8.check(matches(withText("04/21/2020 at 12:00 PM")));

        ViewInteraction button = onView(
                allOf(withId(R.id.schedule_propose_btn_accept), isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.schedule_propose_btn_decline_cant_come), isDisplayed()));
        button2.check(matches(isDisplayed()));

        ViewInteraction button3 = onView(
                allOf(withId(R.id.schedule_propose_btn_decline_not_interested), isDisplayed()));
        button3.check(matches(isDisplayed()));
    }
}
