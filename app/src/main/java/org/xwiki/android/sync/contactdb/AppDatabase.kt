package org.xwiki.android.sync.contactdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.xwiki.android.sync.contactdb.dao.AccountsDao
import org.xwiki.android.sync.contactdb.dao.AllUsersCacheDao
import org.xwiki.android.sync.contactdb.dao.GroupsCacheDao
import org.xwiki.android.sync.utils.SelectedGroupsListConverter


@Database(entities = [UserAccount::class, AccountAllUsersEntity::class, GroupsCacheEntity::class], version = 1, exportSchema = false)
@TypeConverters(SelectedGroupsListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usersDao(): AccountsDao
    abstract fun allUsersCacheDao(): AllUsersCacheDao
    abstract fun groupsCacheDao(): GroupsCacheDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "user.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}