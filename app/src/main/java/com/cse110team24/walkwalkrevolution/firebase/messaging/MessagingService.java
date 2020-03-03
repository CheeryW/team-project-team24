package com.cse110team24.walkwalkrevolution.firebase.messaging;

import com.cse110team24.walkwalkrevolution.models.invitation.Invitation;

public interface MessagingService extends MessagingSubject {

    void subscribeToNotificationsTopic(String topic);
    void sendInvitation(Invitation invitation);
}