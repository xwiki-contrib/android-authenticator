package org.xwiki.android.authenticator.auth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.xwiki.android.authenticator.BuildConfig;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;

import static org.junit.Assert.*;

/**
 * AuthenticatorActivityJvmTest
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AuthenticatorActivityJvmTest {
    private EditText passwordEditText;

    @Before
    public void setUp() throws Exception {
        Intent intent = new Intent();
        //just for passing some param
        //AccountAuthenticatorResponse response = new AccountAuthenticatorResponse(null);
        String authTokenType = Constants.AUTHTOKEN_TYPE_FULL_ACCESS + "android.uid.system";
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        intent.putExtra(AuthenticatorActivity.KEY_AUTH_TOKEN_TYPE, authTokenType);
        //intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AuthenticatorActivity.IS_SETTING_SYNC_TYPE, false);
        Activity act = Robolectric.buildActivity(AuthenticatorActivity.class)
                .withIntent(intent)
                .get();
        passwordEditText = (EditText) act.findViewById(R.id.accountPassword);
    }

    @Test
    public void loginViewFlipper() throws Exception{
        assertTrue(passwordEditText.getVisibility() == View.VISIBLE);
    }


}