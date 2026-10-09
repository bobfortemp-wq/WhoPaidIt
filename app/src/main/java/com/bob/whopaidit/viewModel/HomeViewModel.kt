package com.bob.whopaidit.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bob.whopaidit.data.local.entity.ExpenseEntity
import com.bob.whopaidit.data.local.entity.TripEntity
import com.bob.whopaidit.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tripRepository: TripRepository,
) : ViewModel() {

    val trips: StateFlow<List<TripEntity>> = tripRepository.getAllTrips()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun getExpensesForTrip(tripId: String): Flow<List<ExpenseEntity>> =
        tripRepository.getExpensesForTrip(tripId)

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            tripRepository.insertExpense(expense)
        }
    }

    fun deleteExpense(expenseId: String, tripId: String) {
        viewModelScope.launch {
            tripRepository.deleteExpenseById(expenseId, tripId)
        }
    }

    fun createTrip(
        id: String,
        title: String,
        description: String,
        currency: String,
        startDate: String,
        endDate: String,
        members: List<String>,
    ) {
        viewModelScope.launch {
            val entity = TripEntity(
                id = id,
                title = title,
                description = description,
                currency = currency,
                startDate = startDate.ifBlank { "Upcoming" },
                endDate = endDate.ifBlank { "Upcoming" },
                memberCount = members.size,
                members = members.joinToString(","),
                totalExpense = "₹0.00",
                status = "Active",
                isSettled = false,
                createdAt = System.currentTimeMillis(),
            )
            tripRepository.insertTrip(entity)
        }
    }

    fun updateTrip(
        id: String,
        title: String,
        description: String,
        currency: String,
        startDate: String,
        endDate: String,
        members: List<String>,
    ) {
        viewModelScope.launch {
            val entity = TripEntity(
                id = id,
                title = title,
                description = description,
                currency = currency,
                startDate = startDate.ifBlank { "Upcoming" },
                endDate = endDate.ifBlank { "Upcoming" },
                memberCount = members.size,
                members = members.joinToString(","),
                totalExpense = "₹0.00",
                status = "Active",
                isSettled = false,
                createdAt = System.currentTimeMillis(),
            )
            tripRepository.insertTrip(entity)
        }
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            tripRepository.deleteTripById(tripId)
        }
    }
}
