package org.xwiki.android.sync.contactdb.abstracts

import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.contactdb.UserAccountId

interface AllUsersCacheRepository {
    operator fun get(id: UserAccountId): List<ObjectSummary>?
    operator fun set(id: UserAccountId, objects: List<ObjectSummary>?)
}
