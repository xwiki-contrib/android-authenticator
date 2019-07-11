package org.xwiki.android.sync.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.xwiki.android.sync.contactdb.User
import org.xwiki.android.sync.contactdb.UserDatabase
import org.xwiki.android.sync.contactdb.UserRepository

class SyncSettingsViewModel (application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository

    init {
        val userDao = UserDatabase.getInstance(application).userDao()
        userRepository = UserRepository(userDao)
    }

    fun getUser(accountName: String) : LiveData<User> {
        return userRepository.findByAccountName(accountName)
    }

    fun updateUser (user: User) = viewModelScope.launch {
        userRepository.updateUser(user)
    }
}
