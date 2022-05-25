package com.jack.nars.waver.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber


@Database(
    entities = [Profile::class, LoopInProfile::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao


    // ======== GET INSTANCE ==========
    companion object {
        private const val DATABASE_NAME = "waver-db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(@ApplicationContext context: Context): AppDatabase {
            instance?.also { return it }

            synchronized(this) {
                return buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            Timber.i("Database built!")

            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}