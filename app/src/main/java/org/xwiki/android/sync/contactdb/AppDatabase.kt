package org.xwiki.android.sync.contactdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.xwiki.android.sync.utils.SelectedGroupsListConverter


@Database(entities = [User::class, SyncTypeAllUsersList::class, SyncTypeGroupsList::class], version = 1, exportSchema = false)
@TypeConverters(SelectedGroupsListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun syncTypeAllUsersListDao(): SyncTypeAllUsersDao
    abstract fun syncTypeGroupsListDao(): SyncTypeGroupsListDao

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