package org.xwiki.android.sync.contactdb

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    @Query ("SELECT * from user_table")
    fun getAllAccount () : LiveData<List<User>>

    @Query ("SELECT * FROM user_table WHERE account_name LIKE :name")
    fun findByAccountName(name: String): LiveData<User>

    @Query ("SELECT * FROM user_table WHERE account_name LIKE :name")
    fun getAccountByName(name: String): User

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertAccount (user: User)

    @Update (onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser (user: User)

    @Delete
    fun deleteAccount (user: User)
}