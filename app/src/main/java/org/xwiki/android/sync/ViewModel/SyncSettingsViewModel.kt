package org.xwiki.android.sync.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
    application: Application
) : AndroidViewModel(application) {
    fun updateUser(updatedUserAccount: UserAccount) {
        viewModelScope.launch(Dispatchers.Default) {
            userAccountsRepo.updateAccount(updatedUserAccount)
        }
    }

    fun updateAllUsersCache(summaries: List<ObjectSummary>, id: UserAccountId) {
        viewModelScope.launch(Dispatchers.Default) {
            allUsersCacheRepository[id] = summaries
        }
    }

    fun getAllUsersCache(id: UserAccountId): List<ObjectSummary>? {
        return allUsersCacheRepository[id]
    }

    fun updateGroupsCache(cache: List<XWikiGroup>, id: UserAccountId) {
        viewModelScope.launch(Dispatchers.Default) {
            groupsCacheRepository[id] = cache
        }
    }

    fun getGroupsCache(id: UserAccountId): List<XWikiGroup>? {
        return groupsCacheRepository[id]
    }
}
