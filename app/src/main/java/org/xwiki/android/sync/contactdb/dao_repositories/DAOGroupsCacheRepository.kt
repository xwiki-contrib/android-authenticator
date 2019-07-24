package org.xwiki.android.sync.contactdb.dao_repositories

import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.abstracts.GroupsCacheRepository
import org.xwiki.android.sync.contactdb.dao.GroupsCacheDao
import org.xwiki.android.sync.contactdb.dao.set

class DAOGroupsCacheRepository(
    private val groupsCacheDao: GroupsCacheDao
) : GroupsCacheRepository {
    override fun get(id: UserAccountId): List<XWikiGroup>? = groupsCacheDao[id] ?.groupsList
    override fun set(id: UserAccountId, groups: List<XWikiGroup>) {
        groupsCacheDao[id] = groups
    }
}
