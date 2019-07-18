package org.xwiki.android.sync.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.xwiki.android.sync.contactdb.*

class SyncSettingsViewModel (application: Application) : AndroidViewModel(application) {

    private val appRepository: AppRepository

    init {
        val appDatabase = AppDatabase.getInstance(application)
        val userDao = appDatabase.userDao()
        val syncTypeAllUsersDao = appDatabase.syncTypeAllUsersListDao()
        val syncTypeGroupsListDao = appDatabase.syncTypeGroupsListDao()
        appRepository = AppRepository(userDao, syncTypeAllUsersDao, syncTypeGroupsListDao)
    }

    fun getUser(accountName: String) : LiveData<User> {
        return appRepository.findByAccountName(accountName)
    }

    fun updateUser (user: User) = viewModelScope.launch {
        appRepository.updateUser(user)
    }

    fun insertSyncTypeAllUsersList (list: SyncTypeAllUsersList) = viewModelScope.launch{
        appRepository.insertSyncTypeAllUsersList(list)
    }

    fun getSyncTypeAllUsersList (): LiveData<List<SyncTypeAllUsersList>>? {
        return appRepository.getSyncTypeAllUsersList()
    }

    fun insertSyncTypeGroupsList (list: SyncTypeGroupsList) = viewModelScope.launch{
        appRepository.insertSyncTypeGroupsList(list)
    }

    fun getSyncTypeGroupsList (): LiveData<List<SyncTypeGroupsList>>? {
        return appRepository.getSyncTypeGroupsList()
    }
}
