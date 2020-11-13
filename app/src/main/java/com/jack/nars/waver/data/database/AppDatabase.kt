package com.jack.nars.waver.data.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase


@Entity
data class Test(@PrimaryKey val id: Int)


@Dao
interface TestDao {
    @Query("SELECT * FROM Test")
    fun getAll(): List<Test>

    @Insert
    fun insert(channel: Test)
}


@Database(entities = [Test::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getTestDao(): TestDao


    // ======== GET INSTANCE ==========
    companion object {
        private const val DATABASE_NAME = "waver-db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}