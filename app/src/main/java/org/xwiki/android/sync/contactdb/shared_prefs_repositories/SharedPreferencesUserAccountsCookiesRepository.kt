package org.xwiki.android.sync.contactdb.shared_prefs_repositories

import android.content.Context
import android.content.SharedPreferences
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.abstracts.UserAccountsCookiesRepository

private const val cookiesSharedPreferences = "Cookies"

class SharedPreferencesUserAccountsCookiesRepository(
    private val context: Context
) : UserAccountsCookiesRepository {
    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences(cookiesSharedPreferences, Context.MODE_PRIVATE)

    override fun get(id: UserAccountId): String? {
        return sharedPreferences.getString(id.toString(), null)
    }

    override fun set(id: UserAccountId, cookies: String) {
        val sp = sharedPreferences
        sp.edit().putString(id.toString(), cookies).apply()
    }
}
