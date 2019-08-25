package org.xwiki.android.sync.activities

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleObserver
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xwiki.android.sync.ACCOUNT_TYPE
import org.xwiki.android.sync.XWIKI_DEFAULT_SERVER_ADDRESS
import org.xwiki.android.sync.appContext
import org.xwiki.android.sync.contactdb.AppDatabase
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.contactdb.dao.AccountsDao
import java.io.IOException


/**
 * SyncSettingsActivityTest
 */
@RunWith(AndroidJUnit4::class)
open class SyncSettingsActivityTest : LifecycleObserver {

    private lateinit var activityScenario: ActivityScenario<SyncSettingsActivity>
    private lateinit var accountsDao: AccountsDao
    private lateinit var db: AppDatabase

    @Before
    open fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        accountsDao = db.usersDao()

        val user = UserAccount(
            "testUser1",
            XWIKI_DEFAULT_SERVER_ADDRESS
        )

        accountsDao.insertAccount(user)


        val i = Intent(appContext, SyncSettingsActivity::class.java)
        i.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.accountName)
        i.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE)
        i.putExtra("Test", true)
    }

    @Test
    fun dummyTest() {

    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}