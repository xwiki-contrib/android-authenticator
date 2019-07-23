package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.xwiki.android.sync.USER_TABLE

@Entity (tableName = USER_TABLE, primaryKeys = ["account_name", "server_address"])
data class User(
    @ColumnInfo(name = "account_name") val accountName: String,
    @ColumnInfo(name = "server_address") val serverAddress: String,
    @ColumnInfo(name = "sync_type") var syncType: Int = -1,
    @ColumnInfo(name = "cookie") var cookie : String = "",
    @ColumnInfo(name = "selected_groups") var selectedGroupsList: MutableList<String>
)