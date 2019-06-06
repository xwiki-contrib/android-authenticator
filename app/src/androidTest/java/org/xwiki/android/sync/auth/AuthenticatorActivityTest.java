package org.xwiki.android.sync.auth;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.android.sync.Constants;

/**
 * AuthenticatorActivityTest
 */
public class AuthenticatorActivityTest extends ActivityInstrumentationTestCase2<AuthenticatorActivity> {
    private Solo solo;

    public AuthenticatorActivityTest(){
        super(AuthenticatorActivity.class);
    }

    public AuthenticatorActivityTest(Class<AuthenticatorActivity> activityClass) {
        super(activityClass);
    }

    @Override
    public AuthenticatorActivity getActivity() {
        //pass data params.
        AuthenticatorActivity authenticatorActivity;
        Bundle bundle = new Bundle();
        //AccountAuthenticatorResponse response = new AccountAuthenticatorResponse(null);
        String authTokenType = Constants.AUTHTOKEN_TYPE_FULL_ACCESS + "android.uid.system";
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        bundle.putString(AuthenticatorActivity.KEY_AUTH_TOKEN_TYPE, authTokenType);
        //bundle.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        bundle.putBoolean(AuthenticatorActivity.IS_SETTING_SYNC_TYPE, false);
        authenticatorActivity = launchActivity("org.xwiki.android.sync", AuthenticatorActivity.class, bundle);
        setActivity(authenticatorActivity);
        return super.getActivity();
    }

    @Before
    public void setUp() throws Exception {
        //setUp() is run before a test case is started.
        //This is where the solo object is created.
        solo = new Solo(getInstrumentation());
        getActivity();
    }

    @After
    public void tearDown() throws Exception {
        //tearDown() is run after a test case has finished.
        //finishOpenedActivities() will finish all the activities that have been opened during the test execution.
        solo.finishOpenedActivities();
    }

    @Test
    public void testVisibleUI(){
        //Unlock the lock screen
        //solo.unlockScreen();
        //test view setting or sign
        //View passwordEditText = solo.getView(R.id.accountPassword);
        //assertTrue(passwordEditText.getVisibility() == View.VISIBLE);
    }

    @Test
    public void testSignIn(){

    }

    @Test
    public void testSignUp(){

    }


}