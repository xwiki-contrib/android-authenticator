package org.xwiki.android.sync.contactdb

import androidx.lifecycle.LiveData

class AppRepository (
    private val userDao: UserDao,
    private val syncTypeAllUsersDao: SyncTypeAllUsersDao?,
    private val syncTypeGroupsListDao: SyncTypeGroupsListDao?
) {

    fun insertUser (user: User) {
        userDao.insertAccount(user)
    }

    suspend fun updateUser (user: User) {
        return userDao.updateUser(user)
    }

    fun findByAccountName(name: String): LiveData<User> {
        return userDao.findByAccountName(name)
    }

    fun getAccountByName (name: String) : User {
        return userDao.getAccountByName(name)
    }

    fun deleteUser (user: User) {
        return userDao.deleteAccount(user)
    }

    suspend fun insertSyncTypeAllUsersList (syncTypeAllUsersList: SyncTypeAllUsersList): Unit? {
        return syncTypeAllUsersDao?.insertList(syncTypeAllUsersList)
    }

    fun getSyncTypeAllUsersList (): LiveData<List<SyncTypeAllUsersList>>? {
        return syncTypeAllUsersDao?.getList()
    }

    suspend fun insertSyncTypeGroupsList (syncTypeGroupsList: SyncTypeGroupsList): Unit? {
        return syncTypeGroupsListDao?.insertList(syncTypeGroupsList)
    }

    fun getSyncTypeGroupsList (): LiveData<List<SyncTypeGroupsList>>? {
        return syncTypeGroupsListDao?.getAllUsersList()
    }
}