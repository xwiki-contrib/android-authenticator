package org.xwiki.android.sync.utils

import kotlinx.coroutines.async
import org.xwiki.android.sync.SYNC_TYPE_NO_NEED_SYNC
import org.xwiki.android.sync.XWIKI_DEFAULT_SERVER_ADDRESS
import org.xwiki.android.sync.appCoroutineScope
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.userAccountsRepo

fun getUserAccountById(accountId: UserAccountId): UserAccount? {
    return appCoroutineScope.async {
        userAccountsRepo.findByAccountId(accountId)
    }.awaitBlocking(
        appCoroutineScope
    )
}

fun getUserAccountByAccountName(accountName: String): UserAccount? {
    return appCoroutineScope.async {
        userAccountsRepo.findByAccountName(accountName)
    }.awaitBlocking(
        appCoroutineScope
    )
}

fun removeUser (accountID: Long) {
    appCoroutineScope.async {
        userAccountsRepo.deleteAccount(accountID)
    }.awaitBlocking(
        appCoroutineScope
    )
}

fun getUserSyncType(accountId: UserAccountId): Int {
    return getUserAccountById(accountId) ?.syncType ?: SYNC_TYPE_NO_NEED_SYNC
}

fun getUserAccountName(accountId: UserAccountId): String? {
    return getUserAccountById(accountId) ?.accountName
}

fun getUserServer(accountName: String?): String {
    return accountName ?.let {
        getUserAccountByAccountName(accountName) ?.serverAddress
    } ?: XWIKI_DEFAULT_SERVER_ADDRESS
}
