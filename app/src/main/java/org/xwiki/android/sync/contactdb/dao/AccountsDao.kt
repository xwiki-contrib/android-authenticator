package org.xwiki.android.sync.contactdb.dao

import androidx.room.*
import org.xwiki.android.sync.contactdb.*

@Dao
interface AccountsDao {
    @Query ("SELECT * from USER_TABLE")
    fun getAllAccount() : List<UserAccount>

    @Query ("SELECT * FROM USER_TABLE WHERE $UserAccountAccountNameColumn LIKE :name")
    fun findByAccountName(name: String): UserAccount?

    @Query ("SELECT * FROM USER_TABLE WHERE $UserAccountIdColumn=:id")
    fun findById(id: UserAccountId): UserAccount?

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertAccount(userAccount: UserAccount): UserAccountId

    @Update (onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(userAccount: UserAccount): Int

    @Query ("DELETE FROM USER_TABLE WHERE $UserAccountIdColumn = :userAccountId")
    fun deleteUser(userAccountId: UserAccountId)

    @Query ("SELECT $UserAccountIdColumn FROM USER_TABLE WHERE $UserAccountServerAddressColumn = :serverUrl")
    fun oneServerAccounts(serverUrl: String): List<UserAccountId>
}