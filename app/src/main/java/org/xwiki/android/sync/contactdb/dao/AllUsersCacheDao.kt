package org.xwiki.android.sync.contactdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.contactdb.AccountAllUsersEntity
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.UserAccountIdColumn

@Dao
interface AllUsersCacheDao {
    @Query("SELECT * from ALL_USERS_LIST_TABLE WHERE $UserAccountIdColumn LIKE :id")
    operator fun get(id: UserAccountId): AccountAllUsersEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(syncTypeAccountAllUsersTable: AccountAllUsersEntity)

    operator fun set(id: UserAccountId, objects: List<ObjectSummary>) = set(
        AccountAllUsersEntity(id, objects)
    )
}