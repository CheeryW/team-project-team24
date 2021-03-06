package com.cse110team24.walkwalkrevolution.models.user;

import com.cse110team24.walkwalkrevolution.models.team.walk.TeammateStatus;
import com.cse110team24.walkwalkrevolution.utils.Builder;
import com.cse110team24.walkwalkrevolution.models.invitation.Invitation;

import java.util.List;

public interface UserBuilder extends Builder<IUser> {

    UserBuilder addEmail(String email);
    UserBuilder addInvitationsList(List<Invitation> invitations);
    UserBuilder addDisplayName(String displayName);
    UserBuilder addUid(String uid);
    UserBuilder addTeamUid(String teamUid);
    UserBuilder addLatestWalkStatus(TeammateStatus status);
}
