package com.cse110team24.walkwalkrevolution;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cse110team24.walkwalkrevolution.firebase.firestore.observers.TeamsDatabaseServiceObserver;
import com.cse110team24.walkwalkrevolution.firebase.firestore.services.DatabaseService;
import com.cse110team24.walkwalkrevolution.firebase.firestore.observers.UsersDatabaseServiceObserver;
import com.cse110team24.walkwalkrevolution.firebase.firestore.services.TeamsDatabaseService;
import com.cse110team24.walkwalkrevolution.firebase.messaging.MessagingObserver;
import com.cse110team24.walkwalkrevolution.activities.invitations.InvitationsActivity;
import com.cse110team24.walkwalkrevolution.models.invitation.Invitation;
import com.cse110team24.walkwalkrevolution.models.route.Route;
import com.cse110team24.walkwalkrevolution.models.team.ITeam;
import com.cse110team24.walkwalkrevolution.models.user.IUser;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(AndroidJUnit4.class)
public class InvitationsActivityUnitTest extends TestInjection {

    ActivityScenario<InvitationsActivity> scenario;
    SharedPreferences sp;
    Button acceptBtn;
    Button declineBtn;
    ListView invitationsListView;
    Context appContext;
    Invitation invitation;
    String TOAST_SELECT_INVITATION = "Please select an invitation";

    @Before
    public void setup() {
        super.setup();
        sp = ApplicationProvider.getApplicationContext().getSharedPreferences(HomeActivity.APP_PREF, Context.MODE_PRIVATE);
        sp.edit().putString(IUser.EMAIL_KEY, aTestUser.getEmail())
                .putString(IUser.USER_NAME_KEY, aTestUser.getDisplayName())
                .commit();
        Mockito.when(dsf.createDatabaseService(DatabaseService.Service.USERS)).thenReturn(usersDatabaseService);
        Mockito.when(dsf.createDatabaseService(DatabaseService.Service.INVITATIONS)).thenReturn(invitationsDatabaseService);
        Mockito.when(dsf.createDatabaseService(DatabaseService.Service.TEAMS)).thenReturn(teamsDatabaseService);
        Mockito.when(msf.createMessagingService(Mockito.any(), eq(invitationsDatabaseService))).thenReturn(mMsg);
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        invitation = Invitation.builder()
                .addFromUser(aTestUser)
                .addToEmail("amara@gmail.com")
                .addToDisplayName("cheery")
                .addTeamUid(aTestUser.teamUid())
                .build();

        mMsg.sendInvitation(invitation);
    }

    private void getUIFields(Activity activity) {
        acceptBtn = activity.findViewById(R.id.buttonAccept);
        declineBtn = activity.findViewById(R.id.buttonDecline);
        invitationsListView = activity.findViewById(R.id.invitationList);
    }

    @Test
    public void didNotSelectInvitation() {
        setup();
        scenario = ActivityScenario.launch(InvitationsActivity.class);
        scenario.onActivity(activity -> {
            getUIFields(activity);
            acceptBtn.performClick();
            assertEquals(TOAST_SELECT_INVITATION, ShadowToast.getTextOfLatestToast());
        });
    }

    @Test
    public void approveInvitation() {
        setup();
        scenario = ActivityScenario.launch(InvitationsActivity.class);
        scenario.onActivity(activity -> {
            getUIFields(activity);
            invitationsListView.setSelection(0);
            acceptBtn.performClick();
            assertEquals(aTestUser.teamUid(), otherUser.teamUid());
            //assertEquals(ShadowToast.getTextOfLatestToast(), "welcome to " + invitation.fromName() + "'s team");
        });
    }

    @Test
    public void declineInvitation() {
        setup();
        scenario = ActivityScenario.launch(InvitationsActivity.class);
        scenario.onActivity(activity -> {
            getUIFields(activity);
            invitationsListView.setSelection(0);
            declineBtn.performClick();
            assertNull(aTestUser.teamUid());
        });
    }

    //TODO User is already on a team...Toast must decline

}