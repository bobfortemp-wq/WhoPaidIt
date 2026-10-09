package com.bob.whopaidit.data.repository

import com.bob.whopaidit.data.local.dao.ExpenseDao
import com.bob.whopaidit.data.local.dao.TripDao
import com.bob.whopaidit.data.local.entity.ExpenseEntity
import com.bob.whopaidit.data.local.entity.TripEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val expenseDao: ExpenseDao,
) {

    fun getAllTrips(): Flow<List<TripEntity>> = tripDao.getAllTrips()

    suspend fun insertTrip(trip: TripEntity) = withContext(Dispatchers.IO) {
        tripDao.insertTrip(trip)
    }

    suspend fun deleteTripById(tripId: String) = withContext(Dispatchers.IO) {
        tripDao.deleteTripById(tripId)
        expenseDao.deleteExpensesForTrip(tripId)
    }

    // Expense DAO Operations
    fun getExpensesForTrip(tripId: String): Flow<List<ExpenseEntity>> = expenseDao.getExpensesForTrip(tripId)

    suspend fun insertExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
        recalculateTripTotalExpense(expense.tripId)
    }

    suspend fun deleteExpenseById(expenseId: String, tripId: String) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpenseById(expenseId)
        recalculateTripTotalExpense(tripId)
    }

    private suspend fun recalculateTripTotalExpense(tripId: String) {
        val totalSum = expenseDao.getTotalExpenseSumForTrip(tripId) ?: 0.0
        val formattedTotal = String.format(Locale.US, "₹%.2f", totalSum)
        val currentTrip = tripDao.getTripById(tripId)
        if (currentTrip != null) {
            tripDao.insertTrip(currentTrip.copy(totalExpense = formattedTotal))
        }
    }
}
