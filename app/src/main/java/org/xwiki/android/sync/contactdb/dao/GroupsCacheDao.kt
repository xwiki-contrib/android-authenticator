package org.xwiki.android.sync.contactdb.dao

import androidx.room.*
import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.contactdb.GROUPS_LIST_TABLE
import org.xwiki.android.sync.contactdb.GroupsCacheEntity
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.UserAccountIdColumn

@Dao
interface GroupsCacheDao {
    @Query("SELECT * from $GROUPS_LIST_TABLE where $UserAccountIdColumn LIKE :id")
    operator fun get(id: UserAccountId) : GroupsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(groupsCacheEntity: GroupsCacheEntity)

    @Query ("DELETE FROM $GROUPS_LIST_TABLE WHERE $UserAccountIdColumn = :id")
    fun remove(id: UserAccountId)
}

operator fun GroupsCacheDao.set(id: UserAccountId, groups: List<XWikiGroup>?) = groups ?.let {
    set(GroupsCacheEntity(id, groups))
} ?: remove(id)
