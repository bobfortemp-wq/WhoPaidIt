package com.bob.whopaidit.di

import android.content.Context
import androidx.room.Room
import com.bob.whopaidit.data.local.AppDatabase
import com.bob.whopaidit.data.local.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "whopaidit_db",
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTripDao(database: AppDatabase): TripDao {
        return database.tripDao()
    }

    @Provides
    fun provideExpenseDao(database: AppDatabase): com.bob.whopaidit.data.local.dao.ExpenseDao {
        return database.expenseDao()
    }
}
