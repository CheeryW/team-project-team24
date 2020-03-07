package com.cse110team24.walkwalkrevolution;

import androidx.test.espresso.ViewInteraction;

import com.cse110team24.walkwalkrevolution.fitness.FitnessServiceFactory;
import com.cse110team24.walkwalkrevolution.mockedservices.MockActivityTestRule;
import com.cse110team24.walkwalkrevolution.mockedservices.TestAuth;
import com.cse110team24.walkwalkrevolution.mockedservices.TestFitnessService;
import com.cse110team24.walkwalkrevolution.mockedservices.TestInvitationsDatabaseService;
import com.cse110team24.walkwalkrevolution.mockedservices.TestMessage;
import com.cse110team24.walkwalkrevolution.models.invitation.Invitation;
import com.cse110team24.walkwalkrevolution.models.team.TeamAdapter;
import com.cse110team24.walkwalkrevolution.models.user.FirebaseUserAdapter;
import com.cse110team24.walkwalkrevolution.models.user.IUser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.cse110team24.walkwalkrevolution.mockedservices.TestFitnessService.TEST_SERVICE_KEY;
import static com.cse110team24.walkwalkrevolution.mockedservices.TestTeamsDatabaseService.testTeam;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

public class AcceptInvitationBDDEspressoTest {

    private List<IUser> listOfUsers;
    private List<Invitation> invitationList;

    @Rule
    public MockActivityTestRule<LoginActivity> mActivityTestRule = new MockActivityTestRule<>(LoginActivity.class);

    @Before
    public void setup() {
        FitnessServiceFactory.put(TEST_SERVICE_KEY, activity -> new TestFitnessService(activity));
        mActivityTestRule.getActivity().setFitnessServiceKey(TEST_SERVICE_KEY);
        TestAuth.isTestUserSignedIn = true;
        TestAuth.successUserSignedIn = true;
        TestMessage.invitationSentSuccess = true;

        //ME
        IUser satta_momoh = FirebaseUserAdapter.builder()
                .addDisplayName("Amara Momoh")
                .addEmail("amara@gmail.com")
                .addUid("1")
                .addTeamUid(null)
                .build();

        TestAuth.testAuthUser = satta_momoh;

        listOfUsers = new ArrayList<IUser>();
        invitationList = new ArrayList<Invitation>();
        testTeam = new TeamAdapter(listOfUsers);

        testTeam.addMember(satta_momoh);
//PERSON INVITING ME
        IUser amara_momoh = FirebaseUserAdapter.builder()
                .addDisplayName("Amara Momoh")
                .addEmail("ival@gmail.com")
                .addUid("2")
                .addTeamUid("666")
                .build();
        testTeam.addMember(amara_momoh);

        Invitation sentInvitation = new Invitation(amara_momoh);

        invitationList.add(sentInvitation);

        IUser maria_momoh = FirebaseUserAdapter.builder()
                .addDisplayName("Maria Momoh")
                .addEmail("mars@gmail.com")
                .addUid("3")
                .addTeamUid("666")
                .build();
        testTeam.addMember(maria_momoh);

        TestInvitationsDatabaseService.testInvitationsList = invitationList;

    }
    @Test
    public void loginActivitySignInEspressoTest() {
        setup();

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.enter_gmail_address), isDisplayed()));
        appCompatEditText.perform(replaceText("jose@gmail.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.enter_password), isDisplayed()));
        appCompatEditText2.perform(replaceText("1234jam"), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.et_height_feet), isDisplayed()));
        appCompatEditText4.perform(replaceText("5"), closeSoftKeyboard());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.et_height_remainder_inches), isDisplayed()));
        appCompatEditText5.perform(replaceText("7"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btn_height_finish), withText("Login"), isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.action_team), withContentDescription("Team"), isDisplayed()));
        bottomNavigationItemView.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.btn_pending_invites), withText("Pending Invites"), isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction listViewClick = onView(
                allOf(withId(R.id.invitationList), withContentDescription("Amara Momoh"), isDisplayed()));
        //listViewClick.perform(click());






    }
}
