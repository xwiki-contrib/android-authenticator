package org.xwiki.android.sync.DB

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.xwiki.android.sync.XWIKI_DEFAULT_SERVER_ADDRESS
import org.xwiki.android.sync.contactdb.AppDatabase
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.contactdb.dao.AccountsDao
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AccountsDaoTest {

    private lateinit var accountsDao: AccountsDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build()
        accountsDao = db.usersDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetUser() = runBlocking {
        val user = UserAccount(
            "testUser1",
            XWIKI_DEFAULT_SERVER_ADDRESS
        )
        accountsDao.insertAccount(user)
        val allUsers = accountsDao.getAllAccount()
        assertEquals(allUsers[0].accountName, user.accountName)
    }

    @Test
    @Throws(Exception::class)
    fun getAllUsers() = runBlocking {
        val user1 = UserAccount(
            "testUser1",
            XWIKI_DEFAULT_SERVER_ADDRESS
        )
        accountsDao.insertAccount(user1)
        val user2 = UserAccount(
            "testUser2",
            XWIKI_DEFAULT_SERVER_ADDRESS
        )
        accountsDao.insertAccount(user2)
        val allUsers = accountsDao.getAllAccount()
        assertEquals(allUsers[0].accountName, user1.accountName)
        assertEquals(allUsers[1].accountName, user2.accountName)
    }

    @Test
    @Throws(Exception::class)
    fun deleteAllUsers() = runBlocking {
        val user1Id = accountsDao.insertAccount(
            UserAccount(
                "testUser1",
                XWIKI_DEFAULT_SERVER_ADDRESS
            )
        )
        val user2Id = accountsDao.insertAccount(
            UserAccount(
                "testUser2",
                XWIKI_DEFAULT_SERVER_ADDRESS
            )
        )
        accountsDao.deleteUser(user1Id)
        accountsDao.deleteUser(user2Id)
        val allUsers = accountsDao.getAllAccount()
        assertTrue(allUsers.isEmpty())
    }
}
