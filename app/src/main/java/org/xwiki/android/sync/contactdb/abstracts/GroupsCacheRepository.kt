package org.xwiki.android.sync.contactdb.abstracts

import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.contactdb.UserAccountId

interface GroupsCacheRepository {
    operator fun get(id: UserAccountId): List<XWikiGroup>?
    operator fun set(id: UserAccountId, groups: List<XWikiGroup>?)
}
