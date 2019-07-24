package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.xwiki.android.sync.SYNC_TYPE_NO_NEED_SYNC

typealias UserAccountId = Long

const val USER_TABLE = "user_table"

const val UserAccountIdColumn = "id"
const val UserAccountAccountNameColumn = "account_name"
const val UserAccountServerAddressColumn = "server_address"
const val UserAccountSelectedGroupsColumn = "selected_groups"
const val UserAccountSyncTypeColumn = "sync_type"

@Entity (tableName = USER_TABLE)
data class UserAccount(
    @ColumnInfo(name = UserAccountAccountNameColumn) val accountName: String,
    @ColumnInfo(name = UserAccountServerAddressColumn) val serverAddress: String,
    @ColumnInfo(name = UserAccountSelectedGroupsColumn) var selectedGroupsList: MutableList<String> = mutableListOf(),
    @ColumnInfo(name = UserAccountSyncTypeColumn) var syncType: Int = SYNC_TYPE_NO_NEED_SYNC,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = UserAccountIdColumn)  val id: UserAccountId = 0
)