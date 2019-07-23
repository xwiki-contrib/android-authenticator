package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity (tableName = "user_table", primaryKeys = ["account_name", "server_address"])
data class User(
    @ColumnInfo(name = "account_name") val accountName: String,
    @ColumnInfo(name = "server_address") val serverAddress: String,
    @ColumnInfo(name = "sync_type") var syncType: Int = -1,
    @ColumnInfo(name = "cookie") var cookie : String = "",
    @ColumnInfo(name = "selected_groups") var selectedGroupsList: MutableList<String>
)