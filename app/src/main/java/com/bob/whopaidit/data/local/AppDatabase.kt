package com.bob.whopaidit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bob.whopaidit.data.local.dao.ExpenseDao
import com.bob.whopaidit.data.local.dao.TripDao
import com.bob.whopaidit.data.local.entity.ExpenseEntity
import com.bob.whopaidit.data.local.entity.TripEntity

@Database(
    entities = [TripEntity::class, ExpenseEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun expenseDao(): ExpenseDao
}
