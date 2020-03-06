package com.cse110team24.walkwalkrevolution.mockedservices;

import com.cse110team24.walkwalkrevolution.firebase.auth.AuthService;
import com.cse110team24.walkwalkrevolution.firebase.auth.AuthServiceFactory;
import com.cse110team24.walkwalkrevolution.firebase.auth.AuthServiceObserver;
import com.cse110team24.walkwalkrevolution.models.user.IUser;

import java.util.ArrayList;
import java.util.List;

public class TestAuth {

    /** TODO
     * returns instance of TestAuthService. See blow
     */
    public static class TestAuthAuthServiceFactory implements AuthServiceFactory {

        @Override
        public AuthService createAuthService() {
            return new TestAuthService();
        }
    }


    // set these when you need to.
    public static IUser testAuthUser;
    public static AuthService.AuthError testAuthError;
    public static boolean isTestUserSignedIn;

    public static boolean successUserSignedUp;
    public static boolean successUserSignedIn;

    /** TODO
     * instead of having observers, you should manually call the observer's method
     * For example, if testing a user is successfully signed in, the method
     * [observer].onUserSignedIn(testUser) should be called
     */
    public static class TestAuthService implements AuthService{
        private static final String TAG = "[WWR_TestAuthService]";
        AuthServiceObserver observer;
        @Override
        public void signIn(String email, String password) {
            /** TODO: 3/5/20 should call one of
             * [observer].onUserSignedUp(testAuthUser)
             * [observer].onAuthSignUpError(testAuthError)
             */
            if (successUserSignedIn) {
                observer.onUserSignedIn(testAuthUser);
            } else {
                observer.onAuthSignInError(testAuthError);
            }
        }

        @Override
        public void signUp(String email, String password, String displayName) {
            /** TODO: 3/5/20 should call one of
             * [observer].onUserSignedUp(testAuthUser)
             * [observer].onAuthSignUpError(testAuthError)
             */

            System.out.println(TAG + ": signUp: called with email " + email + " and name " + displayName);
            if (successUserSignedUp) {
                observer.onUserSignedUp(testAuthUser);
            } else {
                observer.onAuthSignUpError(testAuthError);
            }
        }

        @Override
        public IUser getUser() {
            return testAuthUser;
        }

        @Override
        public AuthError getAuthError() {
            return testAuthError;
        }

        @Override
        public boolean isUserSignedIn() {
            return isTestUserSignedIn;
        }

        @Override
        public void notifyObserversSignedIn(IUser user) {

        }

        @Override
        public void notifyObserversSignedUp(IUser user) {

        }

        @Override
        public void notifyObserversSignInError(AuthError error) {

        }

        @Override
        public void notifyObserversSignUpError(AuthError error) {

        }

        @Override
        public void register(AuthServiceObserver authServiceObserver) {
            observer = authServiceObserver;
        }

        @Override
        public void deregister(AuthServiceObserver authServiceObserver) {

        }
    }
}
