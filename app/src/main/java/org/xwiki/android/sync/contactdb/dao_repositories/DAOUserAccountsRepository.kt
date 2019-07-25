package org.xwiki.android.sync.contactdb.dao_repositories

import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.abstracts.UserAccountsRepository
import org.xwiki.android.sync.contactdb.dao.AccountsDao

class DAOUserAccountsRepository (
    private val accountsDao: AccountsDao
) : UserAccountsRepository {
    override suspend fun createAccount(userAccount: UserAccount): UserAccount? {
        val id = accountsDao.insertAccount(userAccount)
        return accountsDao.findById(id)
    }

    override suspend fun findByAccountName(name: String): UserAccount? = accountsDao.findByAccountName(name)
    override suspend fun findByAccountId(id: UserAccountId): UserAccount? = accountsDao.findById(id)

    override suspend fun updateAccount(userAccount: UserAccount) {
        accountsDao.updateUser(userAccount)
    }

    override suspend fun deleteAccount(id: UserAccountId) {
        accountsDao.deleteUser(id)
    }
}