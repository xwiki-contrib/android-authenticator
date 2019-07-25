package org.xwiki.android.sync.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import kotlinx.coroutines.launch
import org.xwiki.android.sync.ACCOUNT_TYPE
import org.xwiki.android.sync.appContext
import org.xwiki.android.sync.appCoroutineScope
import org.xwiki.android.sync.userAccountsRepo

fun AccountManager.enableDetectingOfAccountsRemoving() {
    addOnAccountsUpdatedListener(
        AccountsUpdateListener(),
        null,
        true
    )
}

internal class AccountsUpdateListener : OnAccountsUpdateListener {
    override fun onAccountsUpdated(accounts: Array<Account>) {
        val accountManager = AccountManager.get(appContext)

        val internalAccounts = accountManager.getAccountsByType(ACCOUNT_TYPE).map {
            it.name
        }

        appCoroutineScope.launch {
            val allUsers = userAccountsRepo.getAll()
            allUsers.forEach {
                if (it.accountName !in internalAccounts) {
                    userAccountsRepo.deleteAccount(it.id)
                }
            }
        }
    }
}
