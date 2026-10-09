package com.bob.whopaidit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bob.whopaidit.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY createdAt DESC")
    fun getExpensesForTrip(tripId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE tripId = :tripId")
    fun getTotalExpenseForTrip(tripId: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE tripId = :tripId")
    suspend fun getTotalExpenseSumForTrip(tripId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)

    @Query("DELETE FROM expenses WHERE tripId = :tripId")
    suspend fun deleteExpensesForTrip(tripId: String)
}
