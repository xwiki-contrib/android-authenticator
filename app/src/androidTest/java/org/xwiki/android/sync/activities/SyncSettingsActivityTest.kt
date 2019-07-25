package org.xwiki.android.sync.activities

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleObserver
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xwiki.android.sync.ACCOUNT_TYPE
import org.xwiki.android.sync.R
import org.xwiki.android.sync.XWIKI_DEFAULT_SERVER_ADDRESS
import org.xwiki.android.sync.appContext
import org.xwiki.android.sync.contactdb.AppDatabase
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.utils.idlingResource


/**
 * AuthenticatorActivityTest
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
open class SyncSettingsActivityTest : LifecycleObserver {

    private lateinit var activityScenario: ActivityScenario<SyncSettingsActivity>

    @Before
    open fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        val userDao = db.usersDao()

        val user = UserAccount(
            "testUser1",
            XWIKI_DEFAULT_SERVER_ADDRESS
        )

        userDao.insertAccount(user)


        IdlingRegistry.getInstance().register(idlingResource)
        val i = Intent(appContext, SyncSettingsActivity::class.java)
        i.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.accountName)
        i.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE)
        i.putExtra("Test", true)

        activityScenario = ActivityScenario.launch(i)
    }

    @Test
    fun testLoadingGroups() {
        onView(withId(R.id.nextButton)).check(matches(withText(R.string.save)))
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }
}