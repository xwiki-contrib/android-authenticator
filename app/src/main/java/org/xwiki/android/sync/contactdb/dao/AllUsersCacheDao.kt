package org.xwiki.android.sync.contactdb.dao

import androidx.room.*
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.contactdb.ALL_USERS_LIST_TABLE
import org.xwiki.android.sync.contactdb.AccountAllUsersEntity
import org.xwiki.android.sync.contactdb.UserAccountId
import org.xwiki.android.sync.contactdb.UserAccountIdColumn

@Dao
interface AllUsersCacheDao {
    @Query("SELECT * from $ALL_USERS_LIST_TABLE WHERE $UserAccountIdColumn LIKE :id")
    operator fun get(id: UserAccountId): AccountAllUsersEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(syncTypeAccountAllUsersTable: AccountAllUsersEntity)

    @Query ("DELETE FROM $ALL_USERS_LIST_TABLE WHERE $UserAccountIdColumn = :id")
    fun remove(id: UserAccountId)
}

operator fun AllUsersCacheDao.set(id: UserAccountId, objects: List<ObjectSummary>?) = objects ?.let {
    set(AccountAllUsersEntity(id, objects))
} ?: remove(id)