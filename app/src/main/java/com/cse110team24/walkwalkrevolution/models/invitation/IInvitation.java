package com.cse110team24.walkwalkrevolution.models.invitation;

import com.cse110team24.walkwalkrevolution.models.user.IUser;

import java.util.Map;

public interface IInvitation {
    public IUser fromUser();
    public String fromEmail();
    public String fromName();
    public String fromDocumentKey();
    public String toEmail();
    public String toName();
    public String toDocumentKey();
    public String getTeamUid();
    public InvitationStatus status();
    public void setStatus(InvitationStatus status);
    public String uid();
    public void setUid(String uid);
    public Map<String, Object> invitationData();
}
