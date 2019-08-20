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
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.groupsCacheRepository
import org.xwiki.android.sync.userAccountsRepo

class SyncSettingsViewModel(
    application: Application,
    private val id: UserAccountId
) : AndroidViewModel(application) {
    suspend fun getUser() : UserAccount? {
        return userAccountsRepo.findByAccountId(id)
    }

    fun updateUser(updatedUserAccount: UserAccount) {
        viewModelScope.launch(Dispatchers.Default) {
            userAccountsRepo.updateAccount(updatedUserAccount)
        }
    }

    fun updateAllUsersCache(summaries: List<ObjectSummary>, userID: UserAccountId) {
        viewModelScope.launch(Dispatchers.Default) {
            allUsersCacheRepository[userID] = summaries
        }
    }

    fun getAllUsersCache(userID: UserAccountId): List<ObjectSummary>? {
        return allUsersCacheRepository[userID]
    }

    fun updateGroupsCache(cache: List<XWikiGroup>, userID: UserAccountId) {
        viewModelScope.launch(Dispatchers.Default) {
            groupsCacheRepository[userID] = cache
        }
    }

    fun getGroupsCache(userID: UserAccountId): List<XWikiGroup>? {
        return groupsCacheRepository[userID]
    }
}
