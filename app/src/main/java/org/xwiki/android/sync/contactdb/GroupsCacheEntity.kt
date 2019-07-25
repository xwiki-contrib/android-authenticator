package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.xwiki.android.sync.bean.XWikiGroup

const val GROUPS_LIST_TABLE = "groups_list_table"
const val GroupsListColumn = "groupsList"

@Entity(tableName = GROUPS_LIST_TABLE)
data class GroupsCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = UserAccountIdColumn) val id: UserAccountId,
    @ColumnInfo(name = GroupsListColumn) var groupsList: List<XWikiGroup>
)