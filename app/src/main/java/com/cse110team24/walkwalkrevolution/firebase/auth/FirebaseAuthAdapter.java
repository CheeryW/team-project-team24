package com.cse110team24.walkwalkrevolution.firebase.auth;

import android.util.Log;

import androidx.annotation.NonNull;

import com.cse110team24.walkwalkrevolution.models.user.IUser;
import com.cse110team24.walkwalkrevolution.models.user.FirebaseUserAdapter.Builder;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * {@inheritDoc}
 * The provider for this class is Firebase.
 */
public class FirebaseAuthAdapter implements Auth, FirebaseAuth.AuthStateListener {
    private static String TAG = "WWR_FirebaseAuthAdapter";

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private Builder mUserAdapterBuilder;
    private AuthError mAuthError;

    private boolean signUp;

    private List<AuthObserver> observers;

    public FirebaseAuthAdapter() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mUserAdapterBuilder = new Builder();
        mUserAdapterBuilder.addFirebaseUser(mFirebaseUser);
        mAuth.addAuthStateListener(this);
        observers = new ArrayList<>();
    }

    @Override
    public void signIn(String email, String password) {
        Log.i(TAG, "signIn: beginning sign in process");
        signUp = false;
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "onComplete: user sign-in successful");
                        buildUserEssentials(email);
                        mUserAdapterBuilder.addDisplayName(mFirebaseUser.getDisplayName());
                        notifyObserversSignedIn(mUserAdapterBuilder.build());
                    } else {
                        Log.e(TAG, "signIn: user sign-in failed", task.getException());
                        detectErrorType(task);
                        notifyObserversSignInError(mAuthError);
                    }
                });
    }

    @Override
    public void signUp(String email, String password, String displayName) {
        Log.i(TAG, "signUp: beginning sign up process");
        signUp = true;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "signUp: user creation successful");
                        buildUserEssentials(email);
                        setFirebaseUserDisplayName(displayName);
                        mUserAdapterBuilder.addDisplayName(displayName);
                        notifyObserversSignedUp(mUserAdapterBuilder.build());
                    } else {
                        Log.e(TAG, "signUp: user creation failed", task.getException());
                        detectErrorType(task);
                        notifyObserversSignUpError(mAuthError);
                    }
                });
    }

    private void setFirebaseUserDisplayName(String displayName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        mFirebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated.");
                    }
                });
    }

    @Override
    public IUser getUser() {
        return mUserAdapterBuilder.build();
    }

    @Override
    public boolean isUserSignedIn() {
        return mFirebaseUser != null;
    }

    @Override
    public AuthError getAuthError() {
        return mAuthError;
    }

    private void detectErrorType(Task<AuthResult> task) {
        Exception exception = task.getException();
        if (exception instanceof FirebaseAuthUserCollisionException) {
            mAuthError = AuthError.USER_COLLISION;
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            mAuthError = AuthError.DOES_NOT_EXIST;
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            mAuthError = AuthError.INVALID_PASSWORD;
        } else if (exception instanceof FirebaseNetworkException){
            mAuthError = AuthError.NETWORK_ERROR;
        }
        else {
            mAuthError = AuthError.OTHER;
        }
    }

    private void buildUserEssentials(String email) {
        mUserAdapterBuilder.addEmail(email);
        mFirebaseUser = mAuth.getCurrentUser();
        mUserAdapterBuilder.addFirebaseUser(mFirebaseUser);
        mUserAdapterBuilder.addUid(mFirebaseUser.getUid());
    }

    @Override
    public void register(AuthObserver authObserver) {
        observers.add(authObserver);
    }

    @Override
    public void deregister(AuthObserver authObserver) {
        observers.remove(authObserver);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        mFirebaseUser = firebaseAuth.getCurrentUser();
        if (isUserSignedIn()) {
            buildUserEssentials(mFirebaseUser.getEmail());
        }
    }


    @Override
    public void notifyObserversSignedIn(IUser user) {
        observers.forEach(observer -> {
            observer.onUserSignedIn(user);
        });
    }

    @Override
    public void notifyObserversSignedUp(IUser user) {
        observers.forEach(observer -> {
            observer.onUserSignedUp(user);
        });
    }

    @Override
    public void notifyObserversSignInError(AuthError error) {
        observers.forEach(observer -> {
            observer.onAuthSignInError(error);
        });
    }

    @Override
    public void notifyObserversSignUpError(AuthError error) {
        observers.forEach(observer -> {
            observer.onAuthSignUpError(error);
        });
    }
}
