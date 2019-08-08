/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.android.sync

import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.xwiki.android.sync.contactdb.AppDatabase
import org.xwiki.android.sync.contactdb.UserAccount
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.abstracts.AllUsersCacheRepository
import org.xwiki.android.sync.contactdb.abstracts.GroupsCacheRepository
import org.xwiki.android.sync.contactdb.abstracts.UserAccountsCookiesRepository
import org.xwiki.android.sync.contactdb.dao_repositories.DAOUserAccountsRepository
import org.xwiki.android.sync.contactdb.abstracts.UserAccountsRepository
import org.xwiki.android.sync.contactdb.dao_repositories.DAOAllUsersCacheRepository
import org.xwiki.android.sync.contactdb.dao_repositories.DAOGroupsCacheRepository
import org.xwiki.android.sync.contactdb.shared_prefs_repositories.SharedPreferencesUserAccountsCookiesRepository
import org.xwiki.android.sync.rest.BaseApiManager
import org.xwiki.android.sync.utils.enableDetectingOfAccountsRemoving
import org.xwiki.android.sync.utils.getArrayList
import org.xwiki.android.sync.utils.putArrayList
import java.util.*

/**
 * Instance of context to use it in static methods
 * @return known AppContext instance
 */
lateinit var appContext: Context
    private set

lateinit var userAccountsRepo: UserAccountsRepository
    private set
lateinit var allUsersCacheRepository: AllUsersCacheRepository
    private set
lateinit var groupsCacheRepository: GroupsCacheRepository
    private set
lateinit var userAccountsCookiesRepo: UserAccountsCookiesRepository
    private set

private val apiManagers: MutableMap<UserAccountId, BaseApiManager> = mutableMapOf()

var selectedAccount = ""

var access_token = ""

fun resolveApiManager(serverAddress: String, userAccountId: UserAccountId): BaseApiManager = apiManagers.getOrPut(userAccountId) {
    BaseApiManager(serverAddress, userAccountId, userAccountsCookiesRepo)
}

fun resolveApiManager(userAccount: UserAccount): BaseApiManager = resolveApiManager(
    userAccount.serverAddress, userAccount.id
)

private fun initRepos(context: AppContext) {
    val appDatabase = AppDatabase.getInstance(context)
    userAccountsCookiesRepo = SharedPreferencesUserAccountsCookiesRepository(context)
    allUsersCacheRepository = DAOAllUsersCacheRepository(
        appDatabase.allUsersCacheDao()
    )
    groupsCacheRepository = DAOGroupsCacheRepository(
        appDatabase.groupsCacheDao()
    )
    userAccountsRepo = DAOUserAccountsRepository(
        appDatabase.usersDao(),
        groupsCacheRepository,
        allUsersCacheRepository,
        userAccountsCookiesRepo
    )
}

/**
 * Logging tag
 */
private const val TAG = "AppContext"

val appCoroutineScope = CoroutineScope(Dispatchers.Default)

/**
 * Add app as authorized
 *
 * @param packageName Application package name to add as authorized
 */
fun addAuthorizedApp(packageName: String) {
    Log.d(TAG, "packageName=$packageName")
    var packageList: MutableList<String>? = getArrayList(appContext.applicationContext, PACKAGE_LIST)
    if (packageList == null) {
        packageList = ArrayList()
    }
    packageList.add(packageName)
    putArrayList(appContext.applicationContext, PACKAGE_LIST, packageList)
}

/**
 * Check that application with packageName is authorised.
 *
 * @param packageName Application package name
 * @return true if application was authorized
 */
fun isAuthorizedApp(packageName: String): Boolean {
    val packageList = getArrayList(
        appContext.applicationContext,
        PACKAGE_LIST
    )
    return packageList != null && packageList.contains(packageName)
}

class AppContext : Application() {

    /**
     * Set [.instance] to this object.
     */
    override fun onCreate() {
        super.onCreate()
        appContext = this
        initRepos(this)
        AccountManager.get(this).enableDetectingOfAccountsRemoving()
        Log.d(TAG, "on create")
    }
}
