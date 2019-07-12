package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.xwiki.android.sync.bean.ObjectSummary
import org.xwiki.android.sync.bean.XWikiGroup

@Entity (tableName = "user_table")
data class User (
    @PrimaryKey var uid: String,
    @ColumnInfo(name = "account_name") val accountName: String?,
    @ColumnInfo(name = "server_address") val serverAddress: String?,
    @ColumnInfo(name = "sync_type") var syncType: Int = -1,
    @ColumnInfo(name = "cookie") var cookie : String = "",
    @ColumnInfo(name = "selected_groups") var selectedGroupsList: MutableList<String>?,
    @ColumnInfo(name = "groupsList") var groupsList: MutableList<XWikiGroup>,
    @ColumnInfo(name = "allUsersList") var allUsersList: MutableList<ObjectSummary>
)