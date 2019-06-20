package org.xwiki.android.sync.activities

import androidx.lifecycle.LifecycleObserver
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xwiki.android.sync.appContext
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.After
import org.xwiki.android.sync.R
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
        IdlingRegistry.getInstance().register(idlingResource)
        activityScenario = ActivityScenario.launch(SyncSettingsActivity::class.java)
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