package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.xwiki.android.sync.ALL_USERS_LIST_TABLE
import org.xwiki.android.sync.bean.ObjectSummary

@Entity (tableName = ALL_USERS_LIST_TABLE)
data class SyncTypeAllUsersList(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "allUsersList") var allUsersList: List<ObjectSummary>
)