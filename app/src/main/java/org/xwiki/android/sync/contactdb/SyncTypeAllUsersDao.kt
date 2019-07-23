package org.xwiki.android.sync.contactdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyncTypeAllUsersDao {
    @Query("SELECT * from ALL_USERS_LIST_TABLE")
    fun getList () : LiveData<List<SyncTypeAllUsersList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList (syncTypeAllUsersList: SyncTypeAllUsersList)
}