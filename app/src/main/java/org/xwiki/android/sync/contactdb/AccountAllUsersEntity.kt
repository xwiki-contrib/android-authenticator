package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.xwiki.android.sync.bean.ObjectSummary

const val ALL_USERS_LIST_TABLE = "all_users_list_table"
const val AllUsersListColumn = "dataList"

@Entity(tableName = ALL_USERS_LIST_TABLE)
data class AccountAllUsersEntity(
    @PrimaryKey
    @ColumnInfo(name = UserAccountIdColumn) val id: UserAccountId,
    @ColumnInfo(name = AllUsersListColumn) var allUsersList: List<ObjectSummary>
)