const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendInviteNotification = functions.firestore
    //.document('users/{user}/invitations/{inviteId}')
    .document('invitations/{userInvite}/userInvitations/{inviteId}')
    .onCreate((snap, context) => {
        const document = snap.exists ? snap.data() : null;

        if (document) {
            var message = {
                notification: {
                    title: document.from + ' has invited you to join a team!',
                    body: 'Click to accept or decline this invitation'
                },
                topic: context.params.inviteId
            };

            return admin.messaging().send(message)
                .then((response) => {
                    console.log('Successfully sent invite:', document);
                    return response;
                })
                .catch((error) => {
                    console.log('Error sending invite:', error);
                    return error;
                });
        }

        return "document was null or empty";
    });