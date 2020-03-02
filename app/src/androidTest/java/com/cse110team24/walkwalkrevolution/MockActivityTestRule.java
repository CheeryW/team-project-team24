package com.cse110team24.walkwalkrevolution;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.test.rule.ActivityTestRule;

import com.cse110team24.walkwalkrevolution.firebase.auth.AuthService;
import com.cse110team24.walkwalkrevolution.firebase.auth.FirebaseAuthServiceFactory;
import com.cse110team24.walkwalkrevolution.firebase.auth.AuthServiceObserver;
import com.cse110team24.walkwalkrevolution.firebase.firestore.DatabaseService;
import com.cse110team24.walkwalkrevolution.firebase.firestore.FirestoreDatabaseServiceFactory;
import com.cse110team24.walkwalkrevolution.firebase.messaging.MessagingService;
import com.cse110team24.walkwalkrevolution.firebase.messaging.FirebaseMessagingServiceFactory;
import com.cse110team24.walkwalkrevolution.models.user.FirebaseUserAdapter;
import com.cse110team24.walkwalkrevolution.models.user.IUser;

import java.util.ArrayList;
import java.util.List;

public class MockActivityTestRule<T extends Activity> extends ActivityTestRule<T> {

    protected AuthService.AuthError nextError = AuthService.AuthError.OTHER;
    protected boolean nextSignIn = false;
    protected boolean nextSuccessStatus = false;

    protected AuthService mAuth;
    protected DatabaseService mDb;
    protected MessagingService mMsg;
    protected FirestoreDatabaseServiceFactory dsf;
    protected FirebaseMessagingServiceFactory msf;
    protected FirebaseAuthServiceFactory asf;

    MockActivityTestRule(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected Intent getActivityIntent() {

        return super.getActivityIntent();
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();

        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getSharedPreferences(HomeActivity.HEIGHT_PREF, Context.MODE_PRIVATE)
                .edit()
                .remove(HomeActivity.HEIGHT_FT_KEY)
                .remove(HomeActivity.HEIGHT_IN_KEY)
                .apply();
    }

    public class TestAuthService implements AuthService {
        IUser signedInUser = new FirebaseUserAdapter.Builder()
                .addDisplayName("test")
                .addEmail("tester@gmail.com")
                .addUid("1")
                .build();

        List<AuthServiceObserver> authServiceObservers = new ArrayList<>();

        @Override
        public void signIn(String email, String password) {

            if (nextSuccessStatus) {
                notifyObserversSignedIn(signedInUser);
            } else {
                notifyObserversSignInError(nextError);
            }
        }

        @Override
        public void signUp(String email, String password, String displayName) {
            if (nextSuccessStatus) {
                notifyObserversSignedUp(signedInUser);
            } else {
                notifyObserversSignUpError(nextError);
            }
        }

        @Override
        public IUser getUser() {
            return signedInUser;
        }

        @Override
        public AuthError getAuthError() {
            return null;
        }

        @Override
        public boolean isUserSignedIn() {
            return nextSignIn;
        }

        @Override
        public void notifyObserversSignedIn(IUser user) {
            authServiceObservers.forEach(observer -> {
                observer.onUserSignedIn(user);
            });
        }

        @Override
        public void notifyObserversSignedUp(IUser user) {
            authServiceObservers.forEach(observer -> {
                observer.onUserSignedUp(user);
            });
        }

        @Override
        public void notifyObserversSignInError(AuthError error) {
            authServiceObservers.forEach(observer -> {
                observer.onAuthSignInError(nextError);
            });
        }

        @Override
        public void notifyObserversSignUpError(AuthError error) {
            authServiceObservers.forEach(observer -> {
                observer.onAuthSignUpError(nextError);
            });
        }

        @Override
        public void register(AuthServiceObserver authServiceObserver) {
            authServiceObservers.add(authServiceObserver);
        }

        @Override
        public void deregister(AuthServiceObserver authServiceObserver) {

        }
    }
}