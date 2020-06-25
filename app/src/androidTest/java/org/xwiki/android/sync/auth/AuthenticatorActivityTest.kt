package org.xwiki.android.sync.auth

import android.accounts.AccountManager
import android.content.Intent
import android.util.Log
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import junit.framework.TestCase
import okhttp3.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xwiki.android.sync.*
import java.io.IOException
import java.net.URL

/**
 * AuthenticatorActivityTest
 */

private const val TEST_USERNAME = "aa700"

private const val TEST_PASSWORD = "a7890"

@RunWith(AndroidJUnit4::class)
@MediumTest
class AuthenticatorActivityTest : LifecycleObserver {

    private lateinit var activityScenario: ActivityScenario<AuthenticatorActivity>

    @Before
    fun setUp() {
        val i = Intent(appContext, AuthenticatorActivity::class.java)
        val authTokenType = AUTHTOKEN_TYPE_FULL_ACCESS + "android.uid.system"
        i.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE)
        i.putExtra(KEY_AUTH_TOKEN_TYPE, authTokenType)
        i.putExtra(IS_SETTING_SYNC_TYPE, false)
        i.putExtra("Test", true)
        activityScenario = ActivityScenario.launch(i)
    }

    @Test
    fun testServerUrl () {
        activityScenario.onActivity {
            it.showViewFlipper(0)
        }
        activityScenario.close()
    }

    @Test
    fun testSignUp() {
        activityScenario.onActivity {
            it.showViewFlipper(1)
            it.signUp(it.binding.viewFlipper[1])
        }
        activityScenario.moveToState(Lifecycle.State.STARTED)
        activityScenario.close()
    }

    /*@Test
    fun testSignIn() {
        activityScenario.onActivity {
            it.showViewFlipper(0)
        }
        onView(withId(R.id.btViewSignInFlipper)).perform(click())
        onView(withId(R.id.accountName))
            .perform(typeText(TEST_USERNAME))      // Test user, for log in
        onView(withId(R.id.accountPassword))
            .perform(typeText(TEST_PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.signInButton)).perform(click())
    }*/

    @Test
    fun checkOIDCSupport() {
        activityScenario.moveToState(Lifecycle.State.STARTED)

        val url = URL ("$XWIKI_DEFAULT_SERVER_ADDRESS/oidc/")

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                println(response)
                TestCase.assertTrue(response.isSuccessful)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("Test", e.localizedMessage)
            }
        })
    }

    @After
    fun closeActivity() {
        activityScenario.close()
    }
}