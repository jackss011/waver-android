package com.jack.nars.waver.data.database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Singleton


@InstallIn(ApplicationComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        Timber.i("Providing database instance")
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideProfileDao(appDatabase: AppDatabase): ProfileDao {
        Timber.i("Providing profile dao")
        return appDatabase.profileDao()
    }
}