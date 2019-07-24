package org.xwiki.android.sync.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.xwiki.android.sync.allUsersCacheRepository
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.contactdb.*
import org.xwiki.android.sync.groupsCacheRepository
import org.xwiki.android.sync.userAccountsRepo

class SyncSettingsViewModelFactory(
    private val application: Application,
    private val userAccountId: UserAccountId
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass == SyncSettingsViewModel::class.java) {
            SyncSettingsViewModel(application, userAccountId) as T
        } else {
            super.create(modelClass)
        }
    }
}

class SyncSettingsViewModel(
    application: Application,
    private val id: UserAccountId
) : AndroidViewModel(application) {
    suspend fun getUser() : UserAccount? {
        return userAccountsRepo.findByAccountId(id)
    }

    fun updateUser(updatedUserAccount: UserAccount) {
        viewModelScope.launch(Dispatchers.Default) {
            if (updatedUserAccount.id == id) {
                userAccountsRepo.updateAccount(updatedUserAccount)
            }
        }
    }

    fun updateAllUsersCache(summaries: List<ObjectSummary>) {
        viewModelScope.launch(Dispatchers.Default) {
            allUsersCacheRepository[id] = summaries
        }
    }

    fun getAllUsersCache(): List<ObjectSummary>? {
        return allUsersCacheRepository[id]
    }

    fun updateGroupsCache(cache: List<XWikiGroup>) {
        viewModelScope.launch(Dispatchers.Default) {
            groupsCacheRepository[id] = cache
        }
    }

    fun getGroupsCache(): List<XWikiGroup>? {
        return groupsCacheRepository[id]
    }
}
