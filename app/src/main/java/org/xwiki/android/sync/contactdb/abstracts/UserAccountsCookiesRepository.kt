package org.xwiki.android.sync.contactdb.abstracts

import org.xwiki.android.sync.contactdb.UserAccountId

interface UserAccountsCookiesRepository {
    operator fun get(id: UserAccountId): String?
    operator fun set(id: UserAccountId, cookies: String?)
}
