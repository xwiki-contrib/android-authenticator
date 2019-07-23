package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.xwiki.android.sync.GROUPS_LIST_TABLE
import org.xwiki.android.sync.bean.XWikiGroup

@Entity(tableName = GROUPS_LIST_TABLE)
data class SyncTypeGroupsList(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "groupsList") var groupsList: List<XWikiGroup>
)