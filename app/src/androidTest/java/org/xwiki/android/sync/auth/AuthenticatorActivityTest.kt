package org.xwiki.android.sync.auth

import android.accounts.AccountManager
import android.content.Intent
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.xwiki.android.sync.ACCOUNT_TYPE
import org.xwiki.android.sync.AUTHTOKEN_TYPE_FULL_ACCESS
import org.xwiki.android.sync.appContext
import org.junit.Before
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import org.junit.After
import org.xwiki.android.sync.R
import org.xwiki.android.sync.utils.idlingResource

/**
 * AuthenticatorActivityTest
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class AuthenticatorActivityTest : LifecycleObserver {

    private lateinit var activityScenario: ActivityScenario<AuthenticatorActivity>

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(idlingResource)                       // Idling Resource, waits for the asynchronous call to end.
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
        activityScenario.moveToState(Lifecycle.State.STARTED)
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

    @Test
    fun testSignIn() {
        activityScenario.onActivity {
            it.showViewFlipper(0)
        }
//        onView(withId(R.id.accountServer)).perform(clearText()) // for testing on local host uncomment these line
//        onView(withId(R.id.accountServer)).perform(typeText("localhost:8080/xwiki"))
        onView(withId(R.id.btViewSignInFlipper)).perform(click())
        onView(withId(R.id.accountName))
            .perform(typeText("TestUser"))      // Test user, for log in
        onView(withId(R.id.accountPassword))
            .perform(typeText("test1234"))
        onView(withId(R.id.signInButton)).perform(click())
    }

    @Test
    fun testSignInOnLocalInstance() {
        activityScenario.onActivity {
            it.showViewFlipper(0)
        }
        onView(withId(R.id.accountServer)).perform(clearText())
        onView(withId(R.id.accountServer)).perform(typeText("http://10.0.2.2:8080/xwiki"))
        onView(withId(R.id.btViewSignInFlipper)).perform(click())
        onView(withId(R.id.accountName))
            .perform(typeText("TestUser"))      // Test user, for log in
        onView(withId(R.id.accountPassword))
            .perform(typeText("test1234"))
        onView(withId(R.id.signInButton)).perform(click())
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }
}