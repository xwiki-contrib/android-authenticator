package org.xwiki.android.sync.contactdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyncTypeGroupsListDao {
    @Query("SELECT * from GROUPS_LIST_TABLE")
    fun getAllUsersList () : LiveData<List<SyncTypeGroupsList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList (syncTypeGroupsList: SyncTypeGroupsList)
}