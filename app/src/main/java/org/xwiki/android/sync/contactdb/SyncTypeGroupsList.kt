package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.xwiki.android.sync.bean.XWikiGroup

@Entity(tableName = "groups_list")
data class SyncTypeGroupsList(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "groupsList") var groupsList: List<XWikiGroup>
)