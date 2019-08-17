package org.xwiki.android.sync.contactdb.dao_repositories

import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.abstracts.AllUsersCacheRepository
import org.xwiki.android.sync.contactdb.dao.AllUsersCacheDao
import org.xwiki.android.sync.contactdb.dao.set

class DAOAllUsersCacheRepository(
    private val allUsersCacheDao: AllUsersCacheDao
) : AllUsersCacheRepository {
    override fun get(id: UserAccountId): List<ObjectSummary>? = allUsersCacheDao[id] ?.allUsersList
    override fun set(id: UserAccountId, objects: List<ObjectSummary>?) {
        allUsersCacheDao[id] = objects
    }
}
