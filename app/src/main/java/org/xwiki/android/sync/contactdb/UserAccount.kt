package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    @ColumnInfo(name = UserAccountSyncTypeColumn) var syncType: Int = -1,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = UserAccountIdColumn)  val id: UserAccountId = 0
)