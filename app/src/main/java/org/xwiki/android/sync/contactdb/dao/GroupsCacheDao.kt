package org.xwiki.android.sync.contactdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.xwiki.android.sync.bean.XWikiGroup
import org.xwiki.android.sync.contactdb.GroupsCacheEntity
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.UserAccountIdColumn

@Dao
interface GroupsCacheDao {
    @Query("SELECT * from GROUPS_LIST_TABLE where $UserAccountIdColumn LIKE :id")
    operator fun get(id: UserAccountId) : GroupsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(groupsCacheEntity: GroupsCacheEntity)
}

operator fun GroupsCacheDao.set(id: UserAccountId, groups: List<XWikiGroup>) = set(
    GroupsCacheEntity(id, groups)
)
