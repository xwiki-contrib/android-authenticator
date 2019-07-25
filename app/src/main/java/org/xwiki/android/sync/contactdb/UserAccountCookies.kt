package org.xwiki.android.sync.contactdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val UserAccountCookieColumn = "cookie"

@Entity(tableName = USER_TABLE)
data class UserAccountCookies(
    @PrimaryKey
    @ColumnInfo(name = UserAccountIdColumn)  val id: UserAccountId,
    @ColumnInfo(name = UserAccountCookieColumn) var cookie : String
)
