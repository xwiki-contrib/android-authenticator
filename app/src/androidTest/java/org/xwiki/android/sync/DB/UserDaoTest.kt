package org.xwiki.android.sync.DB

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.xwiki.android.sync.contactdb.User
import org.xwiki.android.sync.contactdb.UserDao
import org.xwiki.android.sync.contactdb.UserDatabase
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var userDao: UserDao
    private lateinit var db: UserDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build()
        userDao = db.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetUser() = runBlocking {
        val user = User("testUser1@https://www.xwiki.org/xwiki",
            "testUser1",
            "https://www.xwiki.org/xwiki",
            0, "",
            arrayListOf(),
            arrayListOf(),
            arrayListOf())
        userDao.insertAccount(user)
        val allUsers = userDao.getAllAccount().waitForValue()
        assertEquals(allUsers[0].accountName, user.accountName)
    }

    @Test
    @Throws(Exception::class)
    fun getAllUsers() = runBlocking {
        val user1 = User("testUser1@https://www.xwiki.org/xwiki",
            "testUser1",
            "https://www.xwiki.org/xwiki",
            0, "",
            arrayListOf(),
            arrayListOf(),
            arrayListOf())
        userDao.insertAccount(user1)
        val user2 = User("testUser2@https://www.xwiki.org/xwiki",
            "testUser2",
            "https://www.xwiki.org/xwiki",
            0, "",
            arrayListOf(),
            arrayListOf(),
            arrayListOf())
        userDao.insertAccount(user2)
        val allUsers = userDao.getAllAccount().waitForValue()
        assertEquals(allUsers[0].accountName, user1.accountName)
        assertEquals(allUsers[1].accountName, user2.accountName)
    }

    @Test
    @Throws(Exception::class)
    fun deleteAllUsers() = runBlocking {
        val user1 = User("testUser1@https://www.xwiki.org/xwiki",
            "testUser2",
            "https://www.xwiki.org/xwiki",
            0, "",
            arrayListOf(),
            arrayListOf(),
            arrayListOf())
        userDao.insertAccount(user1)
        val user2 = User("testUser2@https://www.xwiki.org/xwiki",
            "testUser2",
            "https://www.xwiki.org/xwiki",
            0, "",
            arrayListOf(),
            arrayListOf(),
            arrayListOf())
        userDao.insertAccount(user1)
        userDao.deleteAccount(user1)
        userDao.deleteAccount(user2)
        val allUsers = userDao.getAllAccount().waitForValue()
        assertTrue(allUsers.isEmpty())
    }
}
