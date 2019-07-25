package org.xwiki.android.sync.contactdb.abstracts

import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.contactdb.UserAccountId

interface UserAccountsRepository {
    suspend fun createAccount(userAccount: UserAccount)

    suspend fun findByAccountName(name: String): UserAccount?
    suspend fun findByAccountId(id: UserAccountId): UserAccount?

    suspend fun updateAccount(userAccount: UserAccount)

    suspend fun deleteAccount(id: UserAccountId)
}

suspend fun UserAccountsRepository.deleteAccount(userAccount: UserAccount) = deleteAccount(userAccount.id)
