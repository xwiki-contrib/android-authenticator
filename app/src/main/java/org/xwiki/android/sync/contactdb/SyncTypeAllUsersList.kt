package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.xwiki.android.sync.bean.ObjectSummary

@Entity (tableName = "all_users_list")
data class SyncTypeAllUsersList(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "allUsersList") var allUsersList: List<ObjectSummary>
)