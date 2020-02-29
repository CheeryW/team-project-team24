package com.cse110team24.walkwalkrevolution.models.user;

import com.cse110team24.walkwalkrevolution.models.Builder;

import java.util.List;

public interface IUserBuilder extends Builder<IUser> {

    IUserBuilder addEmail(String email);
    IUserBuilder addInvitationsList(List<Invitation> invitations);
    IUserBuilder addDisplayName(String displayName);
    IUserBuilder addUid(String uid);
}
