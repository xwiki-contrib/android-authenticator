package org.xwiki.android.sync.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.test.ActivityTestCase
import android.util.Log
import com.robotium.solo.Solo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.xwiki.android.sync.ACCOUNT_TYPE
import org.xwiki.android.sync.AUTHTOKEN_TYPE_FULL_ACCESS
import org.xwiki.android.sync.R
import org.xwiki.android.sync.appContext

/**
 * AuthenticatorActivityTest
 */

class AuthenticatorActivityTest : ActivityTestCase() {
    private var solo: Solo? = null

    public override fun getActivity(): AuthenticatorActivity {
        //pass data params.
        val authenticatorActivity: AuthenticatorActivity
        val bundle = Bundle()
        //AccountAuthenticatorResponse response = new AccountAuthenticatorResponse(null);
        val authTokenType = AUTHTOKEN_TYPE_FULL_ACCESS + "android.uid.system"
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE)
        bundle.putString(KEY_AUTH_TOKEN_TYPE, authTokenType)
        //bundle.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        bundle.putBoolean(IS_SETTING_SYNC_TYPE, false)
        bundle.putBoolean("Test", true)
        authenticatorActivity = launchActivity("org.xwiki.android.sync", AuthenticatorActivity::class.java, bundle)
        activity = authenticatorActivity
        return super.getActivity() as AuthenticatorActivity
    }

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        //setUp() is run before a test case is started.
        //This is where the solo object is created.
        solo = Solo(instrumentation)
        activity
    }

    @After
    @Throws(Exception::class)
    public override fun tearDown() {
        //tearDown() is run after a test case has finished.
        //finishOpenedActivities() will finish all the activities th`at have been opened during the test execution.
        solo!!.finishOpenedActivities()
    }

    @Test
    fun testServerUrl () {

    }

    @Test
    fun testSignUp() {
        solo!!.clickOnButton(0)
        solo!!.clickOnView(solo!!.getView(R.id.tvSignUp))
    }

    @Test
    fun testSignIn() {
        solo!!.clickOnButton(0)
        solo!!.enterText(0, "TestUser")
        solo!!.enterText(1, "test1234")
        solo!!.clickOnButton(0)
    }

    @Test
    fun testLoadingGroups() {
        solo!!.clickOnButton(0)
        solo!!.enterText(0, "TestUser")
        solo!!.enterText(1, "test1234")
        solo!!.clickOnButton(0)
        var ac : AccountManager = AccountManager.get(activity)
        var accounts : Array<Account> = ac.accounts
        for (i in accounts.iterator()) {
            when {
                i.name.equals("TestUser") -> {
                    ac.removeAccount(i, null, null)
                    Log.d("UserRemoved", i.name)
                }
            }
        }
    }
}