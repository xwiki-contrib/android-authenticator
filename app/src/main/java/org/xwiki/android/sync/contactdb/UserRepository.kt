package org.xwiki.android.sync.contactdb

import androidx.lifecycle.LiveData

class UserRepository (private val userDao: UserDao) {

    fun insert (user: User) {
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
}